package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProgressRepository
import com.example.engine.BoardGeometry
import com.example.engine.LevelCatalog
import com.example.engine.PuzzleSolver
import com.example.engine.SoundEffectsManager
import com.example.engine.SoundPlayer
import com.example.model.BoardHole
import com.example.model.BoardSnapshot
import com.example.model.BoardTheme
import com.example.model.Bolt
import com.example.model.DifficultyTier
import com.example.model.GameMode
import com.example.model.HintMove
import com.example.model.LevelSpec
import com.example.model.Particle
import com.example.model.Plank
import com.example.model.PlankStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class HapticEvent {
    LIGHT_TAP,
    SUCCESS,
    BLOCKED,
    PLANK_DROP,
    VICTORY
}

enum class FailReason {
    TIME_UP,
    OUT_OF_MOVES
}

data class GameUiState(
    val mode: GameMode = GameMode.CLASSIC,
    val levelNumber: Int = 1,
    val tier: DifficultyTier = DifficultyTier.BEGINNER,
    val holes: List<BoardHole> = emptyList(),
    val bolts: List<Bolt> = emptyList(),
    val planks: List<Plank> = emptyList(),
    val selectedBoltId: Int? = null,
    val availableHoleIds: Set<Int> = emptySet(),
    val blockedFlashHoleId: Int? = null,
    val hintMove: HintMove? = null,
    val isComputingHint: Boolean = false,
    val hintUnavailable: Boolean = false,
    val moves: Int = 0,
    val movesBudget: Int? = null,
    val parMoves: Int = 3,
    val canUndo: Boolean = false,
    val elapsedSeconds: Int = 0,
    val remainingSeconds: Int? = null,
    val isWon: Boolean = false,
    val isLost: Boolean = false,
    val failReason: FailReason? = null,
    val starsEarned: Int = 0,
    val particles: List<Particle> = emptyList(),
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val drillsLeft: Int = 1,
    val cuttersLeft: Int = 1,
    val isDrillMode: Boolean = false,
    val isCutterMode: Boolean = false,
    val statusMessage: String? = null,
    val comboCount: Int = 0,
    val sessionStreak: Int = 0,
    val techCredits: Int = 60,
    val lastCreditsEarned: Int = 0,
    val boardTheme: BoardTheme = BoardTheme.CYBER_NAVY
) {
    val activePlanksCount: Int
        get() = planks.count { it.status == PlankStatus.PINNED || it.status == PlankStatus.SWINGING }
    val totalPlanksCount: Int
        get() = planks.size

    companion object {
        const val DRILL_REFILL_COST = 50
        const val CUTTER_REFILL_COST = 75
    }
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProgressRepository(application.applicationContext)
    private var soundManager: SoundPlayer = SoundEffectsManager()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _hapticEvents = MutableSharedFlow<HapticEvent>(extraBufferCapacity = 8)
    val hapticEvents: SharedFlow<HapticEvent> = _hapticEvents.asSharedFlow()

    private val undoStack = ArrayDeque<BoardSnapshot>()
    private var physicsJob: Job? = null
    private var timerJob: Job? = null
    private var flashJob: Job? = null
    private var messageJob: Job? = null
    private var holeById: Map<Int, BoardHole> = emptyMap()
    private var lastDetachTimeMs: Long = 0L

    init {
        viewModelScope.launch {
            repository.progressFlow.collect { progress ->
                soundManager.enabled = progress.soundEnabled
                _uiState.update {
                    it.copy(
                        soundEnabled = progress.soundEnabled,
                        hapticsEnabled = progress.hapticsEnabled,
                        techCredits = progress.techCredits,
                        boardTheme = progress.selectedTheme
                    )
                }
            }
        }
    }

    internal fun setSoundPlayerForTest(player: SoundPlayer) {
        player.enabled = _uiState.value.soundEnabled
        soundManager = player
    }

    fun todayDateKey(): Int {
        return SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()).toIntOrNull() ?: 20261121
    }

    fun loadLevel(levelNumber: Int, mode: GameMode = GameMode.CLASSIC, keepStreak: Boolean = false) {
        val currentStreak = if (keepStreak) _uiState.value.sessionStreak else 0
        val spec: LevelSpec = when (mode) {
            GameMode.CLASSIC -> LevelCatalog.getLevel(levelNumber)
            GameMode.TIME_ATTACK -> LevelCatalog.getArcadeRoundLevel(levelNumber, modeSalt = 1)
            GameMode.PRECISION -> LevelCatalog.getArcadeRoundLevel(levelNumber, modeSalt = 2)
            GameMode.ENDLESS -> LevelCatalog.getArcadeRoundLevel(levelNumber, modeSalt = 3)
            GameMode.DAILY -> LevelCatalog.getDailyLevel(todayDateKey())
        }

        holeById = spec.holes.associateBy { it.id }
        undoStack.clear()
        physicsJob?.cancel()
        timerJob?.cancel()
        flashJob?.cancel()
        messageJob?.cancel()
        lastDetachTimeMs = 0L

        val initialTimeBudget = if (mode == GameMode.TIME_ATTACK) {
            (spec.parMoves * 5 + 24).coerceIn(45, 110)
        } else {
            null
        }
        val initialMoveBudget = if (mode == GameMode.PRECISION) {
            spec.parMoves + 2
        } else {
            null
        }

        _uiState.update { current ->
            current.copy(
                mode = mode,
                levelNumber = spec.levelNumber,
                tier = spec.tier,
                holes = spec.holes,
                bolts = spec.initialBolts,
                planks = spec.initialPlanks,
                selectedBoltId = null,
                availableHoleIds = emptySet(),
                blockedFlashHoleId = null,
                hintMove = null,
                isComputingHint = false,
                hintUnavailable = false,
                moves = 0,
                movesBudget = initialMoveBudget,
                parMoves = spec.parMoves,
                canUndo = false,
                elapsedSeconds = 0,
                remainingSeconds = initialTimeBudget,
                isWon = false,
                isLost = false,
                failReason = null,
                starsEarned = 0,
                particles = emptyList(),
                drillsLeft = if (mode == GameMode.PRECISION) 0 else 1,
                cuttersLeft = if (mode == GameMode.CLASSIC || mode == GameMode.ENDLESS) 1 else 0,
                isDrillMode = false,
                isCutterMode = false,
                statusMessage = null,
                comboCount = 0,
                sessionStreak = currentStreak,
                lastCreditsEarned = 0
            )
        }
        startTimer()
    }

    fun restartLevel() {
        val state = _uiState.value
        loadLevel(state.levelNumber, state.mode, keepStreak = false)
    }

    fun toggleSound() {
        setSoundEnabled(!_uiState.value.soundEnabled)
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundManager.enabled = enabled
        _uiState.update { it.copy(soundEnabled = enabled) }
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun toggleDrillMode() {
        val state = _uiState.value
        if (state.isWon || state.isLost) return
        if (state.drillsLeft <= 0) {
            if (state.mode == GameMode.PRECISION) {
                showTransientMessage("المثقاب غير متاح في تحدي الدقة")
                return
            }
            val cost = GameUiState.DRILL_REFILL_COST
            if (state.techCredits >= cost) {
                _uiState.update {
                    it.copy(
                        drillsLeft = 1,
                        techCredits = it.techCredits - cost,
                        isDrillMode = true,
                        isCutterMode = false,
                        selectedBoltId = null,
                        availableHoleIds = emptySet()
                    )
                }
                soundManager.playPowerup()
                emitHaptic(HapticEvent.SUCCESS)
                showTransientMessage("تم شحن مثقاب الليزر (-$cost⚡) • اضغط مساحة فارغة")
                viewModelScope.launch { repository.spendCredits(cost) }
            } else {
                soundManager.playBlocked()
                showTransientMessage("تحتاج $cost⚡ طاقة رقمية لشحن المثقاب")
            }
            return
        }
        val next = !state.isDrillMode
        _uiState.update {
            it.copy(
                isDrillMode = next,
                isCutterMode = false,
                selectedBoltId = null,
                availableHoleIds = emptySet()
            )
        }
        if (next) showTransientMessage("اضغط في أي مساحة فارغة لحفر منفذ ليزر جديد")
    }

    fun toggleCutterMode() {
        val state = _uiState.value
        if (state.isWon || state.isLost) return
        if (state.cuttersLeft <= 0) {
            if (state.mode == GameMode.PRECISION || state.mode == GameMode.TIME_ATTACK) {
                showTransientMessage("القاطع مخصص للطور الكلاسيكي واللانهائي واليومي")
                return
            }
            val cost = GameUiState.CUTTER_REFILL_COST
            if (state.techCredits >= cost) {
                _uiState.update {
                    it.copy(
                        cuttersLeft = 1,
                        techCredits = it.techCredits - cost,
                        isCutterMode = true,
                        isDrillMode = false,
                        selectedBoltId = null,
                        availableHoleIds = emptySet()
                    )
                }
                soundManager.playPowerup()
                emitHaptic(HapticEvent.SUCCESS)
                showTransientMessage("تم شحن قاطع النبضات (-$cost⚡) • اضغط على لوح")
                viewModelScope.launch { repository.spendCredits(cost) }
            } else {
                soundManager.playBlocked()
                showTransientMessage("تحتاج $cost⚡ طاقة رقمية لشحن القاطع")
            }
            return
        }
        val next = !state.isCutterMode
        _uiState.update {
            it.copy(
                isCutterMode = next,
                isDrillMode = false,
                selectedBoltId = null,
                availableHoleIds = emptySet()
            )
        }
        if (next) showTransientMessage("اضغط على أي لوح لتفكيكه بالليزر فوراً")
    }

    fun onBoardTap(boardX: Float, boardY: Float) {
        val state = _uiState.value
        if (state.isWon || state.isLost) return

        // Handle Laser Drill Tool
        if (state.isDrillMode) {
            handleDrillTap(boardX, boardY)
            return
        }

        // Handle Pulse Cutter Tool
        if (state.isCutterMode) {
            handleCutterTap(boardX, boardY)
            return
        }

        // 1. Check if tapped near any Bolt
        val tappedBolt = state.bolts.minByOrNull { bolt ->
            val h = holeById[bolt.holeId] ?: return@minByOrNull Float.MAX_VALUE
            hypot(boardX - h.x, boardY - h.y)
        }?.takeIf { bolt ->
            val h = holeById[bolt.holeId] ?: return@takeIf false
            hypot(boardX - h.x, boardY - h.y) <= 28f
        }

        if (tappedBolt != null) {
            if (tappedBolt.reinforced) {
                // First tap breaks the protective energy shield
                undoStack.addLast(BoardSnapshot(state.holes, state.bolts, state.planks, state.moves))
                val updatedBolts = state.bolts.map { b ->
                    if (b.id == tappedBolt.id) b.copy(reinforced = false) else b
                }
                val h = holeById[tappedBolt.holeId]
                val shieldSparks = if (h != null) spawnSparks(h.x, h.y, Color(0xFFFF4D6D), count = 12) else emptyList()
                val avail = computeAvailableHoles(state.holes, updatedBolts, state.planks)
                val nextMoves = state.moves + 1
                _uiState.update {
                    it.copy(
                        bolts = updatedBolts,
                        selectedBoltId = tappedBolt.id,
                        availableHoleIds = avail,
                        moves = nextMoves,
                        canUndo = true,
                        particles = it.particles + shieldSparks
                    )
                }
                soundManager.playShieldBreak()
                emitHaptic(HapticEvent.SUCCESS)
                showTransientMessage("تم كسر درع البرغي! اختر منفذاً لنقله")
                checkPrecisionOutOfMoves()
                ensurePhysicsRunning()
                return
            }

            if (state.selectedBoltId == tappedBolt.id) {
                _uiState.update { it.copy(selectedBoltId = null, availableHoleIds = emptySet()) }
            } else {
                val avail = computeAvailableHoles(state.holes, state.bolts, state.planks)
                _uiState.update {
                    it.copy(
                        selectedBoltId = tappedBolt.id,
                        availableHoleIds = avail,
                        hintUnavailable = false
                    )
                }
                soundManager.playUnscrew()
                emitHaptic(HapticEvent.LIGHT_TAP)
            }
            return
        }

        // 2. If a bolt is selected, check target hole
        val selectedId = state.selectedBoltId ?: return
        val tappedHole = state.holes.minByOrNull { h ->
            hypot(boardX - h.x, boardY - h.y)
        }?.takeIf { h ->
            hypot(boardX - h.x, boardY - h.y) <= 28f
        }

        if (tappedHole == null) {
            _uiState.update { it.copy(selectedBoltId = null, availableHoleIds = emptySet()) }
            return
        }

        val isAvailable = BoardGeometry.isHoleAvailableForBolt(tappedHole, state.bolts, state.planks)
        if (!isAvailable) {
            soundManager.playBlocked()
            emitHaptic(HapticEvent.BLOCKED)
            triggerBlockedFlash(tappedHole.id)
            return
        }

        executeMove(selectedId, tappedHole)
    }

    private fun handleDrillTap(boardX: Float, boardY: Float) {
        val state = _uiState.value
        val clampedX = boardX.coerceIn(44f, BoardGeometry.BOARD_WIDTH - 44f)
        val clampedY = boardY.coerceIn(58f, BoardGeometry.BOARD_HEIGHT - 44f)
        val newId = (state.holes.maxOfOrNull { it.id } ?: 0) + 1
        val candidate = BoardHole(id = newId, x = clampedX, y = clampedY)

        val tooClose = state.holes.any { hypot(it.x - clampedX, it.y - clampedY) < 32f }
        val coveredByPlank = state.planks.any { BoardGeometry.doesPlankBlockHole(it, candidate) }

        if (tooClose || coveredByPlank) {
            soundManager.playBlocked()
            emitHaptic(HapticEvent.BLOCKED)
            showTransientMessage("اختر نقطة فارغة بعيدة عن الألواح والمنافذ")
            return
        }

        undoStack.addLast(BoardSnapshot(state.holes, state.bolts, state.planks, state.moves))
        val newHoles = state.holes + candidate
        holeById = newHoles.associateBy { it.id }
        val sparks = spawnSparks(clampedX, clampedY, Color(0xFF00E699), count = 16)

        _uiState.update {
            it.copy(
                holes = newHoles,
                drillsLeft = (it.drillsLeft - 1).coerceAtLeast(0),
                isDrillMode = false,
                canUndo = true,
                particles = it.particles + sparks
            )
        }
        soundManager.playDrill()
        emitHaptic(HapticEvent.SUCCESS)
        showTransientMessage("تم حفر منفذ ليزر جديد بنجاح!")
        ensurePhysicsRunning()
    }

    private fun handleCutterTap(boardX: Float, boardY: Float) {
        val state = _uiState.value
        val targetPlank = state.planks
            .filter { it.status == PlankStatus.PINNED || it.status == PlankStatus.SWINGING }
            .sortedByDescending { it.layer }
            .firstOrNull { plank ->
                val (lx, ly) = BoardGeometry.worldToLocal(plank, boardX, boardY)
                abs(lx) <= plank.width * 0.5f + 8f && abs(ly) <= plank.height * 0.5f + 8f
            }

        if (targetPlank == null) {
            soundManager.playBlocked()
            showTransientMessage("اضغط مباشرة على أحد الألواح لتفكيكه")
            return
        }

        undoStack.addLast(BoardSnapshot(state.holes, state.bolts, state.planks, state.moves))
        val sparks = spawnSparks(targetPlank.centerX, targetPlank.centerY, targetPlank.woodStyle.grainColor, count = 20)
        val newPlanks = state.planks.map { p ->
            if (p.id == targetPlank.id) {
                p.copy(
                    status = PlankStatus.FALLING,
                    velocityX = Random.nextInt(-40, 41).toFloat(),
                    velocityY = 60f,
                    angularVelocity = 2.2f
                )
            } else {
                p
            }
        }
        _uiState.update {
            it.copy(
                planks = newPlanks,
                cuttersLeft = (it.cuttersLeft - 1).coerceAtLeast(0),
                isCutterMode = false,
                canUndo = true,
                particles = it.particles + sparks
            )
        }
        soundManager.playPlankDetached()
        emitHaptic(HapticEvent.PLANK_DROP)
        ensurePhysicsRunning()
    }

    private fun executeMove(boltId: Int, targetHole: BoardHole) {
        val state = _uiState.value
        undoStack.addLast(BoardSnapshot(state.holes, state.bolts, state.planks, state.moves))

        val newBolts = state.bolts.map { b ->
            if (b.id == boltId) b.copy(holeId = targetHole.id) else b
        }

        var detachedCount = 0
        val newParticles = state.particles.toMutableList()
        newParticles.addAll(spawnSparks(targetHole.x, targetHole.y, Color(0xFF00D4FF), count = 12))

        val newPlanks = state.planks.map { plank ->
            if (plank.status == PlankStatus.FALLING || plank.status == PlankStatus.CLEARED) {
                plank
            } else {
                val pins = BoardGeometry.getPinningBolts(plank, newBolts, holeById)
                when (pins.size) {
                    0 -> {
                        detachedCount++
                        newParticles.addAll(
                            spawnSparks(plank.centerX, plank.centerY, plank.woodStyle.grainColor, count = 16)
                        )
                        plank.copy(
                            status = PlankStatus.FALLING,
                            velocityX = Random.nextInt(-38, 39).toFloat(),
                            velocityY = 45f,
                            angularVelocity = Random.nextFloat() * 3.0f - 1.5f
                        )
                    }
                    1 -> plank.copy(status = PlankStatus.SWINGING)
                    else -> plank.copy(status = PlankStatus.PINNED, angularVelocity = 0f)
                }
            }
        }

        soundManager.playBoltLock()

        var bonusTime = 0
        var newCombo = state.comboCount
        if (detachedCount > 0) {
            soundManager.playPlankDetached()
            emitHaptic(HapticEvent.PLANK_DROP)
            val nowMs = System.currentTimeMillis()
            newCombo = if (nowMs - lastDetachTimeMs <= 2_800L) state.comboCount + detachedCount else detachedCount
            lastDetachTimeMs = nowMs
            if (newCombo >= 2) {
                showTransientMessage("كومبو x$newCombo! ⚡")
            }
            if (state.mode == GameMode.TIME_ATTACK) {
                bonusTime = detachedCount * 4
                showTransientMessage("+${bonusTime}ث بونص! ⏱️")
            }
        } else {
            emitHaptic(HapticEvent.SUCCESS)
        }

        val nextMoves = state.moves + 1
        _uiState.update {
            it.copy(
                bolts = newBolts,
                planks = newPlanks,
                selectedBoltId = null,
                availableHoleIds = emptySet(),
                hintMove = null,
                hintUnavailable = false,
                moves = nextMoves,
                remainingSeconds = it.remainingSeconds?.plus(bonusTime),
                canUndo = undoStack.isNotEmpty(),
                particles = newParticles,
                comboCount = newCombo
            )
        }

        checkPrecisionOutOfMoves()
        ensurePhysicsRunning()
    }

    private fun checkPrecisionOutOfMoves() {
        val state = _uiState.value
        val budget = state.movesBudget ?: return
        if (state.moves >= budget) {
            val anyStillPinned = state.planks.any {
                it.status == PlankStatus.PINNED || it.status == PlankStatus.SWINGING
            }
            if (anyStillPinned && !state.isWon && !state.isLost) {
                timerJob?.cancel()
                soundManager.playFail()
                emitHaptic(HapticEvent.BLOCKED)
                _uiState.update {
                    it.copy(
                        isLost = true,
                        failReason = FailReason.OUT_OF_MOVES,
                        selectedBoltId = null,
                        availableHoleIds = emptySet()
                    )
                }
            }
        }
    }

    fun undoMove() {
        val snapshot = undoStack.removeLastOrNull() ?: return
        holeById = snapshot.holes.associateBy { it.id }
        _uiState.update {
            it.copy(
                holes = snapshot.holes,
                bolts = snapshot.bolts,
                planks = snapshot.planks,
                moves = snapshot.moves,
                selectedBoltId = null,
                availableHoleIds = emptySet(),
                hintMove = null,
                hintUnavailable = false,
                isDrillMode = false,
                isCutterMode = false,
                canUndo = undoStack.isNotEmpty()
            )
        }
        soundManager.playUndo()
        emitHaptic(HapticEvent.LIGHT_TAP)
        ensurePhysicsRunning()
    }

    fun requestHint() {
        val state = _uiState.value
        if (state.isWon || state.isLost || state.isComputingHint) return

        _uiState.update { it.copy(isComputingHint = true, hintUnavailable = false) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                PuzzleSolver.solve(
                    holes = state.holes,
                    bolts = state.bolts,
                    planks = state.planks,
                    maxNodes = 16_000
                )
            }
            val move = result?.moves?.firstOrNull()
            if (move != null) {
                val avail = computeAvailableHoles(
                    _uiState.value.holes,
                    _uiState.value.bolts,
                    _uiState.value.planks
                )
                _uiState.update {
                    it.copy(
                        hintMove = move,
                        selectedBoltId = move.boltId,
                        availableHoleIds = avail,
                        isComputingHint = false,
                        hintUnavailable = false,
                        isDrillMode = false,
                        isCutterMode = false
                    )
                }
                soundManager.playHint()
            } else {
                _uiState.update {
                    it.copy(
                        isComputingHint = false,
                        hintUnavailable = true
                    )
                }
            }
        }
    }

    private fun computeAvailableHoles(
        holes: List<BoardHole>,
        bolts: List<Bolt>,
        planks: List<Plank>
    ): Set<Int> {
        val set = HashSet<Int>()
        for (h in holes) {
            if (BoardGeometry.isHoleAvailableForBolt(h, bolts, planks)) {
                set.add(h.id)
            }
        }
        return set
    }

    private fun ensurePhysicsRunning() {
        if (physicsJob?.isActive == true) return
        physicsJob = viewModelScope.launch {
            var lastTimeNanos = System.nanoTime()
            while (true) {
                delay(16L)
                val now = System.nanoTime()
                val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.005f, 0.033f)
                lastTimeNanos = now

                val state = _uiState.value
                var stillAnimating = false
                val updatedPlanks = ArrayList<Plank>(state.planks.size)

                for (plank in state.planks) {
                    when (plank.status) {
                        PlankStatus.SWINGING -> {
                            val pins = BoardGeometry.getPinningBolts(plank, state.bolts, holeById)
                            if (pins.isEmpty()) {
                                stillAnimating = true
                                updatedPlanks.add(plank.copy(status = PlankStatus.FALLING, velocityY = 45f))
                            } else if (pins.size >= 2) {
                                updatedPlanks.add(plank.copy(status = PlankStatus.PINNED, angularVelocity = 0f))
                            } else {
                                val (pivotBolt, localPivot) = pins[0]
                                val pivotHole = holeById[pivotBolt.holeId]
                                val eqAngle = BoardGeometry.equilibriumAngleForPivot(localPivot)
                                if (pivotHole == null || eqAngle == null) {
                                    updatedPlanks.add(plank.copy(angularVelocity = 0f))
                                } else {
                                    val dx = plank.centerX - pivotHole.x
                                    val dy = plank.centerY - pivotHole.y
                                    var torque = dx * 0.22f
                                    if (abs(dx) < 2.0f && dy < 0f) {
                                        torque += 2.8f
                                    }
                                    val nextAngVel = (plank.angularVelocity + torque * dt * 6.2f) * 0.88f
                                    val nextAngle = plank.angleRad + nextAngVel * dt
                                    val (nextCx, nextCy) = BoardGeometry.centerFromPivot(
                                        pivotHole.x, pivotHole.y, localPivot, nextAngle
                                    )
                                    val candidate = plank.copy(
                                        centerX = nextCx,
                                        centerY = nextCy,
                                        angleRad = nextAngle,
                                        angularVelocity = nextAngVel
                                    )
                                    val collided = state.planks.any { other ->
                                        other.id != plank.id &&
                                            other.layer == plank.layer &&
                                            other.status != PlankStatus.FALLING &&
                                            other.status != PlankStatus.CLEARED &&
                                            BoardGeometry.obbsOverlap(candidate, other, padding = -2.5f)
                                    }
                                    if (collided) {
                                        updatedPlanks.add(plank.copy(angularVelocity = -plank.angularVelocity * 0.2f))
                                    } else {
                                        val deltaToEq = BoardGeometry.normalizeAngleDelta(eqAngle, nextAngle)
                                        if (abs(deltaToEq) < 0.018f && abs(nextAngVel) < 0.08f) {
                                            val (eqCx, eqCy) = BoardGeometry.centerFromPivot(
                                                pivotHole.x, pivotHole.y, localPivot, eqAngle
                                            )
                                            updatedPlanks.add(
                                                plank.copy(
                                                    centerX = eqCx,
                                                    centerY = eqCy,
                                                    angleRad = eqAngle,
                                                    angularVelocity = 0f
                                                )
                                            )
                                        } else {
                                            stillAnimating = true
                                            updatedPlanks.add(candidate)
                                        }
                                    }
                                }
                            }
                        }
                        PlankStatus.FALLING -> {
                            val nextVy = plank.velocityY + 1300f * dt
                            val nextY = plank.centerY + nextVy * dt
                            val nextX = plank.centerX + plank.velocityX * dt
                            val nextAngle = plank.angleRad + plank.angularVelocity * dt
                            val nextAlpha = if (nextY > BoardGeometry.BOARD_HEIGHT - 30f) {
                                (plank.alpha - dt * 2.2f).coerceAtLeast(0f)
                            } else {
                                plank.alpha
                            }
                            if (nextY > BoardGeometry.BOARD_HEIGHT + 90f || nextAlpha <= 0.01f) {
                                updatedPlanks.add(plank.copy(status = PlankStatus.CLEARED, alpha = 0f))
                            } else {
                                stillAnimating = true
                                updatedPlanks.add(
                                    plank.copy(
                                        centerX = nextX,
                                        centerY = nextY,
                                        velocityY = nextVy,
                                        angleRad = nextAngle,
                                        alpha = nextAlpha
                                    )
                                )
                            }
                        }
                        else -> updatedPlanks.add(plank)
                    }
                }

                val updatedParticles = state.particles.mapNotNull { p ->
                    val nextLife = p.life - dt
                    if (nextLife <= 0f) null
                    else p.copy(
                        x = p.x + p.vx * dt,
                        y = p.y + p.vy * dt,
                        vy = p.vy + 420f * dt,
                        life = nextLife,
                        rotation = p.rotation + 4f * dt
                    )
                }
                if (updatedParticles.isNotEmpty()) stillAnimating = true

                val availHoles = if (state.selectedBoltId != null) {
                    computeAvailableHoles(state.holes, state.bolts, updatedPlanks)
                } else {
                    state.availableHoleIds
                }

                val allCleared = updatedPlanks.isNotEmpty() && updatedPlanks.all { it.status == PlankStatus.CLEARED }

                if (allCleared && !state.isWon && !state.isLost) {
                    val stars = calculateStars(state.moves, state.parMoves)
                    val confetti = spawnVictoryConfetti()
                    val nextStreak = if (state.mode == GameMode.TIME_ATTACK) state.sessionStreak + 1 else state.sessionStreak
                    val rewardCredits = when (state.mode) {
                        GameMode.CLASSIC -> stars * 15 + (state.comboCount * 3)
                        GameMode.TIME_ATTACK -> 30 + nextStreak * 8
                        GameMode.PRECISION -> 45
                        GameMode.ENDLESS -> 25 + (state.levelNumber * 2).coerceAtMost(60)
                        GameMode.DAILY -> 80
                    }
                    _uiState.update {
                        it.copy(
                            planks = updatedPlanks,
                            particles = updatedParticles + confetti,
                            availableHoleIds = availHoles,
                            isWon = true,
                            starsEarned = stars,
                            sessionStreak = nextStreak,
                            lastCreditsEarned = rewardCredits,
                            techCredits = it.techCredits + rewardCredits
                        )
                    }
                    timerJob?.cancel()
                    soundManager.playVictory()
                    emitHaptic(HapticEvent.VICTORY)
                    viewModelScope.launch {
                        when (state.mode) {
                            GameMode.CLASSIC -> repository.recordLevelComplete(state.levelNumber, stars, state.moves, rewardCredits)
                            GameMode.TIME_ATTACK -> repository.recordTimeAttackStreak(nextStreak, rewardCredits)
                            GameMode.PRECISION -> repository.recordPrecisionClear(rewardCredits)
                            GameMode.ENDLESS -> repository.recordEndlessRound(state.levelNumber, rewardCredits)
                            GameMode.DAILY -> repository.recordDailyCompleted(todayDateKey(), rewardCredits)
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            planks = updatedPlanks,
                            particles = updatedParticles,
                            availableHoleIds = availHoles
                        )
                    }
                }

                if (!stillAnimating && !allCleared) break
                if (allCleared && updatedParticles.isEmpty()) break
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val s = _uiState.value
                if (s.isWon || s.isLost) continue
                val nextElapsed = s.elapsedSeconds + 1
                val rem = s.remainingSeconds
                if (rem != null) {
                    val nextRem = (rem - 1).coerceAtLeast(0)
                    if (nextRem == 0) {
                        soundManager.playFail()
                        emitHaptic(HapticEvent.BLOCKED)
                        _uiState.update {
                            it.copy(
                                elapsedSeconds = nextElapsed,
                                remainingSeconds = 0,
                                isLost = true,
                                failReason = FailReason.TIME_UP,
                                sessionStreak = 0
                            )
                        }
                        break
                    } else {
                        _uiState.update { it.copy(elapsedSeconds = nextElapsed, remainingSeconds = nextRem) }
                    }
                } else {
                    _uiState.update { it.copy(elapsedSeconds = nextElapsed) }
                }
            }
        }
    }

    private fun triggerBlockedFlash(holeId: Int) {
        flashJob?.cancel()
        flashJob = viewModelScope.launch {
            _uiState.update { it.copy(blockedFlashHoleId = holeId) }
            delay(360L)
            _uiState.update { it.copy(blockedFlashHoleId = null) }
        }
    }

    private fun showTransientMessage(msg: String) {
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = msg) }
            delay(1_800L)
            _uiState.update { it.copy(statusMessage = null) }
        }
    }

    private fun emitHaptic(event: HapticEvent) {
        if (_uiState.value.hapticsEnabled) {
            _hapticEvents.tryEmit(event)
        }
    }

    private fun calculateStars(moves: Int, par: Int): Int {
        return when {
            moves <= par + 1 -> 3
            moves <= par + 4 -> 2
            else -> 1
        }
    }

    private fun spawnSparks(x: Float, y: Float, color: Color, count: Int): List<Particle> {
        return List(count) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = 40f + Random.nextFloat() * 110f
            Particle(
                x = x,
                y = y,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 40f,
                size = 3f + Random.nextFloat() * 3.5f,
                color = color,
                life = 0.45f,
                maxLife = 0.45f,
                rotation = Random.nextFloat() * 6f
            )
        }
    }

    private fun spawnVictoryConfetti(): List<Particle> {
        val colors = listOf(
            Color(0xFF00D4FF),
            Color(0xFF00E699),
            Color(0xFFFFB020),
            Color(0xFFA855F7),
            Color(0xFFF0F6FF)
        )
        return List(44) {
            Particle(
                x = 40f + Random.nextFloat() * (BoardGeometry.BOARD_WIDTH - 80f),
                y = 80f + Random.nextFloat() * 180f,
                vx = Random.nextFloat() * 220f - 110f,
                vy = -120f - Random.nextFloat() * 180f,
                size = 5f + Random.nextFloat() * 5f,
                color = colors[it % colors.size],
                life = 1.6f,
                maxLife = 1.6f,
                rotation = Random.nextFloat() * 6f
            )
        }
    }
}
