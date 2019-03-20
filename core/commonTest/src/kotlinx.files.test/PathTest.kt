package kotlinx.files.test

import kotlinx.files.*
import kotlin.test.*

class PathTest {
    private val fs = FileSystems.Default
    private val sep = fs.pathSeparator

    private fun path(path: String) = UnixPath(fs, path)
    
    @Test
    fun testEmptyPath() {
        val path = path("")
        checkSingleFileName(path, "")
    }

    @Test
    fun testWorkingDirectory() {
        val path = path(".")
        checkSingleFileName(path, ".")
    }

    @Test
    fun testParentDirectory() {
        val path = path("..")
        checkSingleFileName(path, "..")
    }

    @Test
    fun testRoot() {
        val path = path(sep)
        assertTrue(path.isAbsolute)
        assertNull(path.name)
        assertEquals(0, path.componentCount)
        assertEquals(sep, path.toString())
        assertNull(path.parent)
        assertFailsWith<IllegalArgumentException> { path.component(0) }
    }

    @Test
    fun testSingleFile() {
        val path = path("42.txt")
        checkSingleFileName(path, "42.txt")
    }

    @Test
    fun testPathConcatenation() {
        // Where is my parametrized :(

        checkConcatenation(path("a"), "b", "a${sep}b")
        checkConcatenation(path("a"), "${sep}b${sep}", "a${sep}b")
        checkConcatenation(path("foo${sep}..${sep}.."), "..${sep}..${sep}", "foo$sep..$sep..$sep..$sep..")
        checkConcatenation(path(""), sep, sep)
        checkConcatenation(path(sep), "", sep)
        checkConcatenation(path("."), "bar", ".${sep}bar")
        checkConcatenation(path("bar"), ".", "bar$sep.")
        checkConcatenation(path("foo.txt"), "foo.txt", "foo.txt${sep}foo.txt")
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
    fun testRelativePathSlashes() {
        checkSlashes("foo${sep}bar${sep}${sep}test${sep}${sep}${sep}file.txt")
        checkSlashes("foo${sep}bar${sep}${sep}test${sep}${sep}${sep}file.txt${sep}")
        checkSlashes("foo${sep}bar${sep}${sep}test${sep}file.txt${sep}${sep}")
    }

    @Test
    fun testAbsoluteRelativePathSlashes() {
        checkSlashes("${sep}foo${sep}bar${sep}${sep}test${sep}${sep}${sep}file.txt", sep)
        checkSlashes("${sep}foo${sep}bar${sep}${sep}test${sep}${sep}${sep}file.txt${sep}", sep)
        checkSlashes("${sep}foo${sep}bar${sep}${sep}test${sep}file.txt${sep}${sep}", sep)
    }

    private fun checkSlashes(forwardSlashPath: String, prefix: String = "") {
        val path = path(forwardSlashPath.replace(sep, sep))
    
        assertEquals("file.txt", path.name)
        assertEquals(4, path.componentCount)
        assertEquals(prefix + "foo${sep}bar${sep}test${sep}file.txt", path.toString())
        assertEquals(prefix + "foo${sep}bar${sep}test", path.parent!!.toString())

        assertEquals("foo", path.component(0).toString())
        assertEquals("bar", path.component(1).toString())
        assertEquals("test", path.component(2).toString())
        assertEquals("file.txt", path.component(3).toString())
    }


    @Test
    fun testDenormalizedPath() {
        val path = path("1${sep}2${sep}3${sep}..${sep}3${sep}..${sep}..${sep}2${sep}${sep}1.txt")
        assertEquals("1.txt", path.name)
        assertEquals(9, path.componentCount)
        assertEquals("1${sep}2${sep}3${sep}..${sep}3${sep}..${sep}..${sep}2${sep}1.txt", path.toString())
        assertEquals("1${sep}2${sep}3${sep}..${sep}3${sep}..${sep}..${sep}2", path.parent!!.toString())

        assertEquals("1", path.component(0).toString())
        assertEquals("2", path.component(1).toString())
        assertEquals("3", path.component(2).toString())
        assertEquals("..", path.component(3).toString())
    }

}
