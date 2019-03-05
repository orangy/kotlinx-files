@file:Suppress("NOTHING_TO_INLINE")

package kotlinx.files

import kotlinx.io.charsets.*
import kotlinx.io.core.*
import kotlinx.io.errors.*

/**
 * Reads all bytes from a file located at the given [path] and returns [ByteArray].
 * @throws IOException if the operation cannot be completed.
 */
inline fun FileSystem.readBytes(path: Path): ByteArray = openInput(path).use { it.readBytes() }

/**
 * Writes all bytes from [bytes] to a file at the given [path].
 * @throws IOException if the operation cannot be completed.
 */
inline fun FileSystem.writeBytes(path: Path, bytes: ByteArray) : Unit = openOutput(path).use { it.writeFully(bytes) }

/**
 * Reads all bytes from a file located at the given Path and returns [ByteArray].
 * @throws IOException if the operation cannot be completed.
 */
inline fun Path.readBytes(): ByteArray = fileSystem.readBytes(this)

/**
 * Writes all bytes from [bytes] to a file at the given Path.
 * @throws IOException if the operation cannot be completed.
 */
inline fun Path.writeBytes(bytes: ByteArray): Unit = fileSystem.writeBytes(this, bytes)

/**
 * Constructs a new instance of the [Path] for the given [FileSystem] with the [base] path and all [children].
 * @return instance of [Path] representing a combined path in the given file system.
 * @throws IOException if the operation cannot be completed.
 */
inline fun FileSystem.path(base: Path, vararg children: String): Path = path(base.toString(), *children)

/**
 * Constructs a new instance of the [Path] for the default file system with the [base] path and all [children].
 * @return instance of [Path] representing a combined path in the default file system.
 * @throws IOException if the operation cannot be completed.
 */
@Suppress("FunctionName")
inline fun Path(base: String, vararg children: String): Path = FileSystems.Default.path(base, *children)

/**
 * Constructs a new instance of the [Path] for the default file system with the [base] path and all [children].
 * @return instance of [Path] representing a combined path in the default file system.
 * @throws IOException if the operation cannot be completed.
 */
@Suppress("FunctionName")
inline fun Path(base: Path, vararg children: String): Path = FileSystems.Default.path(base.toString(), *children)

/**
 * Opens a file at the given path for input. Caller is responsible for closing the returned [FileInput].
 * @return instance of [FileInput]
 * @throws IOException if the operation cannot be completed.
 */
inline fun Path.openInput(): FileInput = fileSystem.openInput(this)

/**
 * Opens a file at the given path for output. Caller is responsible for closing the returned [FileInput].
 * @return instance of [FileOutput]
 * @throws IOException if the operation cannot be completed.
 */
inline fun Path.openOutput(): FileOutput = fileSystem.openOutput(this)

/**
 * Opens a directory at the given path for enumeration. Caller is responsible for closing the returned [Directory].
 * @return instance of [Directory]
 * @throws IOException if the operation cannot be completed.
 */
inline fun Path.openDirectory(): Directory = fileSystem.openDirectory(this)

/**
 * Enumerates all the children of the [Directory] specified by the given path. 
 * @return list of [Path] elements representing all children.
 * @throws IOException if the operation cannot be completed.
 */
fun Path.listChildren(): List<Path> = openDirectory().use { it.children.toList() }

/**
 * Reads all content from a file located at the given path and decodes it into a [String] using given [charset].
 * @throws IOException if the operation cannot be completed.
 */
inline fun Path.readText(charset: Charset = Charsets.UTF_8): String =
    fileSystem.openInput(this).use { it.readText(charset) }

/**
 * Creates a new empty file at the given path if it doesn't exist. 
 * @return instance of a [Path] used to create a file
 * @throws IOException if the file already exists or there are not enough permissions. 
 */
inline fun Path.createFile() : Path = fileSystem.createFile(this)

/**
 * Creates a new empty directory at the given path if it doesn't exist.
 * @return instance of a [Path] used to create a directory
 * @throws IOException if the directory already exists or there are not enough permissions.
 */
inline fun Path.createDirectory() : Path = fileSystem.createDirectory(this)

/**
 * Deletes a file or a directory at the given path.
 * 
 * If the given path represents a directory, it should be empty. 
 * @throws IOException if the operation cannot be completed.
 */
inline fun Path.delete() {
    if (fileSystem.delete(this))
        return
    throw IOException("File $this doesn't exist")
}

/**
 * Deletes a file or a directory at the given path.
 * @return `true` if the file or directory has been deleted, false otherwise.
 */
inline fun Path.deleteIfExists() = fileSystem.delete(this)

/**
 * Checks if the given path refers to a directory
 */
inline val Path.isDirectory: Boolean get() = fileSystem.isDirectory(this)

/**
 * Checks if the given path refers to a file
 */
inline val Path.isFile: Boolean get() = fileSystem.isFile(this)

/**
 * Copies a file represented by a given receiver path to the location specified by [path].
 */
inline fun Path.copyTo(path: Path) = fileSystem.copy(this, path)

/**
 * Moves a file represented by a given receiver path to the location specified by [path].
 */
inline fun Path.moveTo(path: Path) = fileSystem.move(this, path)

/**
 * Checks if the given represents an existing file
 */
inline fun Path.exists() = fileSystem.exists(this)

// TODO: Do we need these plus-via-string operations? Why?
operator fun Path.plus(other: Path): Path = fileSystem.path(this.toString(), other.toString())

operator fun Path.plus(other: String): Path = fileSystem.path(this.toString(), other)

/**
 * Copies a directory specified by [source] path recursively (including all subfolders and files) into a location specified by [target] path.
 */
fun FileSystem.copyDirectoryRecursively(source: Path, target: Path): Unit =
    openDirectory(source).use { directory ->
        if (!exists(target)) {
            createDirectory(target)
        }

        for (sourceChild in directory.children) {
            val childName = sourceChild.name?.toString()
            if (childName != null) {
                val targetChild = path(target, childName)
                if (sourceChild.isDirectory)
                    copyDirectoryRecursively(sourceChild, targetChild)
                else
                    copy(sourceChild, targetChild)
            }
        }
    }

/**
 * Deletes a directory specified by [path] recursively (including all subfolders and files).
 */
fun FileSystem.deleteDirectoryRecursively(path: Path): Unit = openDirectory(path).use { directory ->
    for (child in directory.children) {
        if (isDirectory(child))
            deleteDirectoryRecursively(child)
        else
            delete(child)
    }
    delete(path)
}