package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

class CopyTest : TestBase() {

    @Test
    fun testCopyFile() {
        val expectedContent = ByteArray(5) { it.toByte() }
        val sourceFile = testFile("copy-file").createFile()
        sourceFile.writeBytes(expectedContent)

        val targetFile = testFile("target-file")
        sourceFile.copyTo(targetFile)

        val sourceBytes = sourceFile.readBytes()
        val targetBytes = targetFile.readBytes()
        assertEquals(expectedContent.contentToString(), sourceBytes.contentToString())
        assertEquals(expectedContent.contentToString(), targetBytes.contentToString())
        assertTrue(sourceFile.exists(), "Source file should exist after copy")
    }

    @Test
    fun testCopyDirectory() {
        val source = testDirectory("copy-directory").createDirectory()
        val target = testDirectory("target-directory")
        assertFalse(target.exists())

        source.copyTo(target)
        assertTrue(source.exists(), "Source directory should exist after copy")
        assertTrue(target.exists(), "Target directory should exist after copy")
    }

    @Test
    fun testCopyDirectoryWithContent() {
        val expectedContent = ByteArray(42) { it.toByte() }
        val directory = testDirectory("copy-directory-with-content").createDirectory()
        val file = Path(directory, "content.txt").createFile()
        file.writeBytes(expectedContent)

        val target = testDirectory("target-directory")
        val targetFile = Path(target, "content.txt")
        assertFalse(target.exists(), "Target directory should not exist before copy")
        assertFalse(targetFile.exists(), "Target file should not exist before copy")

        directory.fileSystem.copyDirectoryRecursively(directory, target)

        assertTrue(target.exists(), "Target directory should exist after copy")
        assertTrue(targetFile.exists(), "Target file should exist after copy")
    }

    @Test
    fun testCopyNothing() {
        val file = testFile("nothing")
        val target = testFile("nothing-target")
        assertFailsWith<IOException> { file.copyTo(target) }
    }

    @Test
    fun testCopyToExistingFile() {
        val file = testFile("file").createFile()
        val target = testFile("existing-file").createFile()
        assertFailsWith<IOException> { file.copyTo(target) }
    }
}
