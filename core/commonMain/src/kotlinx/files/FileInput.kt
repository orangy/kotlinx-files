package kotlinx.files

import kotlinx.io.core.*

/**
 * Represents a file opened for input.
 */
interface FileInput : Input {
    /**
     * An instance of the [String] provided by the file system for diagnostic purposes. 
     */
    val identity: String

    /**
     * Size of the file in bytes.
     */
    val size: Long
    
    /**
     * Current reading position in the file.
     */
    val position: Long

    /**
     * Changes current reading position in the file.
     */
    fun seek(position: Long)
}

