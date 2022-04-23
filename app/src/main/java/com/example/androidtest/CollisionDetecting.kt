package com.example.androidtest

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import kotlin.math.max
import kotlin.math.min


internal fun isCollisionDetected(view1: View, x1: Int, y1: Int, view2: View, x2: Int, y2: Int): Boolean {
    var bitmap1: Bitmap? = getViewBitmap(view1)
    var bitmap2: Bitmap? = getViewBitmap(view2)
    require(!(bitmap1 == null || bitmap2 == null)) { "bitmaps cannot be null" }

    val bounds1 = Rect(x1, y1, x1 + bitmap1.width, y1 + bitmap1.height)
    val bounds2 = Rect(x2, y2, x2 + bitmap2.width, y2 + bitmap2.height)

    if (Rect.intersects(bounds1, bounds2)) {
        val collisionBounds: Rect = getCollisionBounds(bounds1, bounds2)

        for (i in collisionBounds.left until collisionBounds.right) {
            for (j in collisionBounds.top until collisionBounds.bottom) {
                val bitmap1Pixel = bitmap1!!.getPixel(i - x1, j - y1)
                val bitmap2Pixel = bitmap2!!.getPixel(i - x2, j - y2)

                if (isFilled(bitmap1Pixel) && isFilled(bitmap2Pixel)) {
                    bitmap1.recycle()
                    bitmap1 = null
                    bitmap2.recycle()
                    bitmap2 = null

                    return true
                }
            }
        }
    }

    bitmap1!!.recycle()
    bitmap1 = null
    bitmap2!!.recycle()
    bitmap2 = null

    return false
}

private fun getViewBitmap(view: View): Bitmap? {
    if (view.measuredHeight <= 0) {
        val specWidth = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(specWidth, specWidth)

        val bitmap = Bitmap.createBitmap(
            view.layoutParams.width,
            view.layoutParams.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)

        return bitmap
    }

    val bitmap = Bitmap.createBitmap(
        view.layoutParams.width,
        view.layoutParams.height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    view.layout(view.left, view.top, view.right, view.bottom)
    view.draw(canvas)

    return bitmap
}

private fun getCollisionBounds(rect1: Rect, rect2: Rect): Rect {
    val left = max(rect1.left, rect2.left)
    val top = max(rect1.top, rect2.top)

    val right = min(rect1.right, rect2.right)
    val bottom = min(rect1.bottom, rect2.bottom)

    return Rect(left, top, right, bottom)
}

private fun isFilled(pixel: Int): Boolean {
    return pixel != Color.TRANSPARENT
}