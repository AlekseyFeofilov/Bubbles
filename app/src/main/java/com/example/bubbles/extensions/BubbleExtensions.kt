package com.example.bubbles.extensions

import android.view.View
import com.example.bubbles.Bubble
import com.example.bubbles.extensions.ViewExtensions.Companion.viewRadius
import com.example.bubbles.extensions.ViewExtensions.Companion.xCenter
import com.example.bubbles.extensions.ViewExtensions.Companion.yCenter

class BubbleExtensions {
    companion object{
        fun Bubble.synchronize(view: View) = synchronize(view.x, view.y)

        fun Bubble.isBubbleCollisionDetected(v1: View, v2: View) = isBubbleCollisionDetected(
            v1.xCenter(), v1.yCenter(), v2.xCenter(), v2.yCenter(), v1.viewRadius(), v2.viewRadius()
        )
    }
}