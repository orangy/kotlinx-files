package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

class DirectoriesOperationsTest : TestBase() {

    @Test
    fun testCreateDirectory() {
        val path = testDirectory("create-directory")
        assertFalse(path.exists())
        path.createDirectory()
        assertTrue(path.exists())
    }

    @Test
    fun testIsDirectory() {
        val path = testDirectory("is-directory").createDirectory()
        assertTrue(path.exists())
        assertTrue(path.isDirectory)
    }

    @Test
    fun testDelete() {
        val path = testDirectory("delete").createDirectory()
        assertTrue(path.exists())
        path.delete()
        assertFalse(path.isDirectory)
        assertFalse(path.exists())
    }

    @Test
    fun testIsDirectoryMissing() {
        val path = testDirectory("is-directory-missing")
        assertFalse(path.exists())
        assertFalse(path.isDirectory)
    }

    @Test
    fun testIsFile() {
        val path = testDirectory("is-file").createDirectory()
        assertTrue(path.exists())
        assertFalse(path.isFile)
    }

    @Test
    fun testMkdirs() {
        assertFailsWith<IOException> { testDirectory("mkdirs${separator}mkdirs2${separator}mkdirs3").createDirectory() }
    }
}
