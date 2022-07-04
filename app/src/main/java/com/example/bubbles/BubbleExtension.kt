package com.example.bubbles

import android.view.View
import com.example.bubbles.ViewExtension.Companion.viewRadius
import com.example.bubbles.ViewExtension.Companion.xCenter
import com.example.bubbles.ViewExtension.Companion.yCenter

class BubbleExtension {
    companion object{
        fun Bubble.synchronize(view: View) = synchronize(view.x, view.y)

        fun Bubble.isBubbleCollisionDetected(v1: View, v2: View) = isBubbleCollisionDetected(
            v1.xCenter(), v1.yCenter(), v2.xCenter(), v2.yCenter(), v1.viewRadius(), v2.viewRadius()
        )
    }
}