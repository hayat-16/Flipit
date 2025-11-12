package game.flip_it.utils

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast

object SoundManager {

    private var mediaPlayer: MediaPlayer? = null

    fun playSound(context: Context, soundResId: Int) {
        stopSound() // ensure no overlapping
        ensureMinVolume(context, minLevel = 8)
        mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer?.apply {
            setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }
            start()
        }
//        mediaPlayer?.start()
//        mediaPlayer?.setOnCompletionListener {
//            it.release()
//        }
    }

    fun stopSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: IllegalStateException) {
            // Player was in invalid state; just release and ignore
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

//    fun stopSound() {
//        mediaPlayer?.stop()
//        mediaPlayer?.release()
//        mediaPlayer = null
//    }
}
