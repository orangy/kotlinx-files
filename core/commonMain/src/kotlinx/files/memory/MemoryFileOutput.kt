package kotlinx.files.memory

import kotlinx.files.*
import kotlinx.io.core.*

internal class MemoryFileOutput(override val identity: String, private val file: MemoryCatalogueFile) : AbstractOutput(),
    FileOutput {
    private var builder = BytePacketBuilder()

    override fun closeDestination() {
        flush()

        val packet = builder.build()
        builder = BytePacketBuilder()
        val newBytes = packet.readBytes()
        file.data = file.data + newBytes
        builder.close()
    }

    override fun flush(buffer: IoBuffer) {
        builder.writeFully(buffer)
    }

    override val size: Long
        get() = (file.data.size + builder.size).toLong()

    override val position: Long
        get() = size

    override fun seek(position: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}