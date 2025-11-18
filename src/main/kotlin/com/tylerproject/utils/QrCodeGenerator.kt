package com.tylerproject.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO

object QrCodeGenerator {

    fun generateQrCodeBase64(text: String, size: Int = 300): String {
        try {
            val hints =
                    mapOf(
                            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
                            EncodeHintType.MARGIN to 1
                    )

            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints)

            val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    image.setRGB(x, y, if (bitMatrix[x, y]) Color.BLACK.rgb else Color.WHITE.rgb)
                }
            }

            val outputStream = ByteArrayOutputStream()
            ImageIO.write(image, "PNG", outputStream)
            val imageBytes = outputStream.toByteArray()

            val base64String = Base64.getEncoder().encodeToString(imageBytes)
            return "data:image/png;base64,$base64String"
        } catch (e: Exception) {
            throw RuntimeException("Erro ao gerar QR Code: ${e.message}", e)
        }
    }
}
