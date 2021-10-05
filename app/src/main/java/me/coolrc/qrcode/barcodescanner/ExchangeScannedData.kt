package me.coolrc.qrcode.barcodescanner

interface ExchangeScannedData {
    fun sendScannedCode(code: String)
}