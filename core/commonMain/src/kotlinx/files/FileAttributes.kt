package kotlinx.files

/**
 * Basic attributes associated with a file in a file system
 */
open class FileAttributes(
    val isDirectory: Boolean,
    val isFile: Boolean,
    val isSymbolicLink: Boolean,
    val creationTimeUs: Long,
    val lastAccessTimeUs: Long,
    val lastModifiedTimeUs: Long,
    val size: Long
)

