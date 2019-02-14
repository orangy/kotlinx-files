package kotlinx.files

class JsDirectory(private val fileSystem: JsFileSystem, override val path: UnixPath) : Directory {
    override val children = object : Iterable<UnixPath> {
        override fun iterator(): Iterator<UnixPath> {
            val children = JsFileSystem.fs.readdirSync(path.toString()) as Array<String>
            val paths = children
                .filter { it != "." && it != ".." }
                .map { UnixPath(fileSystem, "$path/$it") }
            return paths.iterator()
        }
    }

    override fun close() {}
}
