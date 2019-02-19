package kotlinx.files

import kotlinx.cinterop.*
import kotlinx.cinterop.internal.*
import platform.posix.*
import kotlinx.io.errors.*

fun winstat(@CCall.CString _Filename: String?, _Stat: CValuesRef<_stat64>?): Int = memScoped {
    stat64!!(_Filename?.cstr?.getPointer(memScope), _Stat?.getPointer(memScope))
}

fun winfstat(descriptor: Int, _Stat: CValuesRef<_stat64>?): Int = memScoped {
    fstat64!!(descriptor, _Stat?.getPointer(memScope))
}

actual fun readAttributes(path: Path): PosixFileAttributes = memScoped {
    val stat = alloc<_stat64>()
    if (winstat(path.toString(), stat.ptr) == -1) {
        throw IOException("Failed to call 'stat' on file $path.", PosixException.forErrno())
    }
    attributesFromStat(stat)
}

actual fun readAttributes(fd: Int): PosixFileAttributes = memScoped {
    val stat = alloc<_stat64>()
    if (winfstat(fd, stat.ptr) == -1) {
        throw IOException("Failed to call 'fstat' on descriptor $fd.", PosixException.forErrno())
    }
    attributesFromStat(stat)
}


private fun attributesFromStat(stat: _stat64): PosixFileAttributes {
    val fileType = stat.st_mode.toInt() and S_IFMT
    val permissions = PosixFilePermissions.parse(stat.st_mode.toInt())

    return PosixFileAttributes(
        isDirectory = fileType == S_IFDIR,
        isFile = fileType == S_IFREG,
        isSymbolicLink = false, // TODO: fileType == S_IFLNK
        creationTimeUs = stat.st_ctime.micros(),
        lastAccessTimeUs = stat.st_atime.micros(),
        lastModifiedTimeUs = stat.st_mtime.micros(),
        sizeBytes = stat.st_size,
        permissions = permissions
    )
}

private fun __time64_t.micros(): Long = this * 1000000L

fun PosixFilePermissions.Companion.parse(mode: Int): Set<PosixFilePermissions> {
    val result = mutableSetOf<PosixFilePermissions>()
    if (mode and S_IRUSR != 0) result.add(PosixFilePermissions.OWNER_READ)
    if (mode and S_IWUSR != 0) result.add(PosixFilePermissions.OWNER_WRITE)
    if (mode and S_IXUSR != 0) result.add(PosixFilePermissions.OWNER_EXECUTE)

    if (mode and S_IRGRP != 0) result.add(PosixFilePermissions.GROUP_READ)
    if (mode and S_IWGRP != 0) result.add(PosixFilePermissions.GROUP_WRITE)
    if (mode and S_IXGRP != 0) result.add(PosixFilePermissions.GROUP_EXECUTE)

/* TODO:
    if (mode and S_IROTH != 0) result.add(PosixFilePermissions.OTHERS_READ)
    if (mode and S_IWOTH != 0) result.add(PosixFilePermissions.OTHERS_WRITE)
    if (mode and S_IXOTH != 0) result.add(PosixFilePermissions.OTHERS_EXECUTE)
*/

    return result
}
