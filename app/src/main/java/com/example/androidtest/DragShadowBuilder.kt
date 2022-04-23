package com.example.androidtest

import android.graphics.Canvas
import android.graphics.Point
import android.view.View
import androidx.core.content.res.ResourcesCompat

internal class DragShadowBuilder(view: View, imageID: Int) : View.DragShadowBuilder(view){
    private val shadow =
        ResourcesCompat.getDrawable(view.context.resources, imageID, view.context.theme)

    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        val width = view.width
        val height = view.height

        shadow?.setBounds(0, 0, width, height)

        size.set(width, height)
        touch.set(width / 2, height / 2)
    }

    override fun onDrawShadow(canvas: Canvas) {
        shadow?.draw(canvas)
    }
}