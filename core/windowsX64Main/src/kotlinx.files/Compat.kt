package kotlinx.files

import kotlinx.cinterop.*
import platform.posix.*

actual fun compat_lstat(path: String, ptr: CPointer<stat>): Int = stat(path, ptr)
actual fun compat_mkdir(path: String): Int = mkdir(path)
