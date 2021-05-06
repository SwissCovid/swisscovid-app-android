package ch.admin.bag.dp3t.checkin.generateqrcode

import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.util.Log
import java.io.*

class PdfPrintDocumentAdapter(private val file: File) : PrintDocumentAdapter() {

	override fun onLayout(
		oldAttributes: PrintAttributes?,
		newAttributes: PrintAttributes?,
		cancellationSignal: CancellationSignal,
		callback: LayoutResultCallback,
		extras: Bundle?
	) {
		if (cancellationSignal.isCanceled) {
			callback.onLayoutCancelled()
			return
		}

		val info = PrintDocumentInfo.Builder("swisscovid-qr-code.pdf")
			.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
			.setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
			.build()

		callback.onLayoutFinished(info, oldAttributes != newAttributes)
	}

	override fun onWrite(
		pages: Array<out PageRange>,
		destination: ParcelFileDescriptor,
		cancellationSignal: CancellationSignal,
		callback: WriteResultCallback
	) {
		var inputStream: InputStream? = null
		var outputStream: OutputStream? = null

		try {
			inputStream = FileInputStream(file)
			outputStream = FileOutputStream(destination.fileDescriptor)

			inputStream.copyTo(outputStream)

			if (cancellationSignal.isCanceled) {
				callback.onWriteCancelled()
			} else {
				callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
			}
		} catch (ex: Exception) {
			callback.onWriteFailed(ex.message)
			Log.e("PDFDocumentAdapter", "Could not write: ${ex.localizedMessage}")
		} finally {
			inputStream?.close()
			outputStream?.close()
		}
	}
}