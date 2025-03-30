package com.example.bruh

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

class FontAwesome : androidx.appcompat.widget.AppCompatButton {

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init() {
        // Font name should not contain "/".
        val tf: Typeface = Typeface.createFromAsset(context.assets, "fa-solid-900.ttf")
        typeface = tf
    }
}
