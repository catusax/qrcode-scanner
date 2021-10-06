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

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import me.coolrc.qrcode.databinding.ScanFragmentBinding
import me.coolrc.qrcode.overlay.QRCodeAnalyser
import me.coolrc.qrcode.overlay.isPortraitMode
import me.coolrc.qrcode.utils.Constraints
import java.util.concurrent.Executors

open class ScanFragment : Fragment() {


    companion object {
        private const val TAG = "BarcodeScanningActivity"
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var listener: OverlayListener? = null

    private var camera: Camera? = null

    private var scaleX = 0f

    private var scaleY = 0f

    private lateinit var binding: ScanFragmentBinding

    private lateinit var camPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        camPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    onRequestPermissionFailed()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScanFragmentBinding.inflate(layoutInflater)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        listener = OverlayListener()
        binding.overlay.viewTreeObserver.addOnGlobalLayoutListener(listener)

        camPermissionLauncher.launch(Manifest.permission.CAMERA)

        return binding.root
    }

    inner class OverlayListener : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                bindScan(cameraProvider, binding.overlay.width, binding.overlay.height)
            }, ContextCompat.getMainExecutor(context))
            binding.overlay.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindScan(cameraProvider: ProcessCameraProvider, width: Int, height: Int) {

        Log.i(TAG, "bindScan: width:$width height:$height")

        val preview: Preview = Preview.Builder()
            .build()

        //绑定预览
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        //使用后置相机
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        //配置图片扫描
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(width, height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        //绑定图片扫描解析
        imageAnalysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            QRCodeAnalyser { barcode, imageWidth, imageHeight ->
                //解绑当前所有相机操作
                cameraProvider.unbindAll()
                //初始化缩放比例
                initScale(imageWidth, imageHeight)
                barcode.boundingBox?.let {//扫描二维码的外边框矩形
                    binding.overlay.addRect(translateRect(it))
                    Log.i(
                        TAG,
                        "bindScan: left:${it.left} right:${it.right} top:${it.top} bottom:${it.bottom}"
                    )
                }
                if (barcode.rawValue != null)
                    sendScannedCode(barcode.rawValue!!)
            })
        //将相机绑定到当前控件的生命周期
        camera = cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            imageAnalysis,
            preview
        )
    }

    private fun translateX(x: Float): Float = x * scaleX
    private fun translateY(y: Float): Float = y * scaleY

    //将扫描的矩形换算为当前屏幕大小
    private fun translateRect(rect: Rect) = RectF(
        translateX(rect.left.toFloat()),
        translateY(rect.top.toFloat()),
        translateX(rect.right.toFloat()),
        translateY(rect.bottom.toFloat())
    )

    private fun initScale(imageWidth: Int, imageHeight: Int) {
        if (isPortraitMode(requireContext())) {
            scaleY = binding.overlay.height.toFloat() / imageWidth.toFloat()
            scaleX = binding.overlay.width.toFloat() / imageHeight.toFloat()
        } else {
            scaleY = binding.overlay.height.toFloat() / imageHeight.toFloat()
            scaleX = binding.overlay.width.toFloat() / imageWidth.toFloat()
        }
    }


    open fun sendScannedCode(code: String) {
        Log.e("result", code)
        setFragmentResult(Constraints.SCAN_RESULT, bundleOf("qrcode" to code))
        findNavController().popBackStack()
    }

    open fun onRequestPermissionFailed() {
        findNavController().popBackStack()
    }

}