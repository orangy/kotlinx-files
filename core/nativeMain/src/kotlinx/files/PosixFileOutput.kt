package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*
import kotlinx.io.internal.utils.*
import platform.posix.*

class PosixFileOutput(override val path: UnixPath, private val fileDescriptor: Int) : AbstractOutput(), FileOutput {
    override val size: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val position: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun seek(position: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var closed = false

    init {
        check(fileDescriptor >= 0) { "Illegal fileDescriptor: $fileDescriptor" }
        check(kx_internal_is_non_blocking(fileDescriptor) == 0) { "File descriptor is in O_NONBLOCK mode." }
    }

    override fun flush(buffer: IoBuffer) {
        while (buffer.canRead()) {
            if (kotlinx.io.streams.write(fileDescriptor, buffer) <= 0) {
                throw PosixException.forErrno(posixFunctionName = "write()").wrapIO()
            }
        }
    }

    override fun closeDestination() {
        if (closed) return
        closed = true

        if (close(fileDescriptor) != 0) {
            val error = errno
            if (error != EBADF) { // EBADF is already closed or not opened
                throw PosixException.forErrno(error, posixFunctionName = "close()").wrapIO()
            }
        }
    }
}