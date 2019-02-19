package kotlinx.files

import platform.posix.*

actual fun compat_mkdir(path: String): Int = mkdir(path)
actual val posixPathSeparator = "\\"