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
import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue

/**
 * dp转px
 */
fun Float.toPx(): Int {
    val resources = Resources.getSystem()
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        resources.displayMetrics
    ).toInt()
}


fun isPortraitMode(context: Context): Boolean {
    val mConfiguration: Configuration = context.resources.configuration //获取设置的配置信息
    return mConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT
}