package kotlinx.files

import kotlinx.cinterop.*
import platform.posix.*

actual fun compat_mkdir(path: String): Int = mkdir(path)
