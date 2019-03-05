package kotlinx.files

actual fun createPlatformFileSystem(): FileSystem {
    return PosixFileSystem.Default
}