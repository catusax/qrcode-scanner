/*
 *     qrcode-scanner : simple qrcode scanner and generator for android
 *     Copyright (C) <2021>  <coolrc>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*
    author https://juejin.cn/post/6950448798208376839
 */

package com.github.coolrc136.overlay

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import com.github.coolrc136.R


class ScanOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var animator: ObjectAnimator? = null

    private var bitmap: Bitmap

    private var resultRect: RectF? = null

    private var showLine = true

    private var floatYFraction = 0f
        set(value) {
            field = value
            invalidate()
        }

    init {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
        @ColorInt val colorAccent = typedValue.data

        paint.style = Paint.Style.FILL
        paint.color = colorAccent
        paint.strokeWidth = 3f.toPx().toFloat()
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_scan_line)
        getAnimator().start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (showLine) {
            canvas?.drawBitmap(bitmap, (width - bitmap.width) / 2f, height * floatYFraction, paint)
        }
        resultRect?.let { rect ->
            canvas?.drawCircle(
                rect.left + (rect.right - rect.left) / 2f,
                rect.top + (rect.bottom - rect.top) / 2f,
                10f.toPx().toFloat(),
                paint
            )
        }

    }

    private fun getAnimator(): ObjectAnimator {
        if (animator == null) {
            animator = ObjectAnimator.ofFloat(
                this,
                "floatYFraction",
                0f,
                1f
            )
            animator?.duration = 5000
            animator?.repeatCount = -1 //-1代表无限循环
        }
        return animator!!
    }

    fun addRect(rect: RectF) {
        showLine = false
        resultRect = rect
        getAnimator().cancel()
        invalidate()
    }
}