package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

class FileOperationsTest : TestBase() {

    @Test
    fun testCreateFile() {
        val path = testFile("create-file")
        assertFalse(path.exists(), "File shouldn't exist")
        assertFalse(path.isFile,  "isFile should be false")
        assertFalse(path.isDirectory,  "isDirectory should be false")
        path.createFile()
        assertTrue(path.exists(), "File should be created")
        assertTrue(path.isFile,  "isFile should be true")
        assertFalse(path.isDirectory,  "isDirectory should be false")
    }

    @Test
    fun testDeleteFile() {
        val path = testFile("delete-file").createFile()
        assertTrue(path.exists(), "File should exist")
        assertTrue(path.deleteFileIfExists(), "File should be deleted")
        assertFalse(path.exists(), "File shouldn't exist")
    }

    @Test
    fun testDeleteMissingFile() {
        val path = testFile("non-missing")
        assertFalse(path.exists(), "File shouldn't exist")
        assertFalse(path.deleteFileIfExists(), "Shouldn't delete non-existing file")
        assertFailsWith<IOException>("Can't delete non-existing file") { path.delete() }
    }
}
