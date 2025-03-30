package com.example.myapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

object bruh {
    var locsToChange: MutableList<String> = mutableListOf()
}

class MainActivity : AppCompatActivity() {
    var filesList: Array<File>? = null
    var songsList: MutableList<SongItem> = mutableListOf()

    val pauseIcon: String = "\uf28b"
    val playIcon: String = "\uf144"

    var progressBarThread: Job? = null

    lateinit var fileView: RecyclerView

    lateinit var fileAdapter: RVAdapter

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPlayButtonIcon()
        setSongText()

        val nestedPaths = "/Music/Spotify"
        val albumPath = Environment.getExternalStorageDirectory().toString() + nestedPaths

        if (!Environment.isExternalStorageManager()) {
            val storagePermissionResultLauncher = ActivityResultCallback<ActivityResult> {
                if (Environment.isExternalStorageManager()) { // Check again incase not accepted
                    filesList = File(albumPath).listFiles()

                    loadFiles()
                }
            }.let {
                registerForActivityResult(
                    ActivityResultContracts.StartActivityForResult(),
                    it
                )
            }

            storagePermissionResultLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    "package:$packageName".toUri()
                )
            )
        } else {
            filesList = File(albumPath).listFiles()

            loadFiles()
        }

        findViewById<EditText>(R.id.searchBar).addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                val viewableList: List<SongItem> = songsList.filter { it.name!!.lowercase().contains(s.toString().lowercase()) }
                fileAdapter.setData(viewableList)
            }

        })

        findViewById<Button>(R.id.playButton).setOnClickListener {
            if (MediaPlayerSingleton.mediaPlayer.isPlaying) {
                MediaPlayerSingleton.mediaPlayer.pause()
            } else {
                MediaPlayerSingleton.mediaPlayer.start()
            }

            if (MediaPlayerSingleton.songNode == null) {
                MediaPlayerSingleton.setSong(MediaPlayerSingleton.genSong())
                MediaPlayerSingleton.mediaPlayer.start()
                setSongText()
            }

            setPlayButtonIcon()
        }

        findViewById<Button>(R.id.backButtonSeconds).setOnClickListener {
            MediaPlayerSingleton.setPrevSong()
            setPlayButtonIcon()
            setSongText()
            findViewById<ProgressBar>(R.id.progressBar2).setProgress(0, true)
        }

        findViewById<Button>(R.id.forwardButtonSeconds).setOnClickListener {
            MediaPlayerSingleton.setNextSong()
            setPlayButtonIcon()
            setSongText()
            findViewById<ProgressBar>(R.id.progressBar2).setProgress(0, true)
        }

        findViewById<TextView>(R.id.playingSongTitle).setOnClickListener {
            if (MediaPlayerSingleton.songNode != null) {
                progressBarThread!!.cancel()
                startActivity(Intent(this, SongViewActivity::class.java).apply {
                    putExtra("filePath", MediaPlayerSingleton.songNode!!.filePath)
                })
            }
        }

        MediaPlayerSingleton.mediaPlayer.setOnCompletionListener {
            /* Ok so this activates even if there is no song playing becuz if there is no song
            * playing then it assumes that the song has been completed and selects next song
            * imma leave this here only cuz why not lmao its a gud feature cuz it starts a song
            * automatically for u without u needing to choose for it to do that :)
            * */

//            MediaPlayerSingleton.setSong(MediaPlayerSingleton.genSong())
//            MediaPlayerSingleton.mediaPlayer.start()
//            findViewById<ProgressBar>(R.id.progressBar2).setProgress(0, true)
//            setPlayButtonIcon()
//            setSongText()

            mediaPlayerCompletion()
        }

        setProgressBarThread()
    }

    override fun onResume() {
        super.onResume()
        setPlayButtonIcon()
        setSongText()
        setProgressBarThread()

        MediaPlayerSingleton.mediaPlayer.setOnCompletionListener {
            mediaPlayerCompletion()
        }
    }

    fun mediaPlayerCompletion() {
        MediaPlayerSingleton.setNextSong()
        findViewById<ProgressBar>(R.id.progressBar2).setProgress(0, true)
        MediaPlayerSingleton.mediaPlayer.start()
        setPlayButtonIcon()
        setSongText()
    }

    fun setProgressBarThread() {
        if (progressBarThread != null) {
            if (!progressBarThread!!.isCancelled) return
        }

        progressBarThread = lifecycleScope.launch (Dispatchers.IO) {
            while (isActive) {
                if (MediaPlayerSingleton.songNode != null && MediaPlayerSingleton.mediaPlayer.isPlaying) {
                    launch (Dispatchers.Main) {
                        setProgressBar()
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
        findViewById<ProgressBar>(R.id.progressBar2).setProgress(percent.roundToInt(), true)
    }

    fun loadFiles() {
        if (filesList == null) {
            return
            // TODO
        }

        displayFiles()
    }

    fun displayFiles() {
        fileView = findViewById<RecyclerView>(R.id.recyclerView)
        var linearLayoutManager = LinearLayoutManager(this)
        fileView.layoutManager = linearLayoutManager

        // TODO Dont do this if no mp3s or wavs
        MediaPlayerSingleton.songsList.clear()

        for (file in filesList!!) {
            songsList.add(SongItem(file))
            MediaPlayerSingleton.songsList.add(file.path)
        }

        MediaPlayerSingleton.genSongsList = MediaPlayerSingleton.songsList

        songsList.sortBy { it.name!!.lowercase() }

        fileAdapter = RVAdapter(songsList) { filePath ->
            progressBarThread!!.cancel()
            startActivity(Intent(this, SongViewActivity::class.java).apply {
                putExtra("filePath", filePath)
            })
        }

        fileView.adapter = fileAdapter


        lifecycleScope.launch(Dispatchers.IO) {
            for (song in songsList) {
                song.setByteArray()

                var loc = fileAdapter.songsList.indexOf(song)
                if (loc != -1) {
                    launch (Dispatchers.Main) {
                        fileAdapter.notifyItemChanged(loc)
                    }
                }
            }
        }

    }

    fun setPlayButtonIcon() {
        if (MediaPlayerSingleton.mediaPlayer.isPlaying) {
            findViewById<Button>(R.id.playButton).text = pauseIcon
        } else {
            findViewById<Button>(R.id.playButton).text = playIcon
        }
    }

    fun setSongText() {
        if (MediaPlayerSingleton.songNode != null) {
            findViewById<TextView>(R.id.playingSongTitle).text = MediaPlayerSingleton.getSongData(
                this, MediaPlayerSingleton.songNode!!.filePath)[0]
        }
    }

}