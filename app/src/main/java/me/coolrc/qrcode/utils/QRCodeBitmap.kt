package me.coolrc.qrcode.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.king.zxing.util.LogUtils
import java.util.*

object QRCodeBitmap {

    // 以最高容错级别生成二维码bitmap
    fun createQRCode(content: String?, heightPix: Int): Bitmap? {

        //配置参数
        val hints: MutableMap<EncodeHintType, Any> =
            EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "utf-8"
        //容错级别
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        //设置空白边距的宽度
        hints[EncodeHintType.MARGIN] = 1 //default is 1

        val codeColor = Color.BLACK
        try {

            // 图像数据转换，使用了矩阵转换
            val bitMatrix =
                QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, heightPix, heightPix, hints)
            val pixels = IntArray(heightPix * heightPix)
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (y in 0 until heightPix) {
                for (x in 0 until heightPix) {
                    if (bitMatrix[x, y]) {
                        pixels[y * heightPix + x] = codeColor
                    } else {
                        pixels[y * heightPix + x] = Color.WHITE
                    }
                }
            }

            // 生成二维码图片的格式
            val bitmap = Bitmap.createBitmap(heightPix, heightPix, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, heightPix, 0, 0, heightPix, heightPix)
            return bitmap
        } catch (e: WriterException) {
            LogUtils.w(e.message)
        }
        return null
    }

}