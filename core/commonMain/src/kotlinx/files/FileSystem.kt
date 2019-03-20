package kotlinx.files

import kotlinx.io.errors.*

/**
 * Represents a system for reading, writing, enumerating, creating and deleting files.
 *
 * Files are binary objects that can be identified by a [String], usually exposing hierarchy where each level is 
 * separated by a [pathSeparator]. Hierarchical behavior of such text identifies are captured in the [Path] interface. 
 * 
 * There is a [FileSystems.Default] instance that represents a file system for the platform of the current process.
 * 
 * A file system implementation should be thread-safe.
 */
interface FileSystem {
    /**
     * Creates an instance of the [Path] interface for this file system.
     * @param base a base directory 
     * @param children zero or more names of the child directories or a file
     */
    fun path(base: String, vararg children: String): Path

    /**
     * Checks if the given [path] represents an existing file or directory.
     */
    fun exists(path: Path): Boolean

    /**
     * Opens a file at the given [path] for input. Caller is responsible for closing the returned [FileInput].
     * @return instance of [FileInput]
     * @throws IOException if the operation cannot be completed.
     */
    fun openInput(path: Path): FileInput

    /**
     * Opens a directory at the given [path] for enumeration. Caller is responsible for closing the returned [Directory].
     * @return instance of [Directory]
     * @throws IOException if the operation cannot be completed.
     */
    fun openDirectory(path: Path): Directory

    /**
     * Checks if this instance of a file system is read-only.
     * 
     * Note, that it is not necessary represents if an underlying physical file system is not writeable. It returns a 
     * value indicating if this instance will throw an exception on an attempt to perform a write operation through it.
     */
    val isReadOnly : Boolean

    /**
     * A single character used to separate hierarchical elements of the path in this file system.
     */
    val pathSeparator: String

    /**
     * Opens a file at the given [path] for output. Caller is responsible for closing the returned [FileInput].
     * @return instance of [FileOutput]
     * @throws IOException if the operation cannot be completed.
     */
    fun openOutput(path: Path): FileOutput

    /**
     * Creates a new empty file at the given [path] if it doesn't exist.
     * @return instance of a [Path] used to create a file
     * @throws IOException if the file already exists or there are not enough permissions.
     */
    fun createFile(path: Path): Path

    /**
     * Creates a new empty directory at the given [path] if it doesn't exist.
     * @return instance of a [Path] used to create a directory
     * @throws IOException if the directory already exists or there are not enough permissions.
     */
    fun createDirectory(path: Path): Path

    /**
     * Moves a file represented by [source] path to the location specified by [target] path.
     */
    fun move(source: Path, target: Path): Path

    /**
     * Copies a file represented by [source] path to the location specified by [target] path.
     */
    fun copy(source: Path, target: Path): Path

    /**
     * Deletes a file or a directory at the given [path].
     *
     * If the given path represents a directory, it should be empty.
     * @throws IOException if the operation cannot be completed.
     */
    fun delete(path: Path): Boolean

    /**
     * Checks if the given [path] refers to a directory
     */
    fun isDirectory(path: Path): Boolean

    /**
     * Checks if the given [path] refers to a file
     */
    fun isFile(path: Path): Boolean
}
