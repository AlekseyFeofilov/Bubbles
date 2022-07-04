package com.example.bubbles

import android.view.View
import kotlin.math.pow
import kotlin.math.sqrt

internal fun isBubbleCollisionDetected(v1: View, v2: View) =
    distanceBetweenCenters(v1, v2) < viewRadius(v1) + viewRadius(v2)

private fun distanceBetweenCenters (v1: View, v2: View) =
    sqrt((xCenter(v2) - xCenter(v1)).pow(2) + (yCenter(v2) - yCenter(v1)).pow(2))

private fun xCenter(v: View) = v.x + v.width / 2

private fun yCenter(v: View) = v.y + v.height / 2

private fun viewRadius(v: View) = v.width / 2