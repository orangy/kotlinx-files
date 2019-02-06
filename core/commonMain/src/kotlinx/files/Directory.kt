package kotlinx.files

interface Directory : Iterable<Path> {
    val path: Path
}

fun FileSystem.forEachFile(path: Path, consumer: (Path) -> Unit): Unit = openDirectory(path).forEach(consumer)
