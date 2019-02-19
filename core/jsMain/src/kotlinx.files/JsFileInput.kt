package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*

class JsFileInput(override val identity: String, private val fileDescriptor: Int) : AbstractInput(), FileInput {
    private var closed = false
    private var positionValue = 0L

    override val size: Long
        get() {
            checkClosed()
            val attributes = JsFileSystem.fs.fstatSync(fileDescriptor)
            return attributes.size
        }

    override val position: Long
        get() {
            checkClosed()
            return positionValue
        }

    override fun seek(position: Long) {
        checkClosed()
        positionValue = position
    }

    override fun fill(): IoBuffer? {
        checkClosed()
        val buffer = pool.borrow()
        buffer.reserveEndGap(IoBuffer.ReservedSize)
        try {
            val size = buffer.writeDirect { view ->
                JsFileSystem.fs.readSync(fileDescriptor, view, view.byteOffset, view.byteLength, positionValue)
            }
            if (size == 0) { // EOF
                buffer.release(pool)
                return null
            }
            positionValue += size
        } catch (e: dynamic) {
            throw IOException("Failed to read from input stream for $identity: $e")
        }

        return buffer
    }

    private fun checkClosed() {
        if (closed)
            throw IOException("FileInput for $identity has already been closed.")

    }

    override fun closeSource() {
        if (closed)
            return
        closed = true
        JsFileSystem.fs.closeSync(fileDescriptor)
    }
}

