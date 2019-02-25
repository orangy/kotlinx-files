package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*
import kotlin.contracts.*

class JsFileSystem : FileSystem {
    override val pathSeparator: String
        get() = path.sep

    override fun openDirectory(path: Path): JsDirectory {
        checkCompatible(path)
        return JsDirectory(this, path)
    }

    override val isReadOnly: Boolean get() = false
    
    override fun isDirectory(path: Path): Boolean {
        checkCompatible(path)
        if (!exists(path))
            return false
        val attributes = fs.lstatSync(path.toString())
        return attributes.isDirectory() as Boolean
    }

    override fun isFile(path: Path): Boolean {
        checkCompatible(path)
        if (!exists(path))
            return false
        val attributes = fs.lstatSync(path.toString())
        return attributes.isFile() as Boolean
    }

    override fun path(name: String, vararg children: String): UnixPath {
        if (children.isEmpty()) {
            return UnixPath(this, name)
        }
        return UnixPath(this, "$name$pathSeparator${children.joinToString(pathSeparator)}")
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
            throw IOException("Failed to create directory: $e")
        }
        return path
    }

    override fun copy(source: Path, target: Path): UnixPath {
        checkCompatible(source)
        checkCompatible(target)
        if (isDirectory(source)) {
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
