package me.coolrc.qrcode.barcodescanner

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * Barcode Detector.
 */
class BarcodeScannerProcessor(context: Context?, exchangeScannedData: ExchangeScannedData) :
    VisionProcessorBase<List<Barcode>>(context) {
    private val barcodeScanner: BarcodeScanner
    private val exchangeScannedData: ExchangeScannedData
    override fun stop() {
        super.stop()
        barcodeScanner.close()
    }

    override fun detectInImage(image: InputImage): Task<List<Barcode>> {
        return barcodeScanner.process(image)
    }

    override fun onSuccess(
        results: List<Barcode>, graphicOverlay: GraphicOverlay
    ) {
        if (results.isNullOrEmpty()) {
            Log.v(MANUAL_TESTING_LOG, "No barcode has been detected")
        }
        for (i in results.indices) {
            val barcode = results[i]
            graphicOverlay.add(BarcodeGraphic(graphicOverlay, barcode))
            if (barcode.rawValue != null && !barcode.rawValue.isNullOrEmpty()) {
                exchangeScannedData.sendScannedCode(barcode.rawValue!!)
            }
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Barcode detection failed $e")
    }

    companion object {
        private const val TAG = "BarcodeProcessor"
        private const val MANUAL_TESTING_LOG = "BarcodeProcessor_LOG"
    }

    init {

        // Comment this code if you want to allow open Barcode format.
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_CODE_39)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
        this.exchangeScannedData = exchangeScannedData
    }
}