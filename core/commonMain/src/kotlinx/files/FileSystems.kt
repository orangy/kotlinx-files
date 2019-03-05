package kotlinx.files

/**
 * Provides access to instances of [FileSystem] 
 */
object FileSystems {
    /**
     * Provides a default instance of a file system for the current platform.
     */
    val Default : FileSystem = createPlatformFileSystem()
}

/**
 * Creates a new [FileSystem] instance for the current platform.
 * 
 * Normally [FileSystems.Default] should be used, different instances of same file system are semantically the same, but cannot
 * accept [Path] instance from each other. 
 */
expect fun createPlatformFileSystem(): FileSystem 

