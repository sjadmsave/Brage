package com.example.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

interface SoundPlayer {
    var enabled: Boolean
    fun playUnscrew()
    fun playBoltLock()
    fun playShieldBreak()
    fun playBlocked()
    fun playPlankDetached()
    fun playDrill()
    fun playUndo()
    fun playHint()
    fun playPowerup()
    fun playVictory()
    fun playFail()
}

/**
 * 100% offline digital sci-fi & precision-tech sound effects synthesizer using AudioTrack.
 */
class SoundEffectsManager : SoundPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sampleRate = 22050

    @Volatile
    override var enabled: Boolean = true

    private val unscrewBuffer: ShortArray by lazy {
        val primary = synthTone(75, 680.0, 1460.0, 28.0, 0.28)
        val shimmer = synthTone(60, 1360.0, 2200.0, 36.0, 0.14)
        mixBuffers(primary, shimmer)
    }

    private val boltLockBuffer: ShortArray by lazy {
        val lock = synthTone(85, 480.0, 260.0, 24.0, 0.34)
        val pip = synthTone(55, 1480.0, 1120.0, 42.0, 0.22)
        mixBuffers(lock, pip)
    }

    private val shieldBreakBuffer: ShortArray by lazy {
        val crack = synthTone(65, 1650.0, 620.0, 32.0, 0.30)
        val ring = synthTone(95, 1240.0, 1860.0, 24.0, 0.22)
        mixBuffers(crack, ring)
    }

    private val blockedBuffer: ShortArray by lazy {
        val pulse1 = synthTone(55, 185.0, 130.0, 20.0, 0.30)
        val pulse2 = synthTone(70, 145.0, 95.0, 18.0, 0.30)
        concatBuffers(pulse1, pulse2)
    }

    private val plankDetachedBuffer: ShortArray by lazy {
        val drop = synthTone(150, 520.0, 180.0, 16.0, 0.34)
        val chime = synthTone(110, 880.0, 660.0, 22.0, 0.22)
        mixBuffers(drop, chime)
    }

    private val drillBuffer: ShortArray by lazy {
        val buzz = synthTone(120, 420.0, 1120.0, 16.0, 0.28)
        val laser = synthTone(90, 1800.0, 920.0, 22.0, 0.20)
        mixBuffers(buzz, laser)
    }

    private val undoBuffer: ShortArray by lazy {
        synthTone(65, 980.0, 520.0, 30.0, 0.24)
    }

    private val hintBuffer: ShortArray by lazy {
        val ping = synthTone(120, 1046.5, 1046.5, 18.0, 0.25)
        val overtone = synthTone(90, 1567.98, 1567.98, 24.0, 0.14)
        mixBuffers(ping, overtone)
    }

    private val victoryBuffer: ShortArray by lazy {
        val c5 = synthHarmonicNote(85, 523.25, 1046.50, 14.0, 0.30)
        val e5 = synthHarmonicNote(85, 659.25, 1318.51, 14.0, 0.30)
        val g5 = synthHarmonicNote(85, 783.99, 1567.98, 14.0, 0.30)
        val b5 = synthHarmonicNote(95, 987.77, 1975.53, 12.0, 0.32)
        val c6 = synthHarmonicNote(280, 1046.50, 2093.00, 6.5, 0.38)
        concatBuffers(c5, e5, g5, b5, c6)
    }

    private val failBuffer: ShortArray by lazy {
        val n1 = synthTone(110, 392.0, 349.0, 14.0, 0.28)
        val n2 = synthTone(130, 311.0, 246.0, 12.0, 0.30)
        val n3 = synthTone(220, 220.0, 146.0, 9.0, 0.32)
        concatBuffers(n1, n2, n3)
    }

    private val powerupBuffer: ShortArray by lazy {
        val p1 = synthHarmonicNote(85, 587.3, 1174.6, 20.0, 0.25)
        val p2 = synthHarmonicNote(140, 880.0, 1760.0, 14.0, 0.28)
        concatBuffers(p1, p2)
    }

    override fun playUnscrew() = playAsync(unscrewBuffer)
    override fun playBoltLock() = playAsync(boltLockBuffer)
    override fun playShieldBreak() = playAsync(shieldBreakBuffer)
    override fun playBlocked() = playAsync(blockedBuffer)
    override fun playPlankDetached() = playAsync(plankDetachedBuffer)
    override fun playDrill() = playAsync(drillBuffer)
    override fun playUndo() = playAsync(undoBuffer)
    override fun playHint() = playAsync(hintBuffer)
    override fun playPowerup() = playAsync(powerupBuffer)
    override fun playVictory() = playAsync(victoryBuffer)
    override fun playFail() = playAsync(failBuffer)

    private fun playAsync(pcm: ShortArray) {
        if (!enabled) return
        scope.launch {
            runCatching {
                val byteCount = pcm.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(byteCount)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                val sleepMs = (pcm.size * 1000L / sampleRate) + 40L
                Thread.sleep(sleepMs)
                track.stop()
                track.release()
            }
        }
    }

    private fun synthHarmonicNote(
        durationMs: Int,
        fundamental: Double,
        harmonic: Double,
        decay: Double,
        volume: Double
    ): ShortArray {
        val base = synthTone(durationMs, fundamental, fundamental, decay, volume * 0.78)
        val over = synthTone(durationMs, harmonic, harmonic, decay * 1.4, volume * 0.32)
        return mixBuffers(base, over)
    }

    private fun synthTone(
        durationMs: Int,
        startFreq: Double,
        endFreq: Double,
        decay: Double,
        volume: Double
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val frac = i.toDouble() / count
            val freq = startFreq + (endFreq - startFreq) * frac
            phase += 2.0 * PI * freq / sampleRate
            val env = exp(-decay * t) * (1.0 - exp(-300.0 * t))
            val sample = (sin(phase) * env * volume * Short.MAX_VALUE).toInt()
            out[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun mixBuffers(a: ShortArray, b: ShortArray): ShortArray {
        val len = maxOf(a.size, b.size)
        return ShortArray(len) { i ->
            val va = if (i < a.size) a[i].toInt() else 0
            val vb = if (i < b.size) b[i].toInt() else 0
            (va + vb).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun concatBuffers(vararg parts: ShortArray): ShortArray {
        val total = parts.sumOf { it.size }
        val out = ShortArray(total)
        var offset = 0
        for (p in parts) {
            p.copyInto(out, offset)
            offset += p.size
        }
        return out
    }
}
