package kotlinx.files.test

import kotlinx.files.*
import kotlin.test.*

open class TestBase {
    protected val slash = FileSystems.Default.pathSeparator
    private val testFolderPath = Path("build${slash}testFolder")
    private val testFolder: Path = testFolderPath.also {
        if (!it.exists())
            FileSystems.Default.createDirectory(testFolderPath)
    }

    @AfterTest
    fun tearDown() {
        FileSystems.Default.deleteDirectoryRecursively(testFolderPath)
    }

    fun testFile(path: String): Path = Path(testFolder.toString(), "$path.txt")
    fun testDirectory(path: String): Path = Path(testFolder.toString(), path)
}
