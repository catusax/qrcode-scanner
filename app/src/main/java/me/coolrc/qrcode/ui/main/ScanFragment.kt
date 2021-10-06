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

package me.coolrc.qrcode.ui.main

import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.github.coolrc136.ScanFragment
import me.coolrc.qrcode.R
import me.coolrc.qrcode.utils.Constraints

class ScanFragment : ScanFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delayTime = 100L
    }

    override fun getLayoutId(): Int {
        return R.layout.scan_fragment
    }

    override fun onResult(code: String) {
        Log.e("result", code)
        setFragmentResult(Constraints.SCAN_RESULT, bundleOf("qrcode" to code))
        findNavController().popBackStack()
    }

    override fun onRequestPermissionFailed() {
        findNavController().popBackStack()
    }

}