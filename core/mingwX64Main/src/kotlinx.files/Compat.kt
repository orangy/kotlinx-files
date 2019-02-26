package kotlinx.files

import platform.posix.*

actual fun compat_mkdir(path: String): Int = mkdir(path)
actual val posixPathSeparator = "\\"
actual val O_BINARY : Int = platform.posix.O_BINARY // Binary mode on Windows
