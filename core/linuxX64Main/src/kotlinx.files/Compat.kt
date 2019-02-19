package kotlinx.files

import platform.posix.*

actual fun compat_mkdir(path: String): Int = mkdir(path, 0x1FF) // 0x1FF hex == 511 == 0777 oct
actual val posixPathSeparator = "/"