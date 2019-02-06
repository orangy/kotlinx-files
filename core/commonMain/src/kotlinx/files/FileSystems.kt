package kotlinx.files

object FileSystems {
    val Default = createDefaultPlatformFileSystem()
}

expect fun createDefaultPlatformFileSystem(): FileSystem 

