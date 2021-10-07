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

package com.github.coolrc136.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.github.coolrc136.R

class FlashBtn : androidx.appcompat.widget.AppCompatImageView {

    init {
        setImageResource(R.drawable.flash_on)
        setBackgroundColor(Color.TRANSPARENT)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        android.R.attr.imageButtonStyle
    )

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )
}
