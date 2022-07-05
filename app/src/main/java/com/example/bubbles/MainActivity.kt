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
import com.example.bubbles.extensions.BubbleExtensions.Companion.synchronize

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var touchParams = object {
        var x = 0f
        var y = 0f
        var active = false
    }

    private companion object {
        private const val collisionLimit = 1

        private const val minHorizontalForce = 10
        private const val maxHorizontalForce = 15

        private const val moveDelay: Long = 30
        private const val generateCoefficient = 8
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

    @SuppressLint("ClickableViewAccessibility")
    private fun createBubbleImageView(x: Float, y: Float): ImageView {
        val iv = ImageView(this)
        val density = this.resources.displayMetrics.density
        val params = ConstraintLayout.LayoutParams(
            (bubbleSize * density).toInt(),
            (bubbleSize * density).toInt()
        )

        binding.layout.addView(iv, params)
        iv.x = (x - params.width / 2)
        iv.y = (y - params.height / 2)
        iv.setBackgroundResource(R.drawable.bubble)

        iv.setOnTouchListener(bubbleTouchListener)
        return iv
    }

    private fun getBubbleCollisions(collided: View) =
        bubbles.filter { it.key != collided && isCollided(it.key, collided) }

    private fun isBubbleWallCollided(b: Bubble, iv: ImageView): Boolean {
        return b.isWallCollided(
            0,
            0,
            binding.layout.width - iv.width,
            binding.layout.height - iv.height
        )
    }

    private fun deadBubble(b: Bubble, iv: ImageView) {
        b.dead()
        binding.layout.removeView(iv)
        bubbles.remove(iv)
    }

    private fun recalculateImageViewPosition(iv: ImageView, b: Bubble) {
        iv.x = b.positionX
        iv.y = b.positionY
    }

    private fun moveBubble(b: Bubble, iv: ImageView) {
        if (isBubbleWallCollided(b, iv)) {
            b.push()
        }

        val collisionWith = getBubbleCollisions(iv)

        getBubbleCollisions(iv).forEach {
            if (collisionWith.size > collisionLimit) {
                deadBubble(b, iv)
            }

            b.tryStickTogether(it.value)
        }

        b.move()
        recalculateImageViewPosition(iv, b)
    }

    private fun getBubbleSpeed(posX: Float): Float {
        val speed = (minHorizontalForce..maxHorizontalForce).random().toFloat()
        return if (posX < binding.layout.width / 2) speed else -speed
    }

    private fun createBubble(posX: Float, posY: Float): ImageView {
        val imageView = createBubbleImageView(posX, posY)
        val speedX = getBubbleSpeed(posX)
        val bubble = Bubble(imageView.x, imageView.y, speedX, bubbleSize)

        bubbles[imageView] = bubble
        beginMove(bubbles[imageView]!!, imageView)

        return imageView
    }

    private fun beginMove(b: Bubble, iv: ImageView){
        lifecycleScope.launch {
            b.live(lifecycleScope.launch {
                while (true) {
                    delay(moveDelay)
                    moveBubble(b, iv)
                }
            })

            delay((minSecOfLife..maxSecOfLife).random() * secondsInMinutes)
            deadBubble(b, iv)
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private val createBubble = View.OnTouchListener { _, event ->
        touchParams.x = event.x
        touchParams.y = event.y

        if (event.action == MotionEvent.ACTION_UP ||
            event.action == MotionEvent.ACTION_CANCEL
        ) {
            touchParams.active = false
            return@OnTouchListener true
        }

        if(touchParams.active) return@OnTouchListener true

        touchParams.active = true

        lifecycleScope.launch {
            while (touchParams.active) {
                createBubble(touchParams.x, touchParams.y)
                delay((bubbleSize * generateCoefficient).toLong())
            }
        }

        return@OnTouchListener true
    }

    @SuppressLint("ClickableViewAccessibility")
    private val bubbleTouchListener = View.OnTouchListener { it, _ ->
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

                bubbles[draggableItem]?.synchronize(draggableItem)
                true
            }
            else -> {
                false
            }
        }
    }
}