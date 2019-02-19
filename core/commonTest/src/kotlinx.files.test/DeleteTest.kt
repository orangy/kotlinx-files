package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

class DeleteTest : TestBase() {

    @Test
    fun deleteFile() {
        val file = testFile("delete-file").createFile()
        assertTrue(file.exists())
        file.delete()
        assertFalse(file.exists())

    }

    @Test
    fun deleteNonEmptyDirectory() {
        val directory = testFile("delete-directory").createDirectory()
        val file = (directory + "nested-file.txt").createFile()

        assertTrue(directory.exists())
        assertTrue(file.exists())

        // TODO: Change assertion to IOException when PosixException implements it
        assertFailsWith<Exception> { directory.delete() }

        assertFalse(directory.deleteDirectoryIfExists())
        assertTrue(directory.exists())
        assertTrue(file.exists())

        directory.fileSystem.deleteDirectoryRecursively(directory)
        assertFalse(directory.exists())
        assertFalse(file.exists())
    }

    @Test
    fun deleteNonExisting() {
        val file = testFile("non-existing")
        assertFalse(file.exists())
        assertFalse(file.deleteFileIfExists())
        assertFailsWith<IOException> { file.delete() }
    }
}
