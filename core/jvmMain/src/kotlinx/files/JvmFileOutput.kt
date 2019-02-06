package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.pool.*
import java.nio.channels.*

// TODO: Code for channels copied from kotlinx.io, needs to be extensible there because we need to implement FileInput on it
class JvmFileOutput(
    override val path: Path,
    private val channel: SeekableByteChannel,
    pool: ObjectPool<IoBuffer> = IoBuffer.Pool
) : AbstractOutput(pool = pool), FileOutput {

    override val size: Long get() = channel.size()
    override val position: Long get() = channel.position()

    override fun seek(position: Long) {
        flush()
        channel.position(position)
    }

    override fun flush(buffer: IoBuffer) {
        buffer.readDirect { bb ->
            while (bb.hasRemaining())
                channel.write(bb)
        }
    }

    override fun closeDestination() {
        channel.close()
    }
}