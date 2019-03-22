package kotlinx.files.test

import kotlinx.files.*
import kotlinx.files.platform.*
import kotlinx.io.core.*
import kotlinx.io.errors.*
import kotlin.test.*

abstract class StandardFileSystemTest(fileSystem: FileSystem) : FileSystemTestBase(fileSystem) {
    @Test
    fun attributesFile() {
        val file = testPath("file-attributes").createFile()
        val attributes = file.readAttributes<FileAttributes>()
        assertTrue(attributes.isFile)
        assertFalse(attributes.isDirectory)
        assertFalse(attributes.isSymbolicLink)
        assertEquals(0L, attributes.size)

        assertTimeMakesSense(attributes.creationTimeUs)
        assertTimeMakesSense(attributes.lastAccessTimeUs)
        assertTimeMakesSense(attributes.lastModifiedTimeUs)

        file.writeBytes(ByteArray(42) { it.toByte() })
        val attributes2 = file.readAttributes<FileAttributes>()
        assertEquals(42, attributes2.size)


        // TODO: somehow check it for MemFS, but not NTFS? 
        // For MemFS make sure to wait at least microsecond for a timer to tick before writing data. 
        // https://docs.microsoft.com/en-us/windows/desktop/sysinfo/file-times
        // The NTFS file system delays updates to the last access time for a file by up to 1 hour after the last access.
/*
        assertTrue(
            attributes2.lastModifiedTimeUs > attributes.lastModifiedTimeUs,
            "Last modified time should be touched: ${attributes2.lastModifiedTimeUs} > ${attributes.lastModifiedTimeUs}"
        )

        assertTrue(
            attributes2.lastAccessTimeUs > attributes.lastAccessTimeUs,
            "Last access time should be touched: ${attributes2.lastAccessTimeUs} > ${attributes.lastAccessTimeUs}"
        )
        assertEquals(attributes2.creationTimeUs, attributes.creationTimeUs, "Creation time should not be touched")
*/
    }

    @Test
    fun attributesDirectory() {
        val directory = testPath("directory-attributes").createDirectory()
        val attributes = directory.readAttributes<FileAttributes>()
        assertFalse(attributes.isFile)
        assertTrue(attributes.isDirectory)
        assertFalse(attributes.isSymbolicLink)
    }

    @Test
    fun attributesMissing() {
        val path = testPath("missing-attributes")
        assertFailsWith<IOException> { path.readAttributes<FileAttributes>() }
    }

    // This test fails on Windows
    // On JVM because PosixFileAttributes is not supported
    // On JS & Native because permissions are different
    // TODO: Design how to specify different assertions on different OSes
    @Suppress("SpellCheckingInspection")
    @Test
    @Ignore
    fun attributesPosixPermissions() {
        val file = this.testPath("posix-permissions-file").createFile()
        assertEquals(
            "rw-r--r--",
            file.readAttributes<PosixFileAttributes>().permissions.toHumanReadableString(),
            "File attributes"
        )

        val directory = this.testPath("posix-permissions-directory").createDirectory()
        assertEquals(
            "rwxr-xr-x",
            directory.readAttributes<PosixFileAttributes>().permissions.toHumanReadableString(),
            "Directory attributes"
        )

        val root = fileSystem.path("/")
        assertEquals(
            "rwxr-xr-x",
            root.readAttributes<PosixFileAttributes>().permissions.toHumanReadableString(),
            "Root attributes"
        )
    }


