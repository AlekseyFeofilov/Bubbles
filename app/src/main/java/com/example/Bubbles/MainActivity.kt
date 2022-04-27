package com.example.Bubbles

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
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var touchParams = object {
        var x = 0f
        var y = 0f
        var job: Job? = null
    }

    private var bubbles = mutableListOf<Pair<ImageView, Bubble>>()
    private var bubbleSize = 100

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layout.setOnTouchListener(createBubble)
        binding.layout.setOnDragListener(bubbleDragListener)

        binding.bigBubbleImageView.setOnClickListener { bubbleSize = 150 }
        binding.smallBubbleImageView.setOnClickListener { bubbleSize = 50 }
        binding.mediumBubbleImageView.setOnClickListener { bubbleSize = 100 }
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
            it.first != imageView && isBubbleCollisionDetected(it.first, imageView)
        }

        when {
            bubble.positionX < 0 && bubble.speedX < 0 ||
                    bubble.positionX > binding.layout.width - imageView.width &&
                    bubble.speedX > 0 ||
                    bubble.positionY > binding.layout.height - imageView.height &&
                    bubble.speedY > 0 -> {
                bubble.collision(null)
            }
            collisionWith.isNotEmpty() -> collisionWith.forEach {
                when {
                    collisionWith.size > 1 -> {
                        bubble.dead()
                        binding.layout.removeView(imageView)
                        bubbles.remove(bubbles.find { it.second == bubble })
                    }
                    abs(bubble.speedX - it.second.speedX) < 1 &&
                    abs(bubble.speedY - it.second.speedY) < 1  -> {
                        bubble.stickTogether(it.second)
                    }
                    else -> {
                        bubble.collision(it.second)
                        it.second.collision(bubble)
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
                            if (imageView.x < binding.layout.width / 2) (10..15).random().toDouble()
                            else (-15..-10).random().toDouble()

                        val bubble = Bubble(imageView.x, imageView.y, speedX, bubbleSize)
                        bubbles.add(Pair(imageView, bubble))

                        lifecycleScope.launch {
                            bubble.live(lifecycleScope.launch {
                                while (true) {
                                    delay(30)
                                    moveBubble(bubble, imageView)
                                }
                            })

                            delay((10..30).random().toLong() * 1000)
                            bubble.dead()
                            binding.layout.removeView(imageView)
                            bubbles.remove(bubbles.find { it.second == bubble })
                        }

                        imageView.setOnTouchListener(bubbleClickListener)
                        delay((bubbleSize * 5).toLong())
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

                val bubble = bubbles.find { it.first == draggableItem }!!.second
                bubble.synchronize(draggableItem)

                true
            }
            else -> {
                false
            }
        }
    }
}