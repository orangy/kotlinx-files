package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*

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

/**
 * Enumerates files and folders in the directory represented by this oath and calls [consumer] on each of them.
 */
fun Path.forEachFile(consumer: (Path) -> Unit): Unit = fileSystem.openDirectory(this).children.forEach(consumer)

/**
 * Opens a directory specified by this path, executes [operation] on `it` and closes it.
 */
fun <R> Path.useDirectory(operation: (Directory) -> R): R = openDirectory().use(operation)

/**
 * Enumerates all the children of the [Directory] specified by the given path.
 * @return list of [Path] elements representing all children.
 * @throws IOException if the operation cannot be completed.
 */
fun Path.listFiles(): List<Path> = useDirectory { it.children.toList() }

