package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*
import platform.posix.*
import kotlin.contracts.*
import kotlin.reflect.*

@UseExperimental(ExperimentalIoApi::class)
class PosixFileSystem : FileSystem {
    override val pathSeparator: String
        get() = posixPathSeparator

    override fun path(base: String, vararg children: String): UnixPath {
        if (children.isEmpty()) {
            return UnixPath(this, base)
        }
        return UnixPath(this, "$base$pathSeparator${children.joinToString(pathSeparator)}")
    }

    override fun exists(path: Path): Boolean {
        checkCompatible(path)
        return access(path.toString(), F_OK) == 0
    }

    override fun openDirectory(path: Path): PosixDirectory {
        checkCompatible(path)
        return PosixDirectory(this, path)
    }

    override val isReadOnly: Boolean get() = false

    override fun createFile(path: Path): UnixPath {
        checkCompatible(path)
        // 0x1B6 hex == 438 == 0666 oct
        val fd = open(path.toString(), O_WRONLY or O_CREAT, 0x1B6)
        if (fd == -1) {
            throw IOException("Failed to create $path", PosixException.forErrno())
        }
        close(fd)
        return path
    }

    override fun createDirectory(path: Path): UnixPath {
        checkCompatible(path)
        if (compat_mkdir(path.toString()) == -1) {
            throw IOException("Failed to create directory $path with error code $errno", PosixException.forErrno())
        }
        return path
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : FileAttributes> readAttributes(path: Path, attributesClass: KClass<T>): T {
        checkCompatible(path)
        if (attributesClass != FileAttributes::class && attributesClass != PosixFileAttributes::class) {
            throw UnsupportedOperationException("File attributes of class $attributesClass are not supported by this file system")
        }
        return statAttributes(path) as T
    }

    override fun copy(source: Path, target: Path): UnixPath {
        checkCompatible(source)
        checkCompatible(target)

        if (exists(target)) {
            throw IOException("Path $target already exists")
        }

        when {
            source.isDirectory -> {
                // TODO: Copy permissions & ownership
                createDirectory(target)
            }
            source.isFile -> copyFile(source, target)
            else -> throw IOException("Links are not supported by implementation")
        }

        return target
    }

    override fun move(source: Path, target: Path): UnixPath {
        checkCompatible(source)
        checkCompatible(target)

        if (exists(target)) {
            throw IOException("File $target already exists")
        }

        if (rename(source.toString(), target.toString()) == -1) {
            throw IOException("Failed to move $source to $target", PosixException.forErrno())
        }

        return target
    }

    private fun copyFile(source: Path, target: Path) {
        openInput(source).use { input ->
            openOutput(target).use { output ->
                input.copyTo(output)
            }
        }
    }

    override fun delete(path: Path): Boolean {
        checkCompatible(path)
        if (!exists(path))
            return false

        val isDirectory = path.isDirectory
        val stringPath = path.toString()
        val hasError = if (isDirectory) {
            rmdir(stringPath) == -1
        } else {
            unlink(stringPath) == -1
        }

        val error = if (hasError) errno else 0

        if (error == ENOTEMPTY) {
            // TODO: directory not empty, what to do?
            return false
        }

        if (error != 0 && error != ENOENT) {
            throw IOException("Failed to delete $path (isDirectory = $isDirectory)", PosixException.forErrno(error))
        }

        return error != ENOENT
    }

    override fun openInput(path: Path): FileInput {
        checkCompatible(path)

        val stringPath = path.toString()
        val fd = open(stringPath, O_RDONLY or O_BINARY)
        if (fd == -1) {
            throw IOException("Failed to open $path for reading.", PosixException.forErrno())
        }

        return PosixFileInput(stringPath, fd)
    }

    override fun openOutput(path: Path): FileOutput {
        checkCompatible(path)

        val stringPath = path.toString()
        val fd = open(stringPath, O_CREAT or O_WRONLY or O_TRUNC or O_BINARY, 0x1B6) // TODO constant
        if (fd == -1) {
            throw IOException("Failed to open $path for writing.", PosixException.forErrno())
        }

        return PosixFileOutput(stringPath, fd)
    }

    companion object {
        val Default = PosixFileSystem()
    }
}

@PublishedApi
internal fun timespec.micros(): Long = tv_sec * 1000000L + tv_nsec / 1000L

@UseExperimental(ExperimentalContracts::class)
private fun PosixFileSystem.checkCompatible(path: Path) {
    contract {
        returns() implies (path is UnixPath)
    }
    if (path is UnixPath && path.fileSystem == this)
        return
    throw IllegalStateException("FileSystem ${path.fileSystem} for path $path is not compatible with $this")
}
