package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

class DeleteTest : TestBase() {

    @Test
    fun deleteFile() {
        val file = testFile("delete-file").createFile()
        assertTrue(file.exists(), "File should exist before delete")
        file.delete()
        assertFalse(file.exists(), "File should not exist after delete")

    }

    @Test
    fun deleteNonEmptyDirectory() {
        val directory = testFile("delete-directory").createDirectory()
        val file = (directory + "nested-file.txt").createFile()

        assertTrue(directory.exists(), "Directory should exist before delete")
        assertTrue(file.exists(), "File should exist before delete")

        assertFailsWith<IOException>("Can't delete non-empty directory") { directory.delete() }

        assertFalse(directory.deleteDirectoryIfExists(), "Shouldn't delete non-empty directory")
        assertTrue(directory.exists(), "Directory should still exist")
        assertTrue(file.exists(), "File should still exist")

        directory.fileSystem.deleteDirectoryRecursively(directory)
        assertFalse(directory.exists(), "Directory should be deleted by recursive operation")
        assertFalse(file.exists(), "File should be deleted by recursive operation")
    }

    @Test
    fun deleteNonExisting() {
        val file = testFile("non-existing")
        assertFalse(file.exists(), "File shouldn't exist")
        assertFalse(file.deleteFileIfExists(), "Non-existing file can't be deleted")
        assertFailsWith<IOException>("Can't delete non-existing file") { file.delete() }
    }
}
