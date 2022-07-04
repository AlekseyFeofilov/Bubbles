package com.example.bubbles

import android.view.View
import kotlinx.coroutines.Job
import kotlin.math.*

internal class Bubble(positionX: Float, positionY: Float, speedX: Double, weight: Int) {
    var speedY: Double
        private set
    var positionY: Float
        private set
    var positionX: Float
        private set
    var speedX: Double
        private set
    var weight: Int
        private set
    private var elasticCoefficient = 0.5
    private var job: Job? = null

    init {
        speedY = (-10..4).random().toDouble()

        this.positionX = positionX
        this.positionY = positionY
        this.speedX = speedX
        this.weight = weight
    }

    fun live(job: Job){
        this.job = job
    }

    fun dead(){
        if (job == null) return
        job!!.cancel()
    }

    fun move() {
        speedY = min(speedY + 0.05, 4.0)

        positionX += speedX.toFloat()
        positionY += speedY.toFloat()
    }

    fun collision(collisionBubble: Bubble?) {
        speedX = recalculateSpeedAfterInelasticImpact(
            speedX,
            collisionBubble?.speedX ?: 0.0,
            weight,
            collisionBubble?.weight ?: 1000000,
        )

        speedY = recalculateSpeedAfterInelasticImpact(
            speedY,
            collisionBubble?.speedY ?: 0.0,
            weight,
            collisionBubble?.weight ?: 1000000,
        )
    }

    fun stickTogether(bubble: Bubble) {
        speedX = bubble.speedX
        speedY = bubble.speedY
    }

    fun synchronize (view: View){
        positionX = view.x
        positionY = view.y
    }

    private fun recalculateSpeedAfterInelasticImpact(
        speedTo: Double, speedFrom: Double, weightTo: Int, weightFrom: Int
    ) = elasticCoefficient *
            (weightTo * speedTo + 2 * weightFrom * speedFrom - weightFrom * speedTo) /
            (weightTo + weightFrom)
}