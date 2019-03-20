package kotlinx.files

expect val posixPathSeparator: String
expect fun compat_mkdir(path: String): Int
expect val O_BINARY : Int

expect fun statAttributes(path: Path): PosixFileAttributes
expect fun statAttributes(fd: Int): PosixFileAttributes
