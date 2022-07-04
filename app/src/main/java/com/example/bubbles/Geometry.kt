package com.example.bubbles

import kotlin.math.pow
import kotlin.math.sqrt

class Geometry {
    companion object{
        fun distanceBetweenTwoPoints(x1: Float, x2: Float, y1: Float, y2: Float) =
            sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }
}