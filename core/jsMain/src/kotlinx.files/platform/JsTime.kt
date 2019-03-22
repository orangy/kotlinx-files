package kotlinx.files.platform

import kotlin.js.*

internal actual fun currentTimeMicrosSinceEpoch(): Long {
    return Date().getTime().toLong() * 1000
}