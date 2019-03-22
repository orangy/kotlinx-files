package kotlinx.files.platform

import kotlinx.cinterop.*
import platform.posix.*

internal actual fun currentTimeMicrosSinceEpoch(): Long = memScoped {
    val timespec = alloc<timespec>()
    clock_gettime(CLOCK_REALTIME, timespec.ptr)
    timespec.tv_sec * 1_000_000 + timespec.tv_nsec / 1000
}