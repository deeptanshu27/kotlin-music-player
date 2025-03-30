package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil3.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SongViewActivity : AppCompatActivity() {
    var filePath: String = ""
    var screenWidth: Float = 0f

    val pauseIcon: String = "\uf04c"
    val playIcon: String = "\uf04b"

    var progressBarThread: Job? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_song_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        filePath = intent.getStringExtra("filePath")!!

        setPlayButtonIcon()

        screenWidth = this.windowManager.currentWindowMetrics.bounds.width().toFloat() * (75f / 100f)

        val data = MediaPlayerSingleton.getSongByteArray(filePath)
        var bitmap: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        bitmap = bitmap.scale(
            evalWidth(bitmap.width),
            evalHeight(bitmap.width, bitmap.height),
            false
        )
        findViewById<ImageView>(R.id.songImage).setImageBitmap(bitmap)

        val (songName, songArtist) = MediaPlayerSingleton.getSongData(this, filePath)
        findViewById<TextView>(R.id.songTitle).text = songName
        findViewById<TextView>(R.id.artistName).text = songArtist.replace("/", ", ")

        findViewById<Button>(R.id.playButton).setOnClickListener {
            if (MediaPlayerSingleton.mediaPlayer.duration == 0 || MediaPlayerSingleton.getFilePath() != filePath) {
                MediaPlayerSingleton.setSong(filePath)
            }

            if (MediaPlayerSingleton.mediaPlayer.isPlaying) {
                MediaPlayerSingleton.mediaPlayer.pause()
            } else {
                MediaPlayerSingleton.mediaPlayer.start()
            }

            setPlayButtonIcon()
        }

        findViewById<Button>(R.id.backButtonSeconds).setOnClickListener {
            seek(-10)
        }

        findViewById<Button>(R.id.forwardButtonSeconds).setOnClickListener {
            seek(10)
        }

        MediaPlayerSingleton.mediaPlayer.setOnCompletionListener {
            MediaPlayerSingleton.setSong(MediaPlayerSingleton.genSong())
            MediaPlayerSingleton.mediaPlayer.start()
            setPlayButtonIcon()
            findViewById<ProgressBar>(R.id.progressBar).setProgress(0, true)
        }

        setProgressBarThread()
    }

    override fun onDestroy() {
        super.onDestroy()
//        MediaPlayerSingleton.mediaPlayer.setOnCompletionListener(null)
    }

    fun setProgressBarThread() {
        if (progressBarThread != null) {
            if (!progressBarThread!!.isCancelled) return
        }

        progressBarThread = lifecycleScope.launch (Dispatchers.IO) {
            while (isActive) {
                if (MediaPlayerSingleton.songNode != null && MediaPlayerSingleton.mediaPlayer.isPlaying) {
                    if (MediaPlayerSingleton.songNode!!.filePath == filePath) {
                        launch (Dispatchers.Main) {
                            setProgressBar()
                        } //TODO when i go outof app
                    }
                }
                delay(500)
            }
        }
    }

    fun setProgressBar() {
        var duration = MediaPlayerSingleton.mediaPlayer.duration.toFloat()
        var curr = MediaPlayerSingleton.mediaPlayer.currentPosition.toFloat()
        var percent = (curr / duration) * 100
        findViewById<ProgressBar>(R.id.progressBar).setProgress(percent.roundToInt(), true)
    }

    fun seek(seconds: Int) {
        if (MediaPlayerSingleton.songNode == null) return
        if (MediaPlayerSingleton.songNode!!.filePath != filePath) return
        MediaPlayerSingleton.mediaPlayer.seekTo(MediaPlayerSingleton.mediaPlayer.currentPosition + seconds * 1000)
        setProgressBar()
    }

    fun setPlayButtonIcon() {
        if (MediaPlayerSingleton.mediaPlayer.isPlaying && MediaPlayerSingleton.getFilePath() == filePath) {
            findViewById<Button>(R.id.playButton).text = pauseIcon
        } else {
            findViewById<Button>(R.id.playButton).text = playIcon
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun evalWidth(initialWidth: Int): Int {
        return screenWidth.toInt()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun evalHeight(initialWidth: Int, initialHeight: Int): Int {

        val ratio: Float = screenWidth / initialWidth.toFloat()

        return (initialHeight * ratio).toInt()
    }
}