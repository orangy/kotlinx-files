package kotlinx.files

actual fun createDefaultPlatformFileSystem(): FileSystem {
    return PosixFileSystem.Default
}