package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

@Ignore
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
        assertTrue(expectedContent.contentEquals(sourceBytes), "Source content mismatch: ${expectedContent.contentToString()}\n${sourceBytes.contentToString()}")
        assertTrue(expectedContent.contentEquals(targetBytes), "Target content mismatch: ${expectedContent.contentToString()}\n${targetBytes.contentToString()}")
        assertTrue(sourceFile.exists())
    }

    @Test
    fun testCopyDirectory() {
        val directory = testDirectory("copy-directory").createDirectory()
        val target = testDirectory("target-directory")
        assertFalse(target.exists())

        directory.copyTo(target)
        assertTrue(directory.exists())
        assertTrue(target.exists())
    }

    @Test
    fun testCopyDirectoryWithContent() {
        val expectedContent = ByteArray(42) { it.toByte() }
        val directory = testDirectory("copy-directory-with-content").createDirectory()
        val file = Path(directory, "content.txt").createFile()
        file.writeBytes(expectedContent)

        val target = testDirectory("target-directory")
        val targetFile = Path(target, "content.txt")
        assertFalse(target.exists())
        assertFalse(targetFile.exists())

        directory.fileSystem.copyDirectoryRecursively(directory, target)

        // Content is not copied, but directory is
        assertTrue(target.exists())
        assertTrue(targetFile.exists())
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
