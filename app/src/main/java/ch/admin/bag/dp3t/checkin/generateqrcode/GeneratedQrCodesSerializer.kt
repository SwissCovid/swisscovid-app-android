package ch.admin.bag.dp3t.checkin.generateqrcode

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import ch.admin.bag.dp3t.checkin.models.GeneratedQrCodesWrapper
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object GeneratedQrCodesSerializer : Serializer<GeneratedQrCodesWrapper> {
	override val defaultValue: GeneratedQrCodesWrapper = GeneratedQrCodesWrapper.getDefaultInstance()

	override suspend fun readFrom(input: InputStream): GeneratedQrCodesWrapper {
		try {
			return GeneratedQrCodesWrapper.parseFrom(input)
		} catch (exception: InvalidProtocolBufferException) {
			throw  CorruptionException("Cannot read proto.", exception)
		}
	}

	override suspend fun writeTo(t: GeneratedQrCodesWrapper, output: OutputStream) = t.writeTo(output)
}
