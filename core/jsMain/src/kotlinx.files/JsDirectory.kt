package kotlinx.files

import kotlinx.io.errors.*

class JsDirectory(private val fileSystem: JsFileSystem, override val path: UnixPath) : Directory {
    override val children = object : Iterable<UnixPath> {
        override fun iterator(): Iterator<UnixPath> {
            try {
                val children = JsFileSystem.fs.readdirSync(path.toString()) as Array<String>
                val paths = children
                    .filter { it != "." && it != ".." }
                    .map { UnixPath(fileSystem, "$path/$it") }
                return paths.iterator()
            } catch (e: dynamic) {
                throw IOException("Failed to read contents of directory $path: $e")
            }
        }
    }

    override fun close() {}
}