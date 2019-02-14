package kotlinx.files

class JsDirectory(private val fileSystem: JsFileSystem, override val path: Path) : Directory {
    override val children: Iterable<Path> = object : Iterable<Path> {
        override fun iterator(): Iterator<Path> {
            val children = JsFileSystem.fs.readdirSync(path.toString()) as Array<String>
            val paths = children
                .filter { it != "." && it != ".." }
                .map { UnixPath(fileSystem, "$path/$it") }
            return paths.iterator()
        }
    }

    override fun close() {}
}
