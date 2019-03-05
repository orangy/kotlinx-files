package kotlinx.files

import kotlinx.cinterop.*
import kotlinx.io.errors.*
import platform.posix.*

class PosixDirectory(private val fileSystem: PosixFileSystem, private val path: UnixPath) :
    Directory {
    val dirPtr = opendir(path.toString())
        ?: throw IOException("Failed to open directory $path.", PosixException.forErrno())

    override val children = object : Iterable<UnixPath> {
        override fun iterator() = object : Iterator<UnixPath> {
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

            override fun next(): UnixPath {
                if (dirStruct == null)
                    throw NoSuchElementException("There are no more children in directory $path")
                val name = dirStruct!!.pointed.d_name.toKString()
                return UnixPath(fileSystem, "$path${fileSystem.pathSeparator}$name").also {
                    dirStruct = nextStruct()
                }
            }
        }
    }

    override fun close() {
        if (closedir(dirPtr) != -1)
            return
        throw IOException("Failed to close directory $path.", PosixException.forErrno())
    }
}