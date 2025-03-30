package com.example.myapplication

import androidx.recyclerview.widget.DiffUtil

class MyDiffUtilClass(
    private val oldList: List<SongItem>,
    private val newList: List<SongItem>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldList[oldItemPosition].name == newList[newItemPosition].name
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return when {
            oldList[oldItemPosition].name != newList[newItemPosition].name -> {
                false
            }
            oldList[oldItemPosition].byteArray != newList[newItemPosition].byteArray -> {
                false
            }
            else -> true
        }
    }
}