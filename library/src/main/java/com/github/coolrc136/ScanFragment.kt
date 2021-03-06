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
import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Size
import android.view.*
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.github.coolrc136.overlay.QRCodeAnalyser
import com.github.coolrc136.overlay.ScanOverlay
import com.github.coolrc136.overlay.isPortraitMode
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


abstract class ScanFragment : Fragment() {

    companion object {
        private const val TAG = "BarcodeScanningActivity"
    }

    /**
     * delay time before send qrcode result to [onResult]
     * @see [onResult]
     */
    var delayTime = 100L

    var showDot = true

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var listener: OverlayListener? = null

    private var scaleX = 0f
    private var scaleY = 0f

    private lateinit var camPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var previewView: PreviewView
    private lateinit var overlay: ScanOverlay
    private var flash: ImageView? = null

    private lateinit var camera: Camera
    private var isFlashOn = false

    private var rotation: Int = Surface.ROTATION_0

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
        flash = view.findViewById(R.id.flash_btn)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        listener = OverlayListener()
        overlay.viewTreeObserver.addOnGlobalLayoutListener(listener)

        camPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    inner class OverlayListener : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                bindScan(cameraProvider, previewView.width, previewView.height)
                if (!camera.cameraInfo.hasFlashUnit()) {
                    flash?.isVisible = false
                }

                flash?.setOnClickListener {
                    it.isClickable = false
                    try {
                        if (isFlashOn) {
                            isFlashOn = false
                            camera.cameraControl.enableTorch(false)
                            flash?.setImageResource(R.drawable.flash_on)
                        } else {
                            isFlashOn = true
                            camera.cameraControl.enableTorch(true)
                            flash?.setImageResource(R.drawable.flash_off)
                        }
                    } finally {
                        it.isClickable = true
                    }
                }
            }, ContextCompat.getMainExecutor(context))
            overlay.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindScan(cameraProvider: ProcessCameraProvider, width: Int, height: Int) {

//        Log.i(TAG, "bindScan: width:$width height:$height")


        val preview: Preview = Preview.Builder()
            .build()

        // ?????????????????? listener ?????????????????????????????????????????????????????????????????????
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                // ???????????????????????????????????????
                val currentZoomRatio: Float = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1F

                // ????????????????????????????????????????????????
                val delta = detector.scaleFactor

                // ??????????????????????????????
                camera.cameraControl.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(context, listener)

        previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }

        //????????????
        preview.setSurfaceProvider(previewView.surfaceProvider)

        //??????????????????
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        //??????????????????
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(width, height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        //????????????????????????
        imageAnalysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            QRCodeAnalyser { barcode, imageWidth, imageHeight ->
                //?????????????????????
                initScale(imageWidth, imageHeight)
                if (showDot)
                    overlay.changeRect(translateRect(barcode.boundingBox))//????????????????????????
                if (barcode.rawValue != null) {

                    lifecycleScope.launch {
                        delay(delayTime)
                        onResult(barcode.rawValue!!)
                    }
                }
            })

        //??????????????????????????????
        cameraProvider.unbindAll()

        //?????????????????????????????????????????????
        camera = cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            imageAnalysis,
            preview
        )
    }

    private fun translateX(x: Float): Float = x * scaleX
    private fun translateY(y: Float): Float = y * scaleY

    //?????????????????????????????????????????????
    private fun translateRect(rect: Rect?): RectF? = if (rect != null) {
        RectF(
            translateX(rect.left.toFloat()),
            translateY(rect.top.toFloat()),
            translateX(rect.right.toFloat()),
            translateY(rect.bottom.toFloat())
        )
    } else {
        null
    }

    private fun initScale(imageWidth: Int, imageHeight: Int) {
        if (isPortraitMode(context)) {
            scaleY = overlay.height.toFloat() / imageWidth.toFloat()
            scaleX = overlay.width.toFloat() / imageHeight.toFloat()
        } else {
            scaleY = overlay.height.toFloat() / imageHeight.toFloat()
            scaleX = overlay.width.toFloat() / imageWidth.toFloat()
        }
    }


    /**
     * called when a qrcode result received, override this function to get scanning result
     */
    abstract fun onResult(code: String)

    /**
     * called when failed to request camera permission, override this function to handle permission denial
     */
    abstract fun onRequestPermissionFailed()

}