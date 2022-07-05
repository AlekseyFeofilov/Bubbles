package com.example.bubbles

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.Bubbles.R
import com.example.Bubbles.databinding.ActivityMainBinding
import com.example.bubbles.Geometry.Companion.isCollided
import kotlinx.coroutines.*
import kotlin.math.abs
import com.example.bubbles.extensions.BubbleExtensions.Companion.synchronize

private class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var touchParams = object {
        var x = 0f
        var y = 0f
        var job: Job? = null
    }

    private companion object {
        private const val collisionLimit = 1
        private const val minSpeedDifference = 1

        private const val minHorizontalForce = 10
        private const val maxHorizontalForce = 15

        private const val moveDelay: Long = 30
        private const val generateCoefficient = 5
        private const val secondsInMinutes = 1000

        private const val minSecOfLife: Long = 10
        private const val maxSecOfLife: Long = 30
    }

    private var bubbles = mutableMapOf<ImageView, Bubble>()
    private var bubbleSize = Bubble.middleBubbleSize

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layout.setOnTouchListener(createBubble)
        binding.layout.setOnDragListener(bubbleDragListener)

        binding.bigBubbleImageView.setOnClickListener { bubbleSize = Bubble.bigBubbleSize }
        binding.smallBubbleImageView.setOnClickListener { bubbleSize = Bubble.smallBubbleSize }
        binding.mediumBubbleImageView.setOnClickListener { bubbleSize = Bubble.middleBubbleSize }
    }

    private fun setBubbleImageView(imageView: ImageView, x: Float, y: Float) {
        val density = this.resources.displayMetrics.density

        val params = ConstraintLayout.LayoutParams(
            (bubbleSize * density).toInt(),
            (bubbleSize * density).toInt()
        )

        binding.layout.addView(imageView, params)

        imageView.x = (x - params.width / 2)
        imageView.y = (y - params.height / 2)

        imageView.setBackgroundResource(R.drawable.bubble)
    }

    private fun moveBubble(bubble: Bubble, imageView: ImageView) {
        val collisionWith = bubbles.filter {
            it.first != imageView && bubble.isBubbleCollisionDetected(it.first, imageView)
        }

        when {
            bubble.positionX < 0 && bubble.speedX < 0 ||
                    bubble.positionX > binding.layout.width - imageView.width &&
                    bubble.speedX > 0 ||
                    bubble.positionY > binding.layout.height - imageView.height &&
                    bubble.speedY > 0 -> {
                bubble.push()
            }
            collisionWith.isNotEmpty() -> collisionWith.forEach {
                when {
                    collisionWith.size > collisionLimit -> {
                        bubble.dead()
                        binding.layout.removeView(imageView)
                        bubbles.remove(imageView)
                    }
                    abs(bubble.speedX - it.value.speedX) < minSpeedDifference &&
                    abs(bubble.speedY - it.value.speedY) < minSpeedDifference  -> {
                        bubble.stickTogether(it.value)
                    }
                    else -> {
                        bubble.push(it.value)
                        it.value.push(bubble)
                    }
                }
            }
        }
        bubble.move()

        imageView.x = bubble.positionX
        imageView.y = bubble.positionY
    }

    @SuppressLint("ClickableViewAccessibility")
    private val createBubble = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchParams.x = event.x
                touchParams.y = event.y
                val context = this

                touchParams.job = lifecycleScope.launch {
                    while (true) {
                        val imageView = ImageView(context)
                        setBubbleImageView(imageView, touchParams.x, touchParams.y)

                        val speedX =
                            if (imageView.x < binding.layout.width / 2) (minHorizontalForce..maxHorizontalForce).random().toFloat()
                            else (-maxHorizontalForce..-minHorizontalForce).random().toFloat()

                        val bubble = Bubble(imageView.x, imageView.y, speedX, bubbleSize)
                        bubbles[imageView] = bubble

                        lifecycleScope.launch {
                            bubble.live(lifecycleScope.launch {
                                while (true) {
                                    delay(moveDelay)
                                    moveBubble(bubble, imageView)
                                }
                            })

                            delay((minSecOfLife..maxSecOfLife).random() * secondsInMinutes)
                            bubble.dead()
                            binding.layout.removeView(imageView)
                            bubbles.remove(imageView)
                        }

                        imageView.setOnTouchListener(bubbleClickListener)
                        delay((bubbleSize * generateCoefficient).toLong())
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                touchParams.x = event.x
                touchParams.y = event.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchParams.job!!.cancel()
            }
        }
        true
    }

    @SuppressLint("ClickableViewAccessibility")
    private val bubbleClickListener = View.OnTouchListener { it, _ ->
        val shadow = DragShadowBuilder(it, R.drawable.bubble)
        it.startDragAndDrop(null, shadow, it, 0)

        it.visibility = View.INVISIBLE
        true
    }

    private val bubbleDragListener = View.OnDragListener { view, dragEvent ->
        val draggableItem = dragEvent.localState as View

        when (dragEvent.action) {
            DragEvent.ACTION_DRAG_STARTED,
            DragEvent.ACTION_DRAG_LOCATION,
            DragEvent.ACTION_DRAG_EXITED,
            DragEvent.ACTION_DRAG_ENDED -> {
                true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.invalidate()
                true
            }
            DragEvent.ACTION_DROP -> {
                draggableItem.x = dragEvent.x - (draggableItem.width / 2)
                draggableItem.y = dragEvent.y - (draggableItem.height / 2)
                draggableItem.visibility = View.VISIBLE

                val bubble = bubbles[draggableItem]
                bubble?.synchronize(draggableItem)

                true
            }
            else -> {
                false
            }
        }
    }
}