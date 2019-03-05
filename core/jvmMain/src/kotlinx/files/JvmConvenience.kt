package kotlinx.files

actual fun createPlatformFileSystem(): FileSystem {
    return JvmFileSystem.Default
}