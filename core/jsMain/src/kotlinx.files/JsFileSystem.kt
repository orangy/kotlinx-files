package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*
import kotlin.contracts.*
import kotlin.reflect.*

class JsFileSystem : FileSystem {
    override val pathSeparator: String
        get() = path.sep

    override fun openDirectory(path: Path): JsDirectory {
        checkCompatible(path)
        return JsDirectory(this, path)
    }

    override val isReadOnly: Boolean get() = false
    
    override fun path(base: String, vararg children: String): UnixPath {
        if (children.isEmpty()) {
            return UnixPath(this, base)
        }
        return UnixPath(this, "$base$pathSeparator${children.joinToString(pathSeparator)}")
    }

    override fun exists(path: Path): Boolean {
        checkCompatible(path)
        return fs.existsSync(path.toString()) as Boolean
    }

    override fun createFile(path: Path): UnixPath {
        checkCompatible(path)
        val fd = fs.openSync(path.toString(), "w")
        fs.closeSync(fd)
        return path
    }

    override fun createDirectory(path: Path): UnixPath {
        checkCompatible(path)
        try {
            fs.mkdirSync(path.toString())
        } catch (e: dynamic) {
            throw IOException("Failed to create directory $path: $e")
        }
        return path
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : FileAttributes> readAttributes(path: Path, attributesClass: KClass<T>): T {
        checkCompatible(path)
        if (attributesClass != FileAttributes::class && attributesClass != PosixFileAttributes::class) {
            throw UnsupportedOperationException("File attributes of class $attributesClass are not supported by this file system")
        }

        return try {
            val attributes = fs.lstatSync(path.toString())

            PosixFileAttributes(
                isDirectory = attributes.isDirectory() as Boolean,
                isFile = attributes.isFile() as Boolean,
                isSymbolicLink = attributes.isSymbolicLink() as Boolean,
                creationTimeUs = (attributes.ctimeMs as Double).toLong() * 1000,
                lastAccessTimeUs = (attributes.atimeMs as Double).toLong() * 1000,
                lastModifiedTimeUs = (attributes.mtimeMs as Double).toLong() * 1000,
                sizeBytes = (attributes.size as Double).toLong(),
                permissions = parsePermissions((attributes.mode as Double).toInt())) as T
        } catch (e: dynamic) {
            throw IOException("Failed to read attributes for path $path: $e")
        }
    }

    private fun parsePermissions(mode: Int): Set<PosixFilePermissions> {
        val result = mutableSetOf<PosixFilePermissions>()
        if (mode and 256 != 0) result.add(PosixFilePermissions.OWNER_READ)
        if (mode and 128 != 0) result.add(PosixFilePermissions.OWNER_WRITE)
        if (mode and 64 != 0) result.add(PosixFilePermissions.OWNER_EXECUTE)

        if (mode and 32 != 0) result.add(PosixFilePermissions.GROUP_READ)
        if (mode and 16 != 0) result.add(PosixFilePermissions.GROUP_WRITE)
        if (mode and 8 != 0) result.add(PosixFilePermissions.GROUP_EXECUTE)

        if (mode and 4 != 0) result.add(PosixFilePermissions.OTHERS_READ)
        if (mode and 2 != 0) result.add(PosixFilePermissions.OTHERS_WRITE)
        if (mode and 1 != 0) result.add(PosixFilePermissions.OTHERS_EXECUTE)
        return result
    }

    override fun copy(source: Path, target: Path): UnixPath {
        checkCompatible(source)
        checkCompatible(target)
        if (source.isDirectory) {
            // TODO: Copy permissions & ownership
            createDirectory(target)
        } else {
            try {
                // COPYFILE_EXCL to fail if target exists
                fs.copyFileSync(source.toString(), target.toString(), fs.constants.COPYFILE_EXCL)
            } catch (e: dynamic) {
                throw IOException("Failed to copy $source to $target: $e")
            }
        }

        return target
    }
    
    override fun move(source: Path, target: Path): UnixPath {
        checkCompatible(source)
        checkCompatible(target)
        if (target.exists()) {
            throw IOException("File $target already exists")
        }

        try {
            // COPYFILE_EXCL to fail if target exists
            fs.renameSync(source.toString(), target.toString())
        } catch (e: dynamic) {
            throw IOException("Failed to move $source to $target: $e")
        }

        return target
    }

    override fun delete(path: Path): Boolean {
        checkCompatible(path)
        return try {
            if (path.isDirectory) {
                fs.rmdirSync(path.toString())
            } else {
                fs.unlinkSync(path.toString())
            }
            true
        } catch (e: Throwable) {
            // TODO: properly handle exceptions 
            false
        }
    }

    override fun openInput(path: Path): JsFileInput {
        checkCompatible(path)
        val stringPath = path.toString()
        try {
            val fd = fs.openSync(stringPath, "r");
            return JsFileInput(stringPath, fd)
        } catch (e: dynamic) {
            throw IOException("Failed to create an input stream for $path: $e")
        }
    }

    @UseExperimental(ExperimentalIoApi::class)
    override fun openOutput(path: Path): JsFileOutput {
        checkCompatible(path)
        if (path.isDirectory)
            throw IOException("Cannot create output stream for directory")

        val stringPath = path.toString()
        try {
            val fd = fs.openSync(stringPath, "w");
            return JsFileOutput(stringPath, fd)
        } catch (e: dynamic) {
            throw IOException("Failed to create an output stream for $path: $e")
        }
    }

    companion object {
        val Default = JsFileSystem()

        internal val fs: dynamic = require("fs")
        internal val path: dynamic = require("path")
    }
}

private external fun require(module: String): dynamic

@UseExperimental(ExperimentalContracts::class)
private fun JsFileSystem.checkCompatible(path: Path) {
    contract {
        returns() implies (path is UnixPath)
    }
    if (path is UnixPath && path.fileSystem == this)
        return
    throw kotlin.IllegalStateException("FileSystem ${path.fileSystem} for path $path is not compatible with $this")
}
