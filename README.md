# kotlinx-files [WIP]

Cross-platform file API in pure Kotlin which provides uniform path-based interface for JVM, JS and Native.

## API description
The core API concept is `Path` interface.
`Path` represents a path in the file system, which can point to file, directory or symbolic link.
Path can be absolute or relative.
`Path` instance can be obtained directly from the filesystem (`FileSystem.path(str)`) or via factory method `Path(string)`.
In the latter default file system will be used.

