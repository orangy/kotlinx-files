package kotlinx.files

/**
 * Represents a location in the specific [fileSystem]
 */
interface Path {
    /**
     * Instance of [FileSystem] this `Path` belongs to.
     */
    val fileSystem: FileSystem

    /**
     * [Path] instance of the parent directory in the file system hierarchy, or null if there is no parent specified. 
     */
    val parent : Path?

    /**
     * Text representation of the last component of this path, or null if the path has no components.
     */
    val name: String?

    /**
     * Checks if this path is an absolute, i.e. starts from the file system root.
     * @returns true if this path is absolute, false otherwise. 
     */
    val isAbsolute : Boolean

    /**
     * Returns number of components (directories and files) in this path.
     */
    val componentCount : Int
    
    /**
     * Retrieves a component at [index] from this path.
     * 
     * Index should be non-negative and less than [componentCount]
     */
    fun component(index: Int) : String
}

