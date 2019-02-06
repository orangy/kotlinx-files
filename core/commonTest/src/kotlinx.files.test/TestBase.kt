package kotlinx.files.test

import kotlinx.files.*
import kotlin.test.*

open class TestBase {
    private val testFolder: Path = FileSystems.Default.createDirectory(Path("build/testFolder"))

    @AfterTest
    fun tearDown() {
        testFolder.deleteDirectoryIfExists()
    }

    fun testFile(path: String): Path = Path(testFolder.toString(), "$path.txt")
    fun testDirectory(path: String): Path = Path(testFolder.toString(), path)
}
