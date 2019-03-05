package kotlinx.files

import kotlinx.io.core.*

/**
 * Represents a file opened for output.
 */
interface FileOutput : Output {
    /**
     * An instance of the [String] provided by the file system for diagnostic purposes.
     */
    val identity: String

    /**
     * Size of the file in bytes.
     */
    val size: Long

    /**
     * Current writing position in the file.
     */
    val position: Long

    /**
     * Changes current writing position in the file.
     */
    fun seek(position: Long)
}
