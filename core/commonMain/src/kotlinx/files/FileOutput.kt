package kotlinx.files

import kotlinx.io.core.*

interface FileOutput : Output {
    val path: Path
    val size: Long
    val position: Long
    fun seek(position: Long)
}