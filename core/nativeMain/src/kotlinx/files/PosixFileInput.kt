package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*
import kotlinx.io.internal.utils.*
import kotlinx.io.streams.*
import platform.posix.*

private const val SZERO: ssize_t = 0

class PosixFileInput(override val identity: String, private val fileDescriptor: Int) : AbstractInput(), FileInput {
    private var closed = false

    init {
        check(fileDescriptor >= 0) { "Illegal fileDescriptor: $fileDescriptor" }
        check(kx_internal_is_non_blocking(fileDescriptor) == 0) { "File descriptor is in O_NONBLOCK mode." }
    }

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

        val size = read(fileDescriptor, buffer)
        if (size == SZERO) { // EOF
            buffer.release(pool)
            return null
        }
        if (size < 0) {
            buffer.release(pool)
            throw PosixException.forErrno(posixFunctionName = "read()").wrapIO()
        }

        return buffer
    }

    override fun closeSource() {
        if (closed) return
        closed = true

        if (close(fileDescriptor) != 0) {
            val error = errno
            if (error != EBADF) { // EBADF is already closed or not opened
                throw PosixException.forErrno(error, "close()").wrapIO()
            }
        }
    }
}

internal fun PosixException.wrapIO(): IOException =
    IOException("I/O operation failed due to posix error code $errno", this)
