package kotlinx.files.test

import kotlinx.files.*
import kotlinx.io.errors.*
import kotlin.test.*

class AttributesTest : TestBase() {

    @Test
    fun testFileAttributes() {
        val file = testFile("file-attributes").createFile()
        val attributes = file.readAttributes<FileAttributes>()
        assertTrue(attributes.isFile)
        assertFalse(attributes.isDirectory)
        assertFalse(attributes.isSymbolicLink)
        assertEquals(0L, attributes.size)

        checkTime(attributes.creationTimeUs)
        checkTime(attributes.lastAccessTimeUs)
        checkTime(attributes.lastModifiedTimeUs)

        file.writeBytes(ByteArray(42) { it.toByte() })
        val attributes2 = file.readAttributes<FileAttributes>()
        assertEquals(42, attributes2.size) // FS-dependent
    }

    private fun checkTime(timestampUs: Long) {
        // Yay, common time module!
        val seconds = timestampUs / 1e6.toLong()
        // in now()..now() * 10 ~~ 2018/5/10..2453/07/23
        assertTrue(seconds in 1525968223..15259682219)
    }

    @Test
    fun testDirectoryAttributes() {
        val directory = testFile("directory-attributes").createDirectory()
        val attributes = directory.readAttributes<FileAttributes>()
        assertFalse(attributes.isFile)
        assertTrue(attributes.isDirectory)
        assertFalse(attributes.isSymbolicLink)
    }

    @Test
    fun testMissingAttributes() {
        val path = testFile("missing-attributes")
        assertFailsWith<IOException> { path.readAttributes<FileAttributes>() }
    }

    // This test fails on Windows
    // On JVM because PosixFileAttributes is not supported
    // On JS & Native because permissions are different
    // TODO: Design how to specify different assertions on different OSes
    @Test @Ignore
    fun testPosixPermissions() {
        val file = testFile("posix-permissions-file").createFile()
        assertEquals("rw-r--r--", file.readAttributes<PosixFileAttributes>().permissions.toHumanReadableString(), "File attributes")

        val directory = testFile("posix-permissions-directory").createDirectory()
        assertEquals("rwxr-xr-x", directory.readAttributes<PosixFileAttributes>().permissions.toHumanReadableString(), "Directory attributes")

        val root = Path("/")
        assertEquals("rwxr-xr-x", root.readAttributes<PosixFileAttributes>().permissions.toHumanReadableString(), "Root attributes")
    }
}
