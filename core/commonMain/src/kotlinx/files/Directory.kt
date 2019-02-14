package kotlinx.files

import kotlinx.io.core.*

interface Directory : Closeable {
    val path: Path
    val children: Iterable<Path>
}

fun FileSystem.forEachFile(path: Path, consumer: (Path) -> Unit): Unit = openDirectory(path).children.forEach(consumer)
