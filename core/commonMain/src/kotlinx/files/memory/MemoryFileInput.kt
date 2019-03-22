package kotlinx.files.memory

import kotlinx.files.*
import kotlinx.io.core.*

internal class MemoryFileInput(override val identity: String, private val file: MemoryCatalogueFile) : AbstractInput(),
    FileInput {
    private var readPacket = ByteReadPacket(file.data)

    override val size: Long = readPacket.remaining
    override val position: Long get() = size - readPacket.remaining

    override fun fill(): IoBuffer? {
        if (readPacket.endOfInput)
            return null

        val buffer = pool.borrow()
        buffer.reserveEndGap(IoBuffer.ReservedSize)
        readPacket.readAvailable(buffer)
        return buffer
    }

    override fun closeSource() {
        readPacket.close()
    }

    override fun seek(position: Long) {
        readPacket = ByteReadPacket(file.data, position.toInt())
    }
}