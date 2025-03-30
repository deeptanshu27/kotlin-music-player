package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil3.Image
import coil3.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RVAdapter(var songsList: List<SongItem>, private val listener: (String) -> Unit) : RecyclerView.Adapter<RVAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return songsList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.name.text = songsList[position].name!!.removeSuffix(".mp3")
        val byteArray = songsList[position].byteArray

        if (byteArray == null) {
            bruh.locsToChange.add(songsList[position].name!!)
        }

        holder.image.load(byteArray)

        val filePath = songsList[position].filePath.toString()

        holder.itemView.setOnClickListener {
            listener(filePath)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById<TextView>(R.id.songName)
        val image: ImageView = itemView.findViewById<ImageView>(R.id.songImage)
    }

    fun setData(newList: List<SongItem>) {
        val diffUtil = MyDiffUtilClass(songsList, newList)
        val diffResults = DiffUtil.calculateDiff(diffUtil)
        songsList = newList
        diffResults.dispatchUpdatesTo(this)
    }

}