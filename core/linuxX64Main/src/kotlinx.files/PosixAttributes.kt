package kotlinx.files

import kotlinx.cinterop.*
import kotlinx.io.errors.*
import platform.posix.*

actual fun readAttributes(path: Path): PosixFileAttributes = memScoped {
    val stat = alloc<stat>()
    if (lstat(path.toString(), stat.ptr) == -1) {
        throw IOException("Failed to call 'lstat' on file $path.", PosixException.forErrno())
    }
    attributesFromStat(stat)
}

actual fun readAttributes(fd: Int): PosixFileAttributes = memScoped {
    val stat = alloc<stat>()
    if (fstat(fd, stat.ptr) == -1) {
        throw IOException("Failed to call 'fstat' on descriptor $fd.", PosixException.forErrno())
    }
    attributesFromStat(stat)
}

private fun attributesFromStat(stat: stat): PosixFileAttributes {
    val fileType = stat.st_mode.toInt() and S_IFMT
    val permissions = PosixFilePermissions.parse(stat.st_mode.toInt())

    return PosixFileAttributes(
        isDirectory = fileType == S_IFDIR,
        isFile = fileType == S_IFREG,
        isSymbolicLink = fileType == S_IFLNK,
        creationTimeUs = stat.st_ctimespec.micros(),
        lastAccessTimeUs = stat.st_atimespec.micros(),
        lastModifiedTimeUs = stat.st_mtimespec.micros(),
        sizeBytes = stat.st_size,
        permissions = permissions
    )
}
