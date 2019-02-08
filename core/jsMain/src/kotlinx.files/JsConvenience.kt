package kotlinx.files

actual fun createDefaultPlatformFileSystem(): FileSystem {
    return JsFileSystem.Default
}