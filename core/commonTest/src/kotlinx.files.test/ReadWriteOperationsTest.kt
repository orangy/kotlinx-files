package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

class ReadWriteOperationsTest : TestBase() {

    @Test
    fun testWriteRead() {
        val path = testFile("write-read").createFile()
        val content = ByteArray(255) { it.toByte() }
        path.writeBytes(content)

        val expected = content.copyOf()
        val actual = path.readBytes()

        assertTrue(
            actual.contentEquals(expected),
            "Expected ${expected.contentToString()}, but has ${actual.contentToString()}"
        )
    }
    
    @Test
    fun testWriteReadLong() {
        val path = testFile("write-read").createFile()
        val content = ByteArray(10_000) { it.toByte() }
        path.writeBytes(content)

        val expected = content.copyOf()
        val actual = path.readBytes()

        assertTrue(
            actual.contentEquals(expected),
            "Expected ${expected.contentToString()}, but has ${actual.contentToString()}"
        )
    }

    @Test
    fun testWriteNonExistent() {
        val path = testFile("write-non-existent")
        val content = ByteArray(255) { it.toByte() }
        path.writeBytes(content)

        val actual = path.readBytes()
        assertTrue(
            actual.contentEquals(content),
            "Expected ${content.contentToString()}, but has ${actual.contentToString()}"
        )

        assertTrue(path.exists())
        assertTrue(path.isFile)
        assertFalse(path.isDirectory)
    }

    @Test
    fun testWriteEmpty() {
        val path = testFile("write-empty").createFile()
        path.writeBytes(byteArrayOf())

        val actual = path.readBytes()
        assertTrue(actual.isEmpty())
    }

    @Test
    fun testReadNonExistent() {
        assertFailsWith<IOException> {
            val path = testFile("read-non-existent")
            val actual = path.readBytes()
            assertTrue(actual.isEmpty())
        }
    }

    @Test
    fun testReadEmpty() {
        val path = testFile("read-empty").createFile()
        val actual = path.readBytes()
        assertTrue(actual.isEmpty())
    }

    @Test
    fun testReadFromDirectory() {
        val path = testDirectory("read-from-directory").createDirectory()
        assertTrue(path.isDirectory)

        assertFailsWith<IOException> { path.readBytes() }
        assertTrue(path.isDirectory)
    }

    @Test
    fun testWriteToDirectory() {
        val path = testDirectory("write-to-directory").createDirectory()
        assertTrue(path.isDirectory)

        assertFailsWith<IOException> { path.writeBytes(byteArrayOf()) }
        assertTrue(path.isDirectory)
    }
}
