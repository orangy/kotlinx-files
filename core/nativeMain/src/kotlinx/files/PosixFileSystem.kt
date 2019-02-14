package kotlinx.files

import kotlinx.cinterop.*
import kotlinx.io.core.*
import kotlinx.io.errors.*
import platform.posix.*
import kotlin.contracts.*

@UseExperimental(ExperimentalIoApi::class)
class PosixFileSystem : FileSystem {

    override fun path(name: String, vararg children: String): UnixPath {
        if (children.isEmpty()) {
            return UnixPath(this, name)
        }
        return UnixPath(this, "$name/" + children.joinToString("/"))
    }

    override fun exists(path: Path): Boolean {
        checkCompatible(path)
        return access(path.str(), F_OK) == 0
    }

    override fun openDirectory(path: Path): PosixDirectory {
        checkCompatible(path)
        return PosixDirectory(this, path)
    }

    override val isReadOnly: Boolean get() = false

    override fun isDirectory(path: Path): Boolean = exists(path) && readFileType(path) == S_IFDIR
    override fun isFile(path: Path): Boolean = exists(path) && readFileType(path) == S_IFREG

    private fun readFileType(path: Path): Int = memScoped {
        val stat = alloc<stat>()
        if (lstat(path.str(), stat.ptr) == -1) {
            val errno = errno
            throw IOException(
                "Failed to call 'lstat' on file $path with error code $errno",
                PosixException.forErrno(errno)
            )
        }
        return stat.st_mode.toInt() and S_IFMT
    }


    override fun createFile(path: Path): UnixPath {
        checkCompatible(path)
        // 0x1B6 hex == 438 == 0666 oct
        open(path.str(), O_WRONLY or O_CREAT, 0x1B6)
        return path
    }

    override fun createDirectory(path: Path): UnixPath {
        checkCompatible(path)
        // 0x1FF hex == 511 == 0777 oct
        if (mkdir(path.str(), 0x1FF) == -1) {
            val errno = errno
            throw IOException(
                "Failed to create directory ${path.str()} with error code $errno",
                PosixException.forErrno(errno)
            )
        }
        return path
    }

    override fun copy(source: Path, target: Path): UnixPath {
        checkCompatible(source)
        checkCompatible(target)

        if (exists(target)) {
            throw IOException("Path $target already exists")
        }

        when {
            source.isDirectory -> copyDirectoryRecursive(source, target) 
            source.isFile -> copyFile(source, target)
            else -> throw IOException("Links are not supported by implementation")
        }

        return target
    }

    private fun copyDirectoryRecursive(source: Path, target: Path): Unit = openDirectory(source).use { directory ->
        if (!exists(target)) {
            createDirectory(target)
        }

        for (sourceChild in directory.children) {
            val targetChild = UnixPath(this, "$target/${sourceChild.name}")
            if (sourceChild.isDirectory)
                copyDirectoryRecursive(sourceChild, targetChild)
            else
                copy(sourceChild, targetChild)
        }
    }

    override fun move(source: Path, target: Path): UnixPath {
        checkCompatible(source)
        checkCompatible(target)

        if (exists(target)) {
            throw IOException("File $target already exists")
        }

        if (rename(source.str(), target.str()) == -1) {
            val errno = errno
            throw IOException(
                "Failed to move $source to $target with error code $errno",
                PosixException.forErrno(errno)
            )
        }

        return target
    }

    private fun copyFile(source: Path, target: Path) {
        openOutput(target).use { output ->
            openInput(source).use { input ->
                input.copyTo(output)
            }
        }
    }

    override fun deleteDirectory(path: Path): Boolean {
        if (!exists(path))
            return false
        deleteRecursively(path)
        return true
    }
    
    private fun deleteRecursively(path: Path) : Unit = openDirectory(path).use { directory ->
        for (child in directory.children) {
            if (isDirectory(child))
                deleteRecursively(child)
            else
                delete(child)
        }
        delete(path)
    }

    override fun delete(path: Path): Boolean {
        checkCompatible(path)
        if (!exists(path))
            return false

        val isDirectory = path.isDirectory
        val hasError = if (isDirectory) {
            rmdir(path.str()) == -1
        } else {
            unlink(path.str()) == -1
        }

        val error = if (hasError) errno else 0

        if (error != 0 && error != ENOENT) {
            throw IOException(
                "Failed to delete ${path.str()} (isDirectory = $isDirectory) with error code $error",
                PosixException.forErrno(error)
            )
        }

        return error != ENOENT
    }

    override fun openInput(path: Path): FileInput {
        checkCompatible(path)

        val fd = open(path.str(), O_RDONLY)
        if (fd == -1) {
            val errno = errno
            throw IOException(
                "Failed to open ${path.str()} for reading with error code $errno",
                PosixException.forErrno(errno)
            )
        }

        return PosixFileInput(path, fd)
    }

    override fun openOutput(path: Path): FileOutput {
        checkCompatible(path)

        val fd = open(path.str(), O_CREAT or O_WRONLY or O_TRUNC, 0x1B6) // TODO constant
        if (fd == -1) {
            val errno = errno
            throw IOException(
                "Failed to open ${path.str()} for writing with error code $errno",
                PosixException.forErrno(errno)
            )
        }

        return PosixFileOutput(path, fd)
    }

    companion object {
        val Default = PosixFileSystem()
    }
}

@PublishedApi
internal fun Path.str(): String = (this as UnixPath).normalizedPath

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
