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
inline fun Path(name: String, vararg children: String): Path = FileSystems.Default.path(name, *children)

@Suppress("FunctionName")
inline fun Path(base: Path, vararg children: String): Path = FileSystems.Default.path(base.toString(), *children)

inline fun Path.openInput(): FileInput = fileSystem.openInput(this)
inline fun Path.readText(charset: Charset = Charsets.UTF_8): String =
    fileSystem.openInput(this).use { it.readText(charset) }

inline fun Path.createFile() = fileSystem.createFile(this)
inline fun Path.createDirectory() = fileSystem.createDirectory(this)

inline fun Path.deleteFile() {
    if (fileSystem.deleteFile(this))
        return
    throw IOException("File $this doesn't exist")
}

inline fun Path.deleteDirectory() {
    if (fileSystem.deleteDirectory(this))
        return
    throw IOException("Directory $this doesn't exist")
}

inline fun Path.deleteFileIfExists() = fileSystem.deleteFile(this)
inline fun Path.deleteDirectoryIfExists() = fileSystem.deleteDirectory(this)

inline val Path.isDirectory: Boolean get() = fileSystem.isDirectory(this)
inline val Path.isFile: Boolean get() = fileSystem.isFile(this)

inline fun Path.copyTo(path: Path) = fileSystem.copy(this, path)
inline fun Path.moveTo(path: Path) = fileSystem.move(this, path)
inline fun Path.exists() = fileSystem.exists(this)

// TODO: Do we need these plus-via-string operations? Why?
operator fun Path.plus(other: Path): Path = fileSystem.path(this.toString(), other.toString())

operator fun Path.plus(other: String): Path = fileSystem.path(this.toString(), other)
