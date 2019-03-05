package kotlinx.files

import kotlinx.io.core.*

/**
 * Represents a directory open for enumeration. 
 */
interface Directory : Closeable {
    /**
     * Provides a collection of [Path] instances representing files and folders with this directory.
     */
    val children: Iterable<Path>
}

/**
 * Enumerates files and folders in the directory represented by [path] and calls [consumer] on each of them.
 */
fun FileSystem.forEachFile(path: Path, consumer: (Path) -> Unit): Unit = openDirectory(path).children.forEach(consumer)
