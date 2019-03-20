package kotlinx.files.examples

import kotlinx.files.*
import kotlin.math.*

fun main(args: Array<out String>) {
    Ls.execute(args.lastOrNull() ?: ".", emptyMap())
}

interface FsOperation {
    fun execute(path: String, args: Map<String, String>): Unit
    fun getName(): String
}

object Ls : FsOperation {

    override fun execute(path: String, args: Map<String, String>) {
        val path = Path(path)
        if (!path.exists()) {
            println("ls: $path: No such file or directory")
        }

        if (path.isDirectory) {
            path.forEachFile { println(describe(it)) }
        } else {
            println(describe(path))
        }

    }

    private fun describe(path: Path): String {
        val attributes = path.attributesOfType<PosixFileAttributes>()
        // We could use String.format here
        return attributes.permissions.toHumanReadableString() + "\t" + path.totalSize().prettify() +
                "\t" + attributes.lastAccessTimeUs + "\t" + path.getFileName()
    }

    private fun Long.prettify(): String {
        return when {
            this < 1000L -> "$this" + "B"
            this < 1000 * 1000L -> {
                (round(this / 1000.0 * 10) / 10).toString() + "K"
            }
            else -> {
                (round(this / 1000000.0 * 10) / 10).toLong().toString() + "M"
            }
        }
    }

    override fun getName(): String = "ls"
}
