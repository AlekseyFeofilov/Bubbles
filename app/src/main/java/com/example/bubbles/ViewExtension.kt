package com.example.bubbles

import android.view.View

class ViewExtension {
    companion object{
        fun View.xCenter() = this.x + this.width / 2

        fun View.yCenter() = this.y + this.height / 2

        fun View.viewRadius() = this.width / 2
    }
}