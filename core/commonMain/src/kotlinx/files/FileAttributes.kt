package kotlinx.files

/**
 * Basic attributes associated with a file in a file system
 */
open class FileAttributes(
    /**
     * Indicates if a file system entity is a directory.
     */
    val isDirectory: Boolean,

    /**
     * Indicates if a file system entity is a regular file.
     */
    val isFile: Boolean,

    /**
     * Indicates if a file system entity is a symbolic link.
     */
    val isSymbolicLink: Boolean,

    /**
     * Creation time for the file measured in microseconds since Epoch.
     */
    val creationTimeUs: Long,

    /**
     * Last access time for  the file measured in microseconds since Epoch.
     */
    val lastAccessTimeUs: Long,

    /**
     * Last modification time for the file measured in microseconds since Epoch.
     */
    val lastModifiedTimeUs: Long,

    /**
     * Size of the file in bytes.
     */
    val size: Long
)

