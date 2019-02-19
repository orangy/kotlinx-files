package kotlinx.files

interface FileSystem {
    // Read operations
    fun path(name: String, vararg children: String): Path
    fun exists(path: Path): Boolean

    fun openInput(path: Path): FileInput
    fun openDirectory(path: Path): Directory

    // Write operations
    val isReadOnly : Boolean
    val pathSeparator: String

    fun openOutput(path: Path): FileOutput

    fun createFile(path: Path): Path
    fun createDirectory(path: Path): Path

    fun move(source: Path, target: Path): Path
    fun copy(source: Path, target: Path): Path
    fun delete(path: Path): Boolean

    fun isDirectory(path: Path): Boolean
    fun isFile(path: Path): Boolean
}
