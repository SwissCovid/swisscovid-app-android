package ch.admin.bag.dp3t.checkin.checkinflow;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

public class QrCodeAnalyzer implements ImageAnalysis.Analyzer {

	private final static String TAG = QrCodeAnalyzer.class.getCanonicalName();

	private Listener listener;

	public QrCodeAnalyzer(Listener listener) {
		this.listener = listener;
	}

	@Override
	public void analyze(@NonNull ImageProxy image) {

		int width = image.getWidth();
		int height = image.getHeight();
		int ySize = width * height;

		byte[] imageData = new byte[ySize];

		ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();

		int rowStride = image.getPlanes()[0].getRowStride();
		int pos = 0;

		if (rowStride == width) { // likely
			yBuffer.get(imageData, 0, ySize);
			pos += ySize;
		} else {
			int yBufferPos = -rowStride; // not an actual position
			for (; pos < ySize; pos += width) {
				yBufferPos += rowStride;
				yBuffer.position(yBufferPos);
				yBuffer.get(imageData, pos, width);
			}
		}

		PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
				imageData,
				image.getWidth(), image.getHeight(),
				0, 0,
				image.getWidth(), image.getHeight(),
				false
		);

		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

		try {
			Result result = new QRCodeMultiReader().decode(binaryBitmap);
			checkQrCode(result);
		}
		// Catch all kinds of dubious exceptions that zxing throws
		catch (FormatException e) {
			Log.w(TAG, "Caught FormatException");
		} catch (ChecksumException e) {
			Log.w(TAG, "Caught ChecksumException");
		} catch (NotFoundException e) {
			// Do nothing
			listener.noQRCodeFound();
		} finally {
			// Must be called else new images won't be received or camera may stall (depending on back pressure setting)
			image.close();
		}
	}

	private void checkQrCode(Result qrCode) {
		if (qrCode != null) {
			listener.onQRCodeFound(qrCode.getText());
		} else {
			listener.noQRCodeFound();
		}
	}

	public interface Listener {
		void onQRCodeFound(String qrCodeData);

		void noQRCodeFound();

	}

}
