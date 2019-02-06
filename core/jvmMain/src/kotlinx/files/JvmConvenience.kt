package kotlinx.files

actual fun createDefaultPlatformFileSystem(): FileSystem {
    return JvmFileSystem.Default
}