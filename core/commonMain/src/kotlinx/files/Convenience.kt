@file:Suppress("NOTHING_TO_INLINE")

package kotlinx.files

import kotlinx.io.charsets.*
import kotlinx.io.core.*
import kotlinx.io.errors.*

inline fun FileSystem.readBytes(path: Path): ByteArray = openInput(path).use { it.readBytes() }
inline fun FileSystem.writeBytes(path: Path, bytes: ByteArray) = openOutput(path).use { it.writeFully(bytes) }

inline fun Path.readBytes(): ByteArray = fileSystem.readBytes(this)
inline fun Path.writeBytes(bytes: ByteArray) = fileSystem.writeBytes(this, bytes)

@Suppress("FunctionName")
inline fun FileSystem.path(base: Path, vararg children: String): Path = path(base.toString(), *children)

@Suppress("FunctionName")
inline fun Path(name: String, vararg children: String): Path = FileSystems.Default.path(name, *children)

@Suppress("FunctionName")
inline fun Path(base: Path, vararg children: String): Path = FileSystems.Default.path(base.toString(), *children)

inline fun Path.openInput(): FileInput = fileSystem.openInput(this)
inline fun Path.openDirectory(): Directory = fileSystem.openDirectory(this)

// TODO: should it be in public API?
fun Path.list(): List<Path> = openDirectory().use { it.children.toList() }

inline fun Path.readText(charset: Charset = Charsets.UTF_8): String =
    fileSystem.openInput(this).use { it.readText(charset) }

inline fun Path.createFile() = fileSystem.createFile(this)
inline fun Path.createDirectory() = fileSystem.createDirectory(this)

inline fun Path.delete() {
    if (fileSystem.delete(this))
        return
    throw IOException("File $this doesn't exist")
}

inline fun Path.deleteFileIfExists() = fileSystem.delete(this)
inline fun Path.deleteDirectoryIfExists() = fileSystem.delete(this)

inline val Path.isDirectory: Boolean get() = fileSystem.isDirectory(this)
inline val Path.isFile: Boolean get() = fileSystem.isFile(this)

inline fun Path.copyTo(path: Path) = fileSystem.copy(this, path)
inline fun Path.moveTo(path: Path) = fileSystem.move(this, path)
inline fun Path.exists() = fileSystem.exists(this)

// TODO: Do we need these plus-via-string operations? Why?
operator fun Path.plus(other: Path): Path = fileSystem.path(this.toString(), other.toString())

operator fun Path.plus(other: String): Path = fileSystem.path(this.toString(), other)


fun FileSystem.copyDirectoryRecursively(source: Path, target: Path): Unit =
    openDirectory(source).use { directory ->
        if (!exists(target)) {
            createDirectory(target)
        }

        for (sourceChild in directory.children) {
            val directoryName = sourceChild.name?.toString()
            if (directoryName != null) {
                val targetChild = path(target, directoryName)
                if (sourceChild.isDirectory)
                    copyDirectoryRecursively(sourceChild, targetChild)
                else
                    copy(sourceChild, targetChild)
            }
        }
    }

/**
 * This is a dangerous method, so it is defined on FS only 
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