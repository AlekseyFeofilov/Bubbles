@file:Suppress("MemberVisibilityCanBePrivate")

package com.example.bubbles

import android.view.View
import kotlin.math.pow
import kotlin.math.sqrt
import com.example.bubbles.extensions.ViewExtensions.Companion.viewRadius
import com.example.bubbles.extensions.ViewExtensions.Companion.xCenter
import com.example.bubbles.extensions.ViewExtensions.Companion.yCenter

class Geometry {
    companion object{
        fun isCollided(v1: View, v2: View) = isCollided(
            v1.xCenter(), v1.yCenter(), v2.xCenter(), v2.yCenter(), v1.viewRadius(), v2.viewRadius()
        )

        fun isCollided(
            centerX1: Float, centerX2: Float,
            centerY1: Float, centerY2: Float,
            radius1: Int, radius2: Int
        ) = distanceBetweenTwoPoints(centerX1, centerX2, centerY1, centerY2) <
                radius1 + radius2

        fun distanceBetweenTwoPoints(x1: Float, x2: Float, y1: Float, y2: Float) =
            sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }
}