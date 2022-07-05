package com.example.bubbles

import kotlinx.coroutines.Job
import kotlin.math.abs

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
    private var job: Job? = null

    companion object {
        private const val maxStartSpeedY = -10
        private const val minStartSpeedY = 4
        private const val infinityWeight = 1000000
        private var elasticCoefficient = 0.5f
        private const val minSpeedDifference = 1

        const val smallBubbleSize = 50
        const val middleBubbleSize = 100
        const val bigBubbleSize = 150
    }

    init {
        speedY = (maxStartSpeedY..minStartSpeedY).random().toFloat()

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

    fun isWallCollided(start: Int, top: Int, end: Int, bottom: Int): Boolean {
        return positionX < start && speedX < 0 ||
                positionX > end && speedX > 0 ||
                positionY > bottom && speedY > 0 ||
                positionY > top && speedY < 0
    }

    /**
     * return true if bubbles stick together and false else
     */
    fun tryStickTogether(bubble: Bubble) =
        if (abs(bubble.speedX - speedX) < minSpeedDifference &&
            abs(bubble.speedY - speedY) < minSpeedDifference
        ) {
            stickTogether(bubble)
            true
        } else {
            push(bubble)
            bubble.push(this)
            false
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
            weightTo ?: infinityWeight
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

    private fun stickTogether(bubble: Bubble) = stickTogether(bubble.speedX, bubble.speedY)

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