package kotlinx.files

import kotlinx.cinterop.*
import platform.posix.*

actual fun compat_lstat(path: String, ptr: CPointer<stat>): Int = lstat(path, ptr)
actual fun compat_mkdir(path: String): Int = mkdir(path, 0x1FF) // 0x1FF hex == 511 == 0777 oct
