package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*

class JsFileOutput(override val identity: String, private val fileDescriptor: Int) : AbstractOutput(), FileOutput {
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
        flush()
        positionValue = position
    }

    override fun flush(buffer: IoBuffer) {
        try {
            val size = buffer.readDirect { view ->
                JsFileSystem.fs.writeSync(fileDescriptor, view, view.byteOffset, view.byteLength, positionValue)
            }
            positionValue += size
        } catch (e: dynamic) {
            throw IOException("Failed to write to output stream for $identity: $e")
        }
    }

    private fun checkClosed() {
        if (closed)
            throw IOException("FileOutput for $identity has already been closed.")
    }

    override fun closeDestination() {
        if (closed)
            return
        closed = true
        JsFileSystem.fs.closeSync(fileDescriptor)
    }
}