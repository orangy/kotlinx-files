package kotlinx.files

import kotlinx.cinterop.*
import kotlinx.io.errors.*
import platform.posix.*

class PosixDirectory(private val fileSystem: PosixFileSystem, override val path: Path) :
    Directory {
    val dirPtr = opendir(path.toString())
        ?: throw IOException(
            "Failed to open directory $path",
            PosixException.forErrno()
        )

    override val children = object : Iterable<Path> {
        override fun iterator() = object : Iterator<Path> {
            var dirStruct = nextStruct()

            private fun nextStruct(): CPointer<dirent>? {
                var dirStruct = readdir(dirPtr)
                while (dirStruct != null) {
                    val name = dirStruct.pointed.d_name.toKString()
                    if (name != "." && name != "..") {
                        return dirStruct
                    }
                    dirStruct = readdir(dirPtr)
                }
                return dirStruct
            }

            override fun hasNext() = dirStruct != null

            override fun next(): Path {
                if (dirStruct == null)
                    throw NoSuchElementException("There are no more children in directory $path")
                val name = dirStruct!!.pointed.d_name.toKString()
                return UnixPath(fileSystem, "$path/$name").also {
                    dirStruct = nextStruct()
                }
            }
        }
    }

    override fun close() {
        if (closedir(dirPtr) != -1)
            return
        val errno = errno
        throw IOException(
            "Failed to close directory $path with error code $errno",
            PosixException.forErrno(errno)
        )
    }
}