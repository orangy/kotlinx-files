package kotlinx.files

import kotlinx.cinterop.*
import kotlinx.io.errors.*
import platform.posix.*

actual fun Path.stat(): PosixFileAttributes = memScoped {
    val path = this@stat.toString()
    val stat = alloc<stat64>()
    if (stat64(path, stat.ptr) == -1) {
        val errno = errno
        throw IOException(
            "Failed to call 'stat64' on file $path with error code $errno",
            PosixException.forErrno(errno)
        )
    }

    val fileType = stat.st_mode.toInt() and S_IFMT
    val permissions = PosixFilePermissions.parse(stat.st_mode.toInt())

    PosixFileAttributes(
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

