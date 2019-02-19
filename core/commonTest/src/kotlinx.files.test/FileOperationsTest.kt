package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

class FileOperationsTest : TestBase() {

    @Test
    fun testExists() {
        val path = testFile("exists")
        assertFalse(path.exists())
    }

    @Test
    fun testCreateFile() {
        val path = testFile("create-file")
        assertFalse(path.exists())
        path.createFile()
        assertTrue(path.exists())
        assertTrue(path.isFile)
    }

    @Test
    fun testDeleteFile() {
        val path = testFile("delete-file")
        val file = path.createFile()
        assertTrue(path.exists())

        assertTrue(file.deleteFileIfExists())
        assertFalse(path.exists())
    }

    @Test
    fun testDeleteMissingFile() {
        assertFailsWith<IOException> {
            val path = testFile("non-missing")
            assertTrue(!path.deleteFileIfExists())
            path.delete()
        }
    }

    @Test
    fun testIsDirectory() {
        val path = testFile("is-directory").createFile()
        assertFalse(path.isDirectory)
    }

    @Test
    fun testIsRegularFile() {
        val path = testFile("is-regular-file").createFile()
        assertTrue(path.isFile)
    }

    @Test
    fun testIsRegularFileNonExistent() {
        val path = testFile("is-regular-file-non-existent")
        assertFalse(path.isFile)
    }
}
