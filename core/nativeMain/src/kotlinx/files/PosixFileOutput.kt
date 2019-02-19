package kotlinx.files

import kotlinx.cinterop.*
import kotlinx.io.core.*
import kotlinx.io.errors.*
import kotlinx.io.internal.utils.*
import platform.posix.*

class PosixFileOutput(override val identity: String, private val fileDescriptor: Int) : AbstractOutput(), FileOutput {
    private var closed = false
    private var positionValue = 0L

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
        flush()
        positionValue = position
    }


    init {
        check(fileDescriptor >= 0) { "Illegal fileDescriptor: $fileDescriptor" }
        check(kx_internal_is_non_blocking(fileDescriptor) == 0) { "File descriptor is in O_NONBLOCK mode." }
    }

    override fun flush(buffer: IoBuffer) {
        while (buffer.canRead()) {
            if (kotlinx.io.streams.write(fileDescriptor, buffer) <= 0) {
                throw IOException("Failed to write to FileOutput for $identity.", PosixException.forErrno())
            }
        }
    }

    private fun checkClosed() {
        if (closed)
            throw IOException("FileOutput for $identity has already been closed.")
    }

    override fun closeDestination() {
        if (closed) return
        closed = true

        if (close(fileDescriptor) != 0) {
            val error = errno
            if (error != EBADF) { // EBADF is already closed or not opened
                throw IOException("Failed to close FileOutput for $identity.", PosixException.forErrno(error))
            }
        }
    }
}