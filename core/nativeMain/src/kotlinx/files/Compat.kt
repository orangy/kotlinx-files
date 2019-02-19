package kotlinx.files

import kotlinx.cinterop.*
import platform.posix.*

expect fun compat_lstat(path: String, ptr: CPointer<stat>): Int
expect fun compat_mkdir(path: String): Int