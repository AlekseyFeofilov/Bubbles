package com.example.bubbles.extensions

import android.view.View
import com.example.bubbles.Bubble

class BubbleExtensions {
    companion object{
        fun Bubble.synchronize(view: View) = synchronize(view.x, view.y)
    }
}