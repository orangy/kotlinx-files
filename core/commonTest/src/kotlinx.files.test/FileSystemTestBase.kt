package kotlinx.files.test

import kotlinx.files.*
import kotlin.test.*

abstract class FileSystemTestBase(val fileSystem: FileSystem) {
    protected val separator get() = fileSystem.pathSeparator

    protected val testFolderPath = fileSystem.path("build", "testFolder")
    protected val testFolder: Path = testFolderPath.also {
        if (!it.exists())
            fileSystem.createAllDirectories(testFolderPath)
    }

    @AfterTest
    fun tearDown() {
        fileSystem.deleteDirectoryRecursively(testFolderPath)
    }

    fun testPath(vararg path: String): Path = fileSystem.path(testFolder, *path)
}


