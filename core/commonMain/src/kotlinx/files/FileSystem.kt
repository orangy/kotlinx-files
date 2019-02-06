package kotlinx.files

interface FileSystem {
    // Read operations
    fun path(name: String, vararg children: String): Path
    fun exists(path: Path): Boolean

    fun openInput(path: Path): FileInput
    fun openDirectory(path: Path): Directory

    // Write operations
    val isReadOnly : Boolean
    fun openOutput(path: Path): FileOutput

    fun createFile(path: Path): Path
    fun deleteFile(path: Path): Boolean

    fun createDirectory(path: Path): Path
    fun deleteDirectory(path: Path): Boolean
    
    fun move(source: Path, target: Path): Path
    fun copy(source: Path, target: Path): Path
    
    fun isDirectory(path: Path): Boolean
    fun isFile(path: Path): Boolean
}
