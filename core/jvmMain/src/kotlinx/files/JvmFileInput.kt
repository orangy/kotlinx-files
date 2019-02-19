package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.pool.*
import java.nio.*
import java.nio.channels.*

// TODO: Code for channels copied from kotlinx.io, needs to be extensible there because we need to implement FileInput on it
class JvmFileInput(
    override val identity: String,
    private val channel: SeekableByteChannel,
    pool: ObjectPool<IoBuffer> = IoBuffer.Pool
) : AbstractInput(pool = pool), FileInput {

    override val size: Long get() = channel.size()
    override val position: Long get() = channel.position()

    override fun seek(position: Long) {
        channel.position(position)
        discard()
    }

    override fun fill(): IoBuffer? {
        val buffer: IoBuffer = pool.borrow()
        buffer.reserveEndGap(IoBuffer.ReservedSize)

        try {
            var rc = -1

            buffer.writeDirect(1) { bb: ByteBuffer ->
                rc = channel.read(bb)
            }

            if (rc == -1) {
                buffer.release(pool)
                return null
            }

            return buffer
        } catch (t: Throwable) {
            buffer.release(pool)
            throw t
        }
    }

    override fun closeSource() {
        channel.close()
    }
}