package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*

class JsFileInput(override val path: UnixPath, private val fileDescriptor: Int) : AbstractInput(), FileInput {
    private var closed = false

    override val size: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val position: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun seek(position: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun fill(): IoBuffer? {
        val buffer = pool.borrow()
        buffer.reserveEndGap(IoBuffer.ReservedSize)
        try {
            val size = buffer.writeDirect { view ->
                JsFileSystem.fs.readSync(fileDescriptor, view, view.byteOffset, view.byteLength)
            }
            if (size == 0) { // EOF
                buffer.release(pool)
                return null
            }
        } catch (e:dynamic) {
            throw IOException("Failed to read from input stream for $path: $e")
        }

        return buffer
    }

    override fun closeSource() {
        if (closed)
            return
        closed = true
        JsFileSystem.fs.closeSync(fileDescriptor)
    }
}

