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

package com.github.coolrc136

import android.Manifest
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.github.coolrc136.overlay.QRCodeAnalyser
import com.github.coolrc136.overlay.ScanOverlay
import com.github.coolrc136.overlay.isPortraitMode
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors


open class ScanFragment : Fragment() {

    companion object {
        private const val TAG = "BarcodeScanningActivity"
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var listener: OverlayListener? = null

    private var scaleX = 0f

    private var scaleY = 0f

    private lateinit var camPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var previewView: PreviewView
    private lateinit var overlay: ScanOverlay
    var rotation: Int = Surface.ROTATION_0

    open fun getLayoutId(): Int {
        return R.layout.fragment_scan
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        camPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    onRequestPermissionFailed()
                }
            }

        val orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                rotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
            }
        }
        orientationEventListener.enable()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(getLayoutId(), container, false)
        previewView = view.findViewById(R.id.previewView)
        overlay = view.findViewById(R.id.overlay)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        listener = OverlayListener()
        overlay.viewTreeObserver.addOnGlobalLayoutListener(listener)

        camPermissionLauncher.launch(Manifest.permission.CAMERA)

        return view
    }

    inner class OverlayListener : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                bindScan(cameraProvider, previewView.width, previewView.height)
            }, ContextCompat.getMainExecutor(context))
            overlay.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    private fun bindScan(cameraProvider: ProcessCameraProvider, width: Int, height: Int) {

        Log.i(TAG, "bindScan: width:$width height:$height")

        val preview: Preview = Preview.Builder()
            .build()

        //绑定预览
        preview.setSurfaceProvider(previewView.surfaceProvider)

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
                //初始化缩放比例
                initScale(imageWidth, imageHeight)
                barcode.boundingBox?.let {//扫描二维码的外边框矩形
                    overlay.addRect(translateRect(it))
                    Log.i(
                        TAG,
                        "bindScan: left:${it.left} right:${it.right} top:${it.top} bottom:${it.bottom}"
                    )
                }
                if (barcode.rawValue != null)
                    sendScannedCode(barcode.rawValue!!)
            })

        //解绑当前所有相机操作
        cameraProvider.unbindAll()

        //将相机绑定到当前控件的生命周期
        cameraProvider.bindToLifecycle(
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
            scaleY = overlay.height.toFloat() / imageWidth.toFloat()
            scaleX = overlay.width.toFloat() / imageHeight.toFloat()
        } else {
            scaleY = overlay.height.toFloat() / imageHeight.toFloat()
            scaleX = overlay.width.toFloat() / imageWidth.toFloat()
        }
    }


    open fun sendScannedCode(code: String) {
        Log.i("result", code)
    }

    open fun onRequestPermissionFailed() {
        Log.e("result", "Request Camera Permission Failed")
    }

}