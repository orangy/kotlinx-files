package kotlinx.files

import kotlinx.io.core.*

interface FileInput : Input {
    val identity: String
    val size: Long
    val position: Long
    fun seek(position: Long)
}

