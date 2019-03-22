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

fun assertTimeMakesSense(timestampUs: Long) {
    // Yay, common time module!
    val seconds = timestampUs / 1e6.toLong()
    // in now()..now() * 10 ~~ 2018/5/10..2453/07/23
    assertTrue(seconds in 1525968223..15259682219, "Time $timestampUs doesn't make any sense")
}

