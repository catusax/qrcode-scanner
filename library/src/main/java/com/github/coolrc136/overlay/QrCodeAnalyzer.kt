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

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QRCodeAnalyser(private val listener: (Barcode, Int, Int) -> Unit) : ImageAnalysis.Analyzer {

    companion object {
        const val TAG = "BarcodeScanningActivity"
    }

    //配置当前扫码格式
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC
        )
        .build()

    //获取解析器
    private val detector = BarcodeScanning.getClient(options)

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: kotlin.run {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)
            .addOnSuccessListener { barCodes ->
                if (barCodes.size > 0) {
                    listener.invoke(barCodes[0], imageProxy.width, imageProxy.height)
                    //接收到结果后，就关闭解析
//                    detector.close()
                }
            }
            .addOnFailureListener { Log.e(TAG, "Error: ${it.message}") }
            .addOnCompleteListener { imageProxy.close() }

    }

}