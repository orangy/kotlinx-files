package kotlinx.files

import java.nio.file.*

class JvmDirectory(private val fileSystem: JvmFileSystem, path: JvmPath) : Directory {
    private val stream = Files.newDirectoryStream(path.platformPath)

    override val children = object : Iterable<JvmPath> {
        override fun iterator(): Iterator<JvmPath> = object : Iterator<JvmPath> {
            private val directoryIterator = stream.iterator()
            override fun hasNext(): Boolean = directoryIterator.hasNext()
            override fun next(): JvmPath = JvmPath(fileSystem, directoryIterator.next())
        }
    }

    override fun close() {
        stream.close()
    }
}