    @Test
    fun contentWriteRead() {
        val path = this.testPath("write-read").createFile()
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
    fun contentWriteFlushRead() {
        val path = this.testPath("write-read").createFile()
        val content = ByteArray(255) { it.toByte() }
        path.openOutput().use {
            it.writeFully(content)
            it.flush()
            it.writeFully(content)
        }

        val expected = (content + content).copyOf()
        val actual = path.readBytes()

        assertTrue(
            actual.contentEquals(expected),
            "Expected ${expected.contentToString()}, but has ${actual.contentToString()}"
        )
    }

    @Test
    fun contentWriteReadLong() {
        val path = this.testPath("write-read").createFile()
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
    fun contentWriteNonExistent() {
        val path = this.testPath("write-non-existent")
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
    fun contentWriteEmpty() {
        val path = this.testPath("write-empty").createFile()
        path.writeBytes(byteArrayOf())

        val actual = path.readBytes()
        assertTrue(actual.isEmpty())
    }

    @Test
    fun contentReadNonExistent() {
        assertFailsWith<IOException> {
            val path = this.testPath("read-non-existent")
            val actual = path.readBytes()
            assertTrue(actual.isEmpty())
        }
    }

    @Test
    fun contentReadEmpty() {
        val path = this.testPath("read-empty").createFile()
        val actual = path.readBytes()
        assertTrue(actual.isEmpty())
    }

    @Test
    fun contentReadFromDirectory() {
        val path = testPath("read-from-directory").createDirectory()
        assertTrue(path.isDirectory)

        assertFailsWith<IOException> { path.readBytes() }
        assertTrue(path.isDirectory)
    }

    @Test
    fun contentWriteToDirectory() {
        val path = testPath("write-to-directory").createDirectory()
        assertTrue(path.isDirectory)

        assertFailsWith<IOException> { path.writeBytes(byteArrayOf()) }
        assertTrue(path.isDirectory)
    }


    @Test
    fun copyFile() {
        val expectedContent = ByteArray(5) { it.toByte() }
        val sourceFile = this.testPath("copy-file").createFile()
        sourceFile.writeBytes(expectedContent)

        val targetFile = this.testPath("target-file")
        sourceFile.copyTo(targetFile)

        val sourceBytes = sourceFile.readBytes()
        val targetBytes = targetFile.readBytes()
        assertEquals(expectedContent.contentToString(), sourceBytes.contentToString())
        assertEquals(expectedContent.contentToString(), targetBytes.contentToString())
        assertTrue(sourceFile.exists(), "Source file should exist after copy")
    }

    @Test
    fun copyDirectory() {
        val source = testPath("copy-directory").createDirectory()
        val target = testPath("target-directory")
        assertFalse(target.exists())

        source.copyTo(target)
        assertTrue(source.exists(), "Source directory should exist after copy")
        assertTrue(target.exists(), "Target directory should exist after copy")
    }

    @Test
    fun copyDirectoryWithContent() {
        val expectedContent = ByteArray(42) { it.toByte() }
        val directory = testPath("copy-directory-with-content").createDirectory()
        val file = directory.resolve("content.txt").createFile()
        file.writeBytes(expectedContent)

        val target = testPath("target-directory")
        val targetFile = target.resolve("content.txt")
        assertFalse(target.exists(), "Target directory should not exist before copy")
        assertFalse(targetFile.exists(), "Target file should not exist before copy")

        directory.fileSystem.copyDirectoryRecursively(directory, target)

        assertTrue(target.exists(), "Target directory should exist after copy")
        assertTrue(targetFile.exists(), "Target file should exist after copy")
    }

    @Test
    fun copyNothing() {
        val file = this.testPath("nothing")
        val target = this.testPath("nothing-target")
        assertFailsWith<IOException> { file.copyTo(target) }
    }

    @Test
    fun copyToExistingFile() {
        val file = this.testPath("file").createFile()
        val target = this.testPath("existing-file").createFile()
        assertFailsWith<IOException> { file.copyTo(target) }
    }


    @Test
    fun deleteFile() {
        val path = this.testPath("delete-file").createFile()
        assertTrue(path.exists(), "File should exist before delete")
        assertTrue(path.deleteIfExists(), "File should be deleted")
        assertFalse(path.exists(), "File should not exist after delete")
    }

    @Test
    fun deleteDirectory() {
        val path = testPath("delete").createDirectory()
        assertTrue(path.exists(), "Directory should exist")
        path.delete()
        assertFalse(path.isDirectory, "isDirectory should be false after delete")
        assertFalse(path.exists(), "Directory shouldn't exist")
    }

    @Test
    fun deleteDirectoryNonEmpty() {
        val directory = this.testPath("delete-directory").createDirectory()
        val file = (directory + "nested-file.txt").createFile()

        assertTrue(directory.exists(), "Directory should exist before delete")
        assertTrue(file.exists(), "File should exist before delete")

        assertFailsWith<IOException>("Can't delete non-empty directory") { directory.delete() }

        assertFalse(directory.deleteIfExists(), "Shouldn't delete non-empty directory")
        assertTrue(directory.exists(), "Directory should still exist")
        assertTrue(file.exists(), "File should still exist")

        directory.fileSystem.deleteDirectoryRecursively(directory)
        assertFalse(directory.exists(), "Directory should be deleted by recursive operation")
        assertFalse(file.exists(), "File should be deleted by recursive operation")
    }

    @Test
    fun deleteNonExisting() {
        val file = this.testPath("non-existing")
        assertFalse(file.exists(), "File shouldn't exist")
        assertFalse(file.deleteIfExists(), "Non-existing file can't be deleted")
        assertFailsWith<IOException>("Can't delete non-existing file") { file.delete() }
    }

    @Test
    fun createDirectory() {
        val path = testPath("create-directory")
        assertFalse(path.exists(), "Directory shouldn't exist")
        assertFalse(path.isDirectory, "isDirectory should be false")
        assertFalse(path.isFile, "isFile should be false")
        path.createDirectory()
        assertTrue(path.exists(), "Directory should be created")
        assertTrue(path.isDirectory, "isDirectory should be true")
        assertFalse(path.isFile, "isFile should be false")
    }

    @Test
    fun createNestedDirectory() {
        assertFailsWith<IOException> { testPath("mkdirs", "mkdirs2", "mkdirs3").createDirectory() }
    }

    @Test
    fun createAllDirectories() {
        val parent = testPath("mkdirs")
        assertFalse(parent.exists(), "Directory shouldn't exist")
        assertFalse(parent.isDirectory, "isDirectory should be false")
        assertFalse(parent.isFile, "isFile should be false")

        val path = parent.resolve("mkdirs2", "mkdirs3").createAllDirectories()

        assertTrue(path.exists(), "Directory should be created")
        assertTrue(path.isDirectory, "isDirectory should be true")
        assertFalse(path.isFile, "isFile should be false")
        assertTrue(parent.exists(), "Directory should be created")
        assertTrue(parent.isDirectory, "isDirectory should be true")
        assertFalse(parent.isFile, "isFile should be false")
    }

    @Test
    fun directoryListingNonExistent() {
        val path = testPath("non-existent-listing")
        assertFailsWith<IOException> { path.listFiles() }
    }

    @Test
    fun directoryListingEmpty() {
        val directory = testPath("empty-listing").createDirectory()
        assertTrue(directory.listFiles().isEmpty())
    }

    @Test
    fun directoryListing() {
        val directory = testPath("listing").createDirectory()
        directory.resolve("1.txt").createFile()
        directory.resolve("2").createDirectory()

        val expected = listOf(this.testPath("listing", "1.txt"), testPath("listing", "2")).toSet()
        val actual = directory.listFiles().toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun directoryListingNested() {
        val directory = testPath("nested-listing").createDirectory()
        directory.resolve("1.txt").createFile()
        val nested = directory.resolve("2").createDirectory()
        nested.resolve("3.txt").createFile()

        val expected =
            listOf(this.testPath("nested-listing", "1.txt"), testPath("nested-listing", "2")).toSet()
        assertEquals(expected, directory.listFiles().toSet())
    }


    @Test
    fun createFile() {
        val path = this.testPath("create-file")
        assertFalse(path.exists(), "File shouldn't exist")
        assertFalse(path.isFile, "isFile should be false")
        assertFalse(path.isDirectory, "isDirectory should be false")
        path.createFile()
        assertTrue(path.exists(), "File should be created")
        assertTrue(path.isFile, "isFile should be true")
        assertFalse(path.isDirectory, "isDirectory should be false")
    }

    @Test
    fun moveFile() {
        val expectedContent = ByteArray(5) { it.toByte() }
        val file = this.testPath("move-file").createFile()
        file.writeBytes(expectedContent)

        val target = this.testPath("target-file")
        assertFalse(target.exists())
        file.moveTo(target)

        assertTrue(target.exists())
        assertTrue(expectedContent.contentEquals(target.readBytes()))
        assertFalse(file.exists())
    }

    @Test
    fun moveDirectory() {
        val directory = testPath("move-directory").createDirectory()
        val target = testPath("target-directory")
        assertFalse(target.exists())

        directory.moveTo(target)
        assertFalse(directory.exists())
        assertTrue(target.exists())
    }

    @Test
    fun moveDirectoryWithContent() {
        val expectedContent = ByteArray(42) { it.toByte() }
        val directory = testPath("move-directory-with-content").createDirectory()
        val file = directory.resolve("content.txt").createFile()
        file.writeBytes(expectedContent)

        val target = testPath("target-directory")
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
    fun moveNothing() {
        val file = this.testPath("nothing")
        val target = this.testPath("nothing-target")
        assertFailsWith<IOException> { file.moveTo(target) }
    }

    @Test
    fun moveToExistingFile() {
        val file = this.testPath("file").createFile()
        val target = this.testPath("existing-file").createFile()
        assertFailsWith<IOException> { file.moveTo(target) }
    }
}

fun assertTimeMakesSense(timestampUs: Long) {
    // Yay, common time module!
    val seconds = timestampUs / 1e6.toLong()
    // in now()..now() * 10 ~~ 2018/5/10..2453/07/23
    assertTrue(seconds in 1525968223..15259682219, "Time $timestampUs doesn't make any sense")
}
