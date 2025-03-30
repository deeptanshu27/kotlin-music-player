package com.example.myapplication

import java.io.File

class SongItem {
    var name: String? = null
    var byteArray: ByteArray? = null
    var filePath: String? = null

    constructor(file: File) {
        this.name = file.name
        this.filePath = file.path
    }

    fun setByteArray() {
        this.byteArray = MediaPlayerSingleton.getSongByteArray(filePath!!)
    }
}