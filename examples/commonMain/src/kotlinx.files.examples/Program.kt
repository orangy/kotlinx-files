package kotlinx.files.examples

import kotlinx.files.*

fun main() {
    val text = Path("content/utf8.txt").readText()
    println(text)
}