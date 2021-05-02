package ch.admin.bag.dp3t.checkin.generateqrcode

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import org.crowdnotifier.android.sdk.model.v3.ProtoV3
import java.io.InputStream
import java.io.OutputStream

object QRCodePayloadSerializer : Serializer<ProtoV3.QRCodePayload> {
	override val defaultValue: ProtoV3.QRCodePayload = ProtoV3.QRCodePayload.getDefaultInstance()

	override suspend fun readFrom(input: InputStream): ProtoV3.QRCodePayload {
		try {
			return ProtoV3.QRCodePayload.parseFrom(input)
		} catch (exception: InvalidProtocolBufferException) {
			throw  CorruptionException("Cannot read proto.", exception)
		}
	}

	override suspend fun writeTo(t: ProtoV3.QRCodePayload, output: OutputStream) = t.writeTo(output)
}

val Context.qrCodePayloadDataStore: DataStore<ProtoV3.QRCodePayload> by dataStore(
	fileName = "qrCodePayloads.pb",
	serializer = QRCodePayloadSerializer
)