package kotlinx.files.examples

import kotlinx.files.*
import kotlinx.io.core.*
import kotlin.math.*

fun main(args: Array<out String>) {
    val path = Path(args.lastOrNull() ?: ".")
    ls(path, emptyMap())
}

fun ls(path: Path, args: Map<String, String>) {
    if (!path.exists()) {
        println("ls: $path: No such file or directory")
    }

    if (path.isDirectory) {
        path.openDirectory().use {
            it.children.sortedBy { it.name.toString() }.forEach {
                println(describe(it))
            }
        }
    } else {
        println(describe(path))
    }
}

private fun describe(path: Path): String {
    val attributes = path.readAttributes<PosixFileAttributes>()
    // We could use String.format here
    return attributes.permissions.toHumanReadableString() +
            "\t" + attributes.size.prettify() +
            "\t" + attributes.lastAccessTimeUs +
            "\t" + path.name.toString()
}

private fun Long.prettify(): String {
    return when {
        this < 1000L -> "$this "
        this < 1000 * 1000L -> (round(this / 1000.0 * 10) / 10).toString() + "K"
        else -> (round(this / 1000000.0 * 10) / 10).toLong().toString() + "M"

    }.padStart(8)
}

