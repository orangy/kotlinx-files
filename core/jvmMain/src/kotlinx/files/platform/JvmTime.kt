package kotlinx.files.platform

internal actual fun currentTimeMicrosSinceEpoch(): Long = System.currentTimeMillis() * 1000L