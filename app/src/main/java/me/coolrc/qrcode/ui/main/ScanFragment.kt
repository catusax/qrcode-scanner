package me.coolrc.qrcode.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.mlkit.common.MlKitException
import me.coolrc.qrcode.barcodescanner.BarcodeScannerProcessor
import me.coolrc.qrcode.barcodescanner.ExchangeScannedData
import me.coolrc.qrcode.barcodescanner.VisionImageProcessor
import me.coolrc.qrcode.databinding.ScanFragmentBinding

class ScanFragment : Fragment(), ExchangeScannedData {

    private lateinit var binding: ScanFragmentBinding

    private lateinit var cameraProvider: ProcessCameraProvider
    private val previewUseCase = Preview.Builder().build()

    private val analysisUseCase: ImageAnalysis =
        ImageAnalysis.Builder().setTargetResolution(Size(1920, 1080))
            .build()

    private val lensFacing = CameraSelector.LENS_FACING_BACK
    private val cameraSelector: CameraSelector =
        CameraSelector.Builder().requireLensFacing(lensFacing).build()


    private lateinit var imageProcessor: VisionImageProcessor

    private var needUpdateGraphicOverlayImageSourceInfo = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cameraProvider =
            ProcessCameraProvider.getInstance(context).get()
        imageProcessor = BarcodeScannerProcessor(context, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScanFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindAllCameraUseCases()
    }


    override fun sendScannedCode(code: String) {
        Log.e("result", code)
        setFragmentResult("qrcode", bundleOf("qrcode" to code))
        findNavController().popBackStack()
    }


    private fun bindAllCameraUseCases() {
        bindPreviewUseCase()
        bindAnalysisUseCase()
    }

    private fun bindPreviewUseCase() {
        previewUseCase.setSurfaceProvider(binding.previewView.surfaceProvider)
        cameraProvider.bindToLifecycle(
            viewLifecycleOwner,
            cameraSelector, previewUseCase
        )
    }

    private fun bindAnalysisUseCase() {
        needUpdateGraphicOverlayImageSourceInfo = true
        analysisUseCase.setAnalyzer( // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(context),
            { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped =
                        lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        binding.graphicOverlay.setImageSourceInfo(
                            imageProxy.width, imageProxy.height, isImageFlipped
                        )
                    } else {
                        binding.graphicOverlay.setImageSourceInfo(
                            imageProxy.height, imageProxy.width, isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    imageProcessor.processImageProxy(imageProxy, binding.graphicOverlay)
                } catch (e: MlKitException) {
                    Toast.makeText(
                        context,
                        e.localizedMessage,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
        cameraProvider.bindToLifecycle(
            viewLifecycleOwner,
            cameraSelector, analysisUseCase, previewUseCase
        )
    }
}