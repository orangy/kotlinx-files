package kotlinx.files.test

import kotlinx.files.*
import kotlin.test.*

abstract class PathTest(private val fileSystem: FileSystem) {
    private fun path(path: String) = fileSystem.path(path)
    protected val separator get() = fileSystem.pathSeparator

    @Test
    fun pathEmpty() {
        val path = path("")
        checkSingleFileName(path, "")
    }

    @Test
    fun pathCurrentDirectory() {
        val path = path(".")
        checkSingleFileName(path, ".")
    }

    @Test
    fun pathParentDirectory() {
        val path = path("..")
        checkSingleFileName(path, "..")
    }

    @Test
    fun pathRoot() {
        val path = path(separator)
        assertTrue(path.isAbsolute)
        assertNull(path.name)
        assertEquals(0, path.componentCount)
        assertEquals(separator, path.toString())
        assertNull(path.parent)
        assertFailsWith<IllegalArgumentException> { path.component(0) }
    }

    @Test
    fun pathFileName() {
        val path = path("42.txt")
        checkSingleFileName(path, "42.txt")
    }

    @Test
    fun pathConcatenation() {
        // Where is my parametrized :(

        checkConcatenation(path("a"), "b", "a${separator}b")
        checkConcatenation(path("a"), "${separator}b$separator", "a${separator}b")
        checkConcatenation(
            path("foo$separator..$separator.."),
            "..$separator..$separator",
            "foo$separator..$separator..$separator..$separator.."
        )
        checkConcatenation(path(""), separator, separator)
        checkConcatenation(path(separator), "", separator)
        checkConcatenation(path("."), "bar", ".${separator}bar")
        checkConcatenation(path("bar"), ".", "bar$separator.")
        checkConcatenation(path("foo.txt"), "foo.txt", "foo.txt${separator}foo.txt")
    }

    private fun checkConcatenation(base: Path, other: String, expected: String) {
        val result1 = base + other
        val result2 = base + path(other)

        assertEquals(expected, result1.toString())
        assertEquals(expected, result2.toString())
    }

    private fun checkSingleFileName(path: Path, expectedName: String) {
        assertFalse(path.isAbsolute)
        assertEquals(expectedName, path.name)
        assertEquals(1, path.componentCount)
        assertEquals(expectedName, path.toString())
        assertNull(path.parent)
        assertFailsWith<IllegalArgumentException> { path.component(1) }
    }

    @Test
    fun pathRelativeSlashes() {
        checkSlashes("foo${separator}bar$separator${separator}test$separator$separator${separator}file.txt")
        checkSlashes("foo${separator}bar$separator${separator}test$separator$separator${separator}file.txt$separator")
        checkSlashes("foo${separator}bar$separator${separator}test${separator}file.txt$separator$separator")
    }

    @Test
    fun pathAbsoluteSlashes() {
        checkSlashes(
            "${separator}foo${separator}bar$separator${separator}test$separator$separator${separator}file.txt",
            separator
        )
        checkSlashes(
            "${separator}foo${separator}bar$separator${separator}test$separator$separator${separator}file.txt$separator",
            separator
        )
        checkSlashes(
            "${separator}foo${separator}bar$separator${separator}test${separator}file.txt$separator$separator",
            separator
        )
    }

    private fun checkSlashes(forwardSlashPath: String, prefix: String = "") {
        val path = path(forwardSlashPath.replace(separator, separator))

        assertEquals("file.txt", path.name)
        assertEquals(4, path.componentCount)
        assertEquals(prefix + "foo${separator}bar${separator}test${separator}file.txt", path.toString())
        assertEquals(prefix + "foo${separator}bar${separator}test", path.parent!!.toString())

        assertEquals("foo", path.component(0).toString())
        assertEquals("bar", path.component(1).toString())
        assertEquals("test", path.component(2).toString())
        assertEquals("file.txt", path.component(3).toString())
    }


    @Test
    fun pathDenormalized() {
        val path =
            path("1${separator}2${separator}3$separator..${separator}3$separator..$separator..${separator}2$separator${separator}1.txt")
        assertEquals("1.txt", path.name)
        assertEquals(9, path.componentCount)
        assertEquals(
            "1${separator}2${separator}3$separator..${separator}3$separator..$separator..${separator}2${separator}1.txt",
            path.toString()
        )
        assertEquals(
            "1${separator}2${separator}3$separator..${separator}3$separator..$separator..${separator}2",
            path.parent!!.toString()
        )

        assertEquals("1", path.component(0).toString())
        assertEquals("2", path.component(1).toString())
        assertEquals("3", path.component(2).toString())
        assertEquals("..", path.component(3).toString())
    }

}
