package kotlinx.files

import java.nio.file.*
import java.util.stream.*

class JvmDirectory(private val fileSystem: JvmFileSystem, override val path: JvmPath) : Directory {
    override val children = object : Iterable<JvmPath> {
        override fun iterator(): Iterator<JvmPath> {
            val children = Files.list(path.platformPath).collect(Collectors.toList())
            val paths = children.map { JvmPath(fileSystem, it) }
            return paths.iterator()
        }
    }

    override fun close() {}
}
