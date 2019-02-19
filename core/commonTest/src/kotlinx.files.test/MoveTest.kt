package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

class MoveTest : TestBase() {
    @Test
    fun testMoveFile() {
        val expectedContent = ByteArray(5) { it.toByte() }
        val file = testFile("move-file").createFile()
        file.writeBytes(expectedContent)

        val target = testFile("target-file")
        assertFalse(target.exists())
        file.moveTo(target)

        assertTrue(target.exists())
        assertTrue(expectedContent.contentEquals(target.readBytes()))
        assertFalse(file.exists())
    }

    @Test
    fun testMoveDirectory() {
        val directory = testDirectory("move-directory").createDirectory()
        val target = testDirectory("target-directory")
        assertFalse(target.exists())

        directory.moveTo(target)
        assertFalse(directory.exists())
        assertTrue(target.exists())
    }

    @Test
    fun testMoveDirectoryWithContent() {
        val expectedContent = ByteArray(42) { it.toByte() }
        val directory = testDirectory("move-directory-with-content").createDirectory()
        val file = Path(directory, "content.txt").createFile()
        file.writeBytes(expectedContent)

        val target = testDirectory("target-directory")
        val targetFile = target + "content.txt"
        assertFalse(target.exists())
        assertFalse(targetFile.exists())

        directory.moveTo(target)

        assertTrue(target.exists())
        assertTrue(targetFile.exists())
        assertTrue(expectedContent.contentEquals(targetFile.readBytes()))
        assertFalse(directory.exists())
    }

    @Test
    fun testMoveNothing() {
        val file = testFile("nothing")
        val target = testFile("nothing-target")
        assertFailsWith<IOException> { file.moveTo(target) }
    }

    @Test
    fun testMoveToExistingFile() {
        val file = testFile("file").createFile()
        val target = testFile("existing-file").createFile()
        assertFailsWith<IOException> { file.moveTo(target) }
    }
}
