package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*
import kotlinx.io.internal.utils.*
import kotlinx.io.streams.*
import platform.posix.*

private const val SZERO: ssize_t = 0

class PosixFileInput(override val identity: String, private val fileDescriptor: Int) : AbstractInput(), FileInput {
    private var closed = false
    private var positionValue = 0L

    init {
        check(fileDescriptor >= 0) { "Illegal fileDescriptor: $fileDescriptor" }
        check(kx_internal_is_non_blocking(fileDescriptor) == 0) { "File descriptor is in O_NONBLOCK mode." }
    }

    override val size: Long
        get()  {
            checkClosed()
            val attributes = readAttributes(fileDescriptor)
            return attributes.sizeBytes
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
        val buffer = pool.borrow()
        buffer.reserveEndGap(IoBuffer.ReservedSize)

        val size = read(fileDescriptor, buffer)
        if (size == SZERO) { // EOF
            buffer.release(pool)
            return null
        }
        if (size < 0) {
            buffer.release(pool)
            throw IOException("Failed to read from FileInput for $identity.", PosixException.forErrno())
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

        if (close(fileDescriptor) != 0) {
            val error = errno
            if (error != EBADF) { // EBADF is already closed or not opened
                throw IOException("Failed to close FileInput for $identity.", PosixException.forErrno(error))
            }
        }
    }
}
