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
        checkConcatenation(path("a"), "/b/", "a${sep}b")
        checkConcatenation(path("foo/../.."), "../../", "foo$sep..$sep..$sep..$sep..")
        checkConcatenation(path(""), "/", sep)
        checkConcatenation(path("/"), "", sep)
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
        assertEquals(expectedName, path.name.toString())
        assertEquals(1, path.componentCount)
        assertEquals(expectedName, path.toString())
        assertNull(path.parent)
        assertFailsWith<IllegalArgumentException> { path.component(1) }
    }

    @Test
    fun testRelativePathSlashes() {
        checkSlashes("foo/bar//test///file.txt")
        checkSlashes("foo/bar//test///file.txt/")
        checkSlashes("foo/bar//test/file.txt//")
    }

    @Test
    fun testAbsoluteRelativePathSlashes() {
        checkSlashes("/foo/bar//test///file.txt", "/")
        checkSlashes("/foo/bar//test///file.txt/", "/")
        checkSlashes("/foo/bar//test/file.txt//", "/")
    }

    private fun checkSlashes(forwardSlashPath: String, prefix: String = "") {
        val path = path(forwardSlashPath.replace("/", sep))
    
        assertEquals("file.txt", path.name.toString())
        assertEquals(4, path.componentCount)
        assertEquals(prefix + "foo/bar/test/file.txt", path.toString())
        assertEquals(prefix + "foo/bar/test", path.parent!!.toString())

        assertEquals("foo", path.component(0).toString())
        assertEquals("bar", path.component(1).toString())
        assertEquals("test", path.component(2).toString())
        assertEquals("file.txt", path.component(3).toString())
    }


    @Test
    fun testDenormalizedPath() {
        val path = path("1/2/3/../3/../../2//1.txt")
        assertEquals("1.txt", path.name.toString())
        assertEquals(9, path.componentCount)
        assertEquals("1/2/3/../3/../../2/1.txt", path.toString())
        assertEquals("1/2/3/../3/../../2", path.parent!!.toString())

        assertEquals("1", path.component(0).toString())
        assertEquals("2", path.component(1).toString())
        assertEquals("3", path.component(2).toString())
        assertEquals("..", path.component(3).toString())
    }

}
