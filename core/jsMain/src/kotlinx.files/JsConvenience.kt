package kotlinx.files

actual fun createPlatformFileSystem(): FileSystem {
    return JsFileSystem.Default
}