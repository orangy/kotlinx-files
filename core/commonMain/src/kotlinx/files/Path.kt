package kotlinx.files

interface Path {
    val fileSystem: FileSystem
    val parent : Path?
    val name: Path?
    
    val isAbsolute : Boolean
    
    val componentCount : Int
    fun component(index: Int) : Path
}

