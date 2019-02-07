package kotlinx.files

import kotlinx.cinterop.*
import platform.posix.*

private val temporaryDirectory: String = getenv("TMPDIR")?.toKString() ?: P_tmpdir ?: "/tmp"

fun generateTemporaryDirectoryName(prefix: String): String {
    // TODO need something more robust
    srand(clock().toUInt())
    val random = rand()
    return "$temporaryDirectory/$prefix-$random/"
}

