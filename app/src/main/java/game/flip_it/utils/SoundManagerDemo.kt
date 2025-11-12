package game.flip_it.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManagerDemo(private val context: Context) {

    private val soundPool: SoundPool
    private val soundMap: MutableMap<String, Int> = mutableMapOf()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    /**
     * Preload a sound resource into memory
     */
    fun loadSound(name: String, resId: Int) {
        val soundId = soundPool.load(context, resId, 1)
        soundMap[name] = soundId
    }

    /**
     * Play a previously loaded sound by its name
     */
    fun playSound(name: String, volume: Float = 1.0f) {
        soundMap[name]?.let { soundId ->
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    /**
     * Release resources when done
     */
    fun release() {
        soundPool.release()
    }
}
