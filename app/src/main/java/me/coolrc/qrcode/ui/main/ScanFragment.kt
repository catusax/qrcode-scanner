package me.coolrc.qrcode.ui.main

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import com.king.zxing.CaptureFragment
import me.coolrc.qrcode.R

class ScanFragment : CaptureFragment() {

    override fun getLayoutId(): Int {
        return R.layout.scan_fragment
    }

    override fun onResultCallback(result: String?): Boolean {
        setFragmentResult("qrcode", bundleOf("qrcode" to result))
        view?.findNavController()?.popBackStack()
        return true
    }

}