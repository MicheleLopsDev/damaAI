package io.github.luposolitario.damaai.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import io.github.luposolitario.damaai.datastore.ModelSettings
import java.io.InputStream
import java.io.OutputStream

object ModelSettingsSerializer : Serializer<ModelSettings> {
    override val defaultValue: ModelSettings = ModelSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ModelSettings {
        try {
            return ModelSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ModelSettings,
        output: OutputStream
    ) = t.writeTo(output)
}
