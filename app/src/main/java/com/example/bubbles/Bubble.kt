package com.example.bubbles

import kotlinx.coroutines.Job

class Bubble(positionX: Float, positionY: Float, speedX: Float, weight: Int) {
    var speedY: Float
        private set
    var speedX: Float
        private set
    var positionY: Float
        private set
    var positionX: Float
        private set

    private var weight: Int
    private var elasticCoefficient = 0.5f
    private var job: Job? = null

    init {
        speedY = (-10..4).random().toFloat()

        this.positionX = positionX
        this.positionY = positionY
        this.speedX = speedX
        this.weight = weight
    }

    fun live(job: Job) {
        this.job = job
    }

    fun dead() {
        job?.cancel()
    }

    fun move() {
        positionX += speedX
        positionY += speedY
    }

    fun push(collisionBubble: Bubble? = null) {
        speedX = recalculateSpeedAfterInelasticImpact(
            speedX, collisionBubble?.speedX, collisionBubble?.weight,
        )

        speedY = recalculateSpeedAfterInelasticImpact(
            speedY, collisionBubble?.speedY, collisionBubble?.weight,
        )
    }

    private fun recalculateSpeedAfterInelasticImpact(
        speedTo: Float,
        speedFrom: Float?,
        weightTo: Int?
    ): Float {
        return recalculateSpeedAfterInelasticImpact(
            speedTo,
            speedFrom ?: 0f,
            weight,
            weightTo ?: 1000000
        )
    }

    private fun recalculateSpeedAfterInelasticImpact(
        speedTo: Float,
        speedFrom: Float,
        weightTo: Int,
        weightFrom: Int
    ): Float {
        return elasticCoefficient *
                (weightTo * speedTo + 2 * weightFrom * speedFrom - weightFrom * speedTo) /
                (weightTo + weightFrom)
    }

    fun stickTogether(bubble: Bubble) = stickTogether(bubble.speedX, bubble.speedY)

    @Suppress("MemberVisibilityCanBePrivate")
    fun stickTogether(speedX: Float, speedY: Float) {
        this.speedX = speedX
        this.speedY = speedY
    }

    fun synchronize(positionX: Float, positionY: Float) {
        this.positionX = positionX
        this.positionY = positionY
    }
}