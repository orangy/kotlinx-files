package kotlinx.files

import kotlinx.cinterop.*
import platform.posix.*

actual fun compat_lstat(path: String, ptr: CPointer<stat>): Int = stat(path, ptr)