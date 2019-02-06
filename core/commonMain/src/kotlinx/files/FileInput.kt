package kotlinx.files

import kotlinx.io.core.*

interface FileInput : Input {
    val path: Path
    val size: Long
    val position: Long
    fun seek(position: Long)
}

