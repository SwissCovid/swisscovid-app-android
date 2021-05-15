package ch.admin.bag.dp3t.checkin.generateqrcode

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.utils.getSubtitle
import org.crowdnotifier.android.sdk.model.VenueInfo

private const val PDF_WIDTH = 1240
private const val PDF_HEIGHT = 1748

fun createEntryPdf(venueInfo: VenueInfo, bitmap: Bitmap, context: Context): PdfDocument {
	val w = PDF_WIDTH
	val h = PDF_HEIGHT

	val document = PdfDocument()
	val pageInfo = PdfDocument.PageInfo.Builder(w, h, 1).create() // A4 size
	val page = document.startPage(pageInfo)
	page.canvas.apply {
		drawText(venueInfo.description, w / 2f, 200f, titlePaint)
		drawText(context.getString(venueInfo.getSubtitle()), w / 2f, 250f, subtitlePaint)

		val indicatorOffset = 20f
		val qrCodeY = 400f
		drawLinesAroundQrCode(
			start = (w - bitmap.width) / 2f - indicatorOffset,
			top = qrCodeY - indicatorOffset,
			end = w - (w - bitmap.width) / 2f + indicatorOffset,
			bottom = qrCodeY + bitmap.height + indicatorOffset,
			this,
			ContextCompat.getColor(context, R.color.blue_main)
		)
		drawBitmap(bitmap, (w - bitmap.width) / 2f, qrCodeY, Paint())

		//TODO: draw swisscovid logo and slogan...

	}

	document.finishPage(page)
	return document

}

private val titlePaint = Paint().apply {
	textAlign = Paint.Align.CENTER
	textSize = 50f
	typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
}

private val subtitlePaint = Paint().apply {
	textAlign = Paint.Align.CENTER
	textSize = 30f
}

private fun drawLinesAroundQrCode(start: Float, top: Float, end: Float, bottom: Float, canvas: Canvas, @ColorInt strokeColor: Int) {
	val indicatorWidth = (end - start) / 5

	val linePaint = Paint().apply {
		color = strokeColor
		strokeWidth = 8f
		style = Paint.Style.STROKE
	}
	canvas.apply {
		drawPath(Path().apply {
			moveTo(start, top + indicatorWidth)
			lineTo(start, top)
			lineTo(start + indicatorWidth, top)
			moveTo(end - indicatorWidth, top)
			lineTo(end, top)
			lineTo(end, top + indicatorWidth)
			moveTo(end, bottom - indicatorWidth)
			lineTo(end, bottom)
			lineTo(end - indicatorWidth, bottom)
			moveTo(start + indicatorWidth, bottom)
			lineTo(start, bottom)
			lineTo(start, bottom - indicatorWidth)

		}, linePaint)
	}
}