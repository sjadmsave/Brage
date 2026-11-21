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
import com.example.model.Bolt
import com.example.model.DifficultyTier
import com.example.model.HintMove
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

data class GameUiState(
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
    val parMoves: Int = 3,
    val canUndo: Boolean = false,
    val elapsedSeconds: Int = 0,
    val isWon: Boolean = false,
    val starsEarned: Int = 0,
    val particles: List<Particle> = emptyList(),
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
) {
    val activePlanksCount: Int
        get() = planks.count { it.status == PlankStatus.PINNED || it.status == PlankStatus.SWINGING }
    val totalPlanksCount: Int
        get() = planks.size
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
    private var holeById: Map<Int, BoardHole> = emptyMap()

    init {
        viewModelScope.launch {
            repository.progressFlow.collect { progress ->
                soundManager.enabled = progress.soundEnabled
                _uiState.update {
                    it.copy(
                        soundEnabled = progress.soundEnabled,
                        hapticsEnabled = progress.hapticsEnabled
                    )
                }
            }
        }
    }

    internal fun setSoundPlayerForTest(player: SoundPlayer) {
        player.enabled = _uiState.value.soundEnabled
        soundManager = player
    }

    fun loadLevel(levelNumber: Int) {
        val spec = LevelCatalog.getLevel(levelNumber)
        holeById = spec.holes.associateBy { it.id }
        undoStack.clear()
        physicsJob?.cancel()
        timerJob?.cancel()
        flashJob?.cancel()

        _uiState.update { current ->
            current.copy(
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
                parMoves = spec.parMoves,
                canUndo = false,
                elapsedSeconds = 0,
                isWon = false,
                starsEarned = 0,
                particles = emptyList()
            )
        }
        startTimer()
    }

    fun restartLevel() {
        loadLevel(_uiState.value.levelNumber)
    }

    fun toggleSound() {
        setSoundEnabled(!_uiState.value.soundEnabled)
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundManager.enabled = enabled
        _uiState.update { it.copy(soundEnabled = enabled) }
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun onBoardTap(boardX: Float, boardY: Float) {
        val state = _uiState.value
        if (state.isWon) return

        val tappedBolt = state.bolts.minByOrNull { bolt ->
            val h = holeById[bolt.holeId] ?: return@minByOrNull Float.MAX_VALUE
            hypot(boardX - h.x, boardY - h.y)
        }?.takeIf { bolt ->
            val h = holeById[bolt.holeId] ?: return@takeIf false
            hypot(boardX - h.x, boardY - h.y) <= 28f
        }

        if (tappedBolt != null) {
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

    private fun executeMove(boltId: Int, targetHole: BoardHole) {
        val state = _uiState.value
        undoStack.addLast(
            BoardSnapshot(
                bolts = state.bolts,
                planks = state.planks,
                moves = state.moves
            )
        )

        val newBolts = state.bolts.map { b ->
            if (b.id == boltId) b.copy(holeId = targetHole.id) else b
        }

        var droppedAnyPlank = false
        val newParticles = state.particles.toMutableList()
        newParticles.addAll(spawnSparks(targetHole.x, targetHole.y, Color(0xFF00D4FF), count = 12))

        val newPlanks = state.planks.map { plank ->
            if (plank.status == PlankStatus.FALLING || plank.status == PlankStatus.CLEARED) {
                plank
            } else {
                val pins = BoardGeometry.getPinningBolts(plank, newBolts, holeById)
                when (pins.size) {
                    0 -> {
                        droppedAnyPlank = true
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
        if (droppedAnyPlank) {
            soundManager.playPlankDetached()
            emitHaptic(HapticEvent.PLANK_DROP)
        } else {
            emitHaptic(HapticEvent.SUCCESS)
        }

        _uiState.update {
            it.copy(
                bolts = newBolts,
                planks = newPlanks,
                selectedBoltId = null,
                availableHoleIds = emptySet(),
                hintMove = null,
                hintUnavailable = false,
                moves = it.moves + 1,
                canUndo = undoStack.isNotEmpty(),
                particles = newParticles
            )
        }

        ensurePhysicsRunning()
    }

    fun undoMove() {
        val snapshot = undoStack.removeLastOrNull() ?: return
        _uiState.update {
            it.copy(
                bolts = snapshot.bolts,
                planks = snapshot.planks,
                moves = snapshot.moves,
                selectedBoltId = null,
                availableHoleIds = emptySet(),
                hintMove = null,
                hintUnavailable = false,
                canUndo = undoStack.isNotEmpty()
            )
        }
        soundManager.playUndo()
        emitHaptic(HapticEvent.LIGHT_TAP)
        ensurePhysicsRunning()
    }

    fun requestHint() {
        val state = _uiState.value
        if (state.isWon || state.isComputingHint) return

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
                        hintUnavailable = false
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
                                updatedPlanks.add(
                                    plank.copy(status = PlankStatus.FALLING, velocityY = 45f)
                                )
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
                                    var nextAngVel = (plank.angularVelocity + torque * dt * 6.2f) * 0.88f
                                    var nextAngle = plank.angleRad + nextAngVel * dt
                                    var (nextCx, nextCy) = BoardGeometry.centerFromPivot(
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

                if (allCleared && !state.isWon) {
                    val stars = calculateStars(state.moves, state.parMoves)
                    val confetti = spawnVictoryConfetti()
                    _uiState.update {
                        it.copy(
                            planks = updatedPlanks,
                            particles = updatedParticles + confetti,
                            availableHoleIds = availHoles,
                            isWon = true,
                            starsEarned = stars
                        )
                    }
                    timerJob?.cancel()
                    soundManager.playVictory()
                    emitHaptic(HapticEvent.VICTORY)
                    viewModelScope.launch {
                        repository.recordLevelComplete(state.levelNumber, stars, state.moves)
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

                if (!stillAnimating && !allCleared) {
                    break
                }
                if (allCleared && updatedParticles.isEmpty()) {
                    break
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (!_uiState.value.isWon) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
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
