package kotlinx.files

expect val posixPathSeparator: String
expect fun compat_mkdir(path: String): Int