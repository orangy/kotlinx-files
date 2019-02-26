package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

class DirectoriesOperationsTest : TestBase() {

    @Test
    fun testCreateDirectory() {
        val path = testDirectory("create-directory")
        assertFalse(path.exists(), "Directory shouldn't exist")
        assertFalse(path.isDirectory, "isDirectory should be false")
        assertFalse(path.isFile, "isFile should be false")
        path.createDirectory()
        assertTrue(path.exists(), "Directory should be created")
        assertTrue(path.isDirectory, "isDirectory should be true")
        assertFalse(path.isFile, "isFile should be false")
    }
    
    @Test
    fun testDelete() {
        val path = testDirectory("delete").createDirectory()
        assertTrue(path.exists(), "Directory should exist")
        path.delete()
        assertFalse(path.isDirectory, "isDirectory should be false after delete")
        assertFalse(path.exists(), "Directory shouldn't exist")
    }

    @Test
    fun testMkdirs() {
        assertFailsWith<IOException> { testDirectory("mkdirs${separator}mkdirs2${separator}mkdirs3").createDirectory() }
    }
}
