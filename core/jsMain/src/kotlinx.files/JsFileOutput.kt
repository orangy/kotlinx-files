package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*

class JsFileOutput(override val path: UnixPath, private val fileDescriptor: Int) : AbstractOutput(), FileOutput {
    override val size: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val position: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun seek(position: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var closed = false

    override fun flush(buffer: IoBuffer) {
        try {
            buffer.readDirect { view ->
                JsFileSystem.fs.writeSync(fileDescriptor, view)
            }
        } catch (e: dynamic) {
            throw IOException("Failed to write to output stream for $path: $e")
        }
    }

    override fun closeDestination() {
        if (closed)
            return
        closed = true
        JsFileSystem.fs.closeSync(fileDescriptor)
    }
}