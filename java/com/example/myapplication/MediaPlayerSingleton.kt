package com.example.myapplication

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.widget.ProgressBar
import java.io.File

class Node (
    var next: Node? = null,
    var prev: Node? = null,
    var filePath: String = ""
)

object MediaPlayerSingleton {
    val mediaPlayer: MediaPlayer = MediaPlayer()

    var songNode: Node? = null

    var songsList: MutableList<String> = mutableListOf()
    var genSongsList: MutableList<String> = mutableListOf()


    fun getSongByteArray(filePath: String) : ByteArray {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(filePath)
        return mmr.embeddedPicture!!
    }

    fun getSongData(context: Context, songFilePath: String) : Array<String> {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(context, Uri.fromFile(File(songFilePath)))

        return arrayOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).toString(),
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).toString())
    }

    private fun prepareSong(filePath: String) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(filePath)
        mediaPlayer.prepare()
    }

    fun setSong(filePath: String) {
        prepareSong(filePath)

        val nextNode = Node()
        nextNode.filePath = filePath
        if (songNode != null) {
            nextNode.prev = songNode
            songNode!!.next = nextNode
        }
        songNode = nextNode

        genSongsList.remove(filePath)

        if (genSongsList.isEmpty()) genSongsList = songsList
    }

    fun setNextSong() {
        val isPlaying = mediaPlayer.isPlaying
        if (songNode == null) return
        if (songNode!!.next == null) {
            setSong(genSong())
        } else {
            songNode = songNode!!.next
            prepareSong(songNode!!.filePath)
        }
        if (isPlaying) mediaPlayer.start()
    }

    fun setPrevSong() {
        val isPlaying = mediaPlayer.isPlaying
        if (songNode == null) return
        if (songNode!!.prev == null) return
        songNode = songNode!!.prev

        prepareSong(songNode!!.filePath)

        if (isPlaying) mediaPlayer.start()
    }

    fun genSong() : String {
        return genSongsList.random()
    }

    fun getFilePath() : String {
        if (songNode == null) return ""
        else return songNode!!.filePath
    }
}