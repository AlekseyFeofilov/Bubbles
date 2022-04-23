package com.example.Bubbles

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

internal class Bubble(var positionX: Float, var positionY: Float, var speedX: Double) {
    var speedY = (-10..10).random().toDouble()

    fun move() {
        when {
            speedY > 4 -> speedY += (-60..40).random().toDouble() / 100
            speedY < 4 -> speedY += (-40..60).random().toDouble() / 100
        }

        positionX += speedX.toFloat()
        positionY += speedY.toFloat()
    }

    fun collision(forceDirection: Double) {
        speedX += 2 * (cos(forceDirection) * 0.7 * speedX)
        speedY += 2 * (sin(forceDirection) * 0.7 * speedY)
    }
}