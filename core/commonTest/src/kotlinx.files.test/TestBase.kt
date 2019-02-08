package kotlinx.files.test

import kotlinx.files.*
import kotlin.test.*

open class TestBase {
    private val testFolderPath = Path("build/testFolder")
    private val testFolder: Path = testFolderPath.also {
        if (!it.exists())
            FileSystems.Default.createDirectory(testFolderPath)
    }

    @AfterTest
    fun tearDown() {
        testFolder.deleteDirectoryIfExists()
    }

    fun testFile(path: String): Path = Path(testFolder.toString(), "$path.txt")
    fun testDirectory(path: String): Path = Path(testFolder.toString(), path)
}
