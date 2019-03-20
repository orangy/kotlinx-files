package kotlinx.files

import kotlinx.cinterop.*
import kotlinx.io.errors.*
import platform.posix.*

actual fun statAttributes(path: Path): PosixFileAttributes = memScoped {
    val stat = alloc<stat>()
    if (lstat(path.toString(), stat.ptr) == -1) {
        throw IOException("Failed to call 'lstat' on file $path.", PosixException.forErrno())
    }
    attributesFromStat(stat)
}

actual fun statAttributes(fd: Int): PosixFileAttributes = memScoped {
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

fun PosixFilePermissions.Companion.parse(mode: Int): Set<PosixFilePermissions> {
    val result = mutableSetOf<PosixFilePermissions>()
    if (mode and S_IRUSR != 0) result.add(PosixFilePermissions.OWNER_READ)
    if (mode and S_IWUSR != 0) result.add(PosixFilePermissions.OWNER_WRITE)
    if (mode and S_IXUSR != 0) result.add(PosixFilePermissions.OWNER_EXECUTE)

    if (mode and S_IRGRP != 0) result.add(PosixFilePermissions.GROUP_READ)
    if (mode and S_IWGRP != 0) result.add(PosixFilePermissions.GROUP_WRITE)
    if (mode and S_IXGRP != 0) result.add(PosixFilePermissions.GROUP_EXECUTE)

    if (mode and S_IROTH != 0) result.add(PosixFilePermissions.OTHERS_READ)
    if (mode and S_IWOTH != 0) result.add(PosixFilePermissions.OTHERS_WRITE)
    if (mode and S_IXOTH != 0) result.add(PosixFilePermissions.OTHERS_EXECUTE)

    if (mode and S_ISUID != 0) result.add(PosixFilePermissions.SETUID)
    if (mode and S_ISGID != 0) result.add(PosixFilePermissions.SETGID)
    if (mode and S_ISVTX != 0) result.add(PosixFilePermissions.STICKY_BIT)

    return result
}
