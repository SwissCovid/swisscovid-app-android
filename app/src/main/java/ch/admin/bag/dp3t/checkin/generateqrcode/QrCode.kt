package ch.admin.bag.dp3t.checkin.generateqrcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import kotlin.math.max

class QrCode private constructor(val value: String, val size: Int) {

	companion object {
		@JvmStatic
		fun create(value: String): QrCode {
			return QrCode(value, encode(value, 0).width)
		}

		@JvmStatic
		private fun encode(value: String, size: Int) = MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.MARGIN to 0))
	}

	/**
	 * Renders a bitmap to the exact provided outsize. A margin might be added if the QR Code does not fit exactly into the provided
	 * outSize.
	 */
	fun renderToBitmap(outSize: Int = 0): Bitmap {
		val bitMatrix = encode(value, outSize)
		val bitmap = Bitmap.createBitmap(bitMatrix.width, bitMatrix.height, Bitmap.Config.RGB_565)
		for (x in 0 until bitMatrix.width) {
			for (y in 0 until bitMatrix.height) {
				bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
			}
		}
		return bitmap
	}


	/**
	 * Renders a bitmap to the closest possible size of maxOutSize but no larger than maxOutSize. No padding is added.
	 */
	fun renderToMaxSizeBitmap(maxOutSize: Int): Bitmap {
		val bitMatrix = encode(value, 0)
		val factor: Int = max(maxOutSize / bitMatrix.width, 1)
		val bitmap = Bitmap.createBitmap(bitMatrix.width * factor, bitMatrix.height * factor, Bitmap.Config.RGB_565)
		for (x in 0 until bitMatrix.width) {
			for (y in 0 until bitMatrix.height) {
				val intArray = IntArray(factor * factor) { if (bitMatrix[x, y]) Color.BLACK else Color.WHITE }
				bitmap.setPixels(intArray, 0, factor, x * factor, y * factor, factor, factor)
			}
		}
		return bitmap
	}

}