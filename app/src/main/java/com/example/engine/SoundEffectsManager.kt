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
    fun playBlocked()
    fun playPlankDetached()
    fun playUndo()
    fun playHint()
    fun playVictory()
}

/**
 * 100% offline digital sci-fi & precision-tech sound effects synthesizer using AudioTrack.
 */
class SoundEffectsManager : SoundPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sampleRate = 22050

    @Volatile
    override var enabled: Boolean = true

    // Futuristic upward servo-chirp when unscrewing/selecting a bolt
    private val unscrewBuffer: ShortArray by lazy {
        val primary = synthTone(durationMs = 75, startFreq = 680.0, endFreq = 1460.0, decay = 28.0, volume = 0.28)
        val shimmer = synthTone(durationMs = 60, startFreq = 1360.0, endFreq = 2200.0, decay = 36.0, volume = 0.14)
        mixBuffers(primary, shimmer)
    }

    // Precision magnetic lock-in click + resonant confirmation ping when bolt seats in socket
    private val boltLockBuffer: ShortArray by lazy {
        val lock = synthTone(durationMs = 85, startFreq = 480.0, endFreq = 260.0, decay = 24.0, volume = 0.34)
        val pip = synthTone(durationMs = 55, startFreq = 1480.0, endFreq = 1120.0, decay = 42.0, volume = 0.22)
        mixBuffers(lock, pip)
    }

    // Dual low digital error buzz when tapping a blocked socket
    private val blockedBuffer: ShortArray by lazy {
        val pulse1 = synthTone(durationMs = 55, startFreq = 185.0, endFreq = 130.0, decay = 20.0, volume = 0.30)
        val pulse2 = synthTone(durationMs = 70, startFreq = 145.0, endFreq = 95.0, decay = 18.0, volume = 0.30)
        concatBuffers(pulse1, pulse2)
    }

    // Tech power-down swoosh + metallic harmonic chime when a plate detaches and falls
    private val plankDetachedBuffer: ShortArray by lazy {
        val drop = synthTone(durationMs = 150, startFreq = 520.0, endFreq = 180.0, decay = 16.0, volume = 0.34)
        val chime = synthTone(durationMs = 110, startFreq = 880.0, endFreq = 660.0, decay = 22.0, volume = 0.22)
        mixBuffers(drop, chime)
    }

    // Crisp reverse-chirp for Undo
    private val undoBuffer: ShortArray by lazy {
        synthTone(durationMs = 65, startFreq = 980.0, endFreq = 520.0, decay = 30.0, volume = 0.24)
    }

    // Clean radar ping for Hint
    private val hintBuffer: ShortArray by lazy {
        val ping = synthTone(durationMs = 120, startFreq = 1046.5, endFreq = 1046.5, decay = 18.0, volume = 0.25)
        val overtone = synthTone(durationMs = 90, startFreq = 1567.98, endFreq = 1567.98, decay = 24.0, volume = 0.14)
        mixBuffers(ping, overtone)
    }

    // Triumphant cyber-electronic major-9th arpeggio when completing a level
    private val victoryBuffer: ShortArray by lazy {
        val c5 = synthHarmonicNote(85, 523.25, 1046.50, 14.0, 0.30)
        val e5 = synthHarmonicNote(85, 659.25, 1318.51, 14.0, 0.30)
        val g5 = synthHarmonicNote(85, 783.99, 1567.98, 14.0, 0.30)
        val b5 = synthHarmonicNote(95, 987.77, 1975.53, 12.0, 0.32)
        val c6 = synthHarmonicNote(280, 1046.50, 2093.00, 6.5, 0.38)
        concatBuffers(c5, e5, g5, b5, c6)
    }

    override fun playUnscrew() = playAsync(unscrewBuffer)
    override fun playBoltLock() = playAsync(boltLockBuffer)
    override fun playBlocked() = playAsync(blockedBuffer)
    override fun playPlankDetached() = playAsync(plankDetachedBuffer)
    override fun playUndo() = playAsync(undoBuffer)
    override fun playHint() = playAsync(hintBuffer)
    override fun playVictory() = playAsync(victoryBuffer)

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
