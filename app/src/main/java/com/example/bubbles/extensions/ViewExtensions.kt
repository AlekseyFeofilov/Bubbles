package com.example.bubbles.extensions

import android.view.View

class ViewExtensions {
    companion object{
        fun View.xCenter() = this.x + this.width / 2

        fun View.yCenter() = this.y + this.height / 2

        fun View.viewRadius() = this.width / 2
    }
}