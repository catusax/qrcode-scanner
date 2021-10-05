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

package me.coolrc.qrcode.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View


class ScanOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var resultRect: RectF? = null

    init {
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 3f.toPx().toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        resultRect?.let { rect ->
            canvas?.drawCircle(
                rect.left + (rect.right - rect.left) / 2f,
                rect.top + (rect.bottom - rect.top) / 2f,
                10f.toPx().toFloat(),
                paint
            )
        }

    }

    fun addRect(rect: RectF) {
        resultRect = rect
        invalidate()
    }

}