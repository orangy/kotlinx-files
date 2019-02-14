package kotlinx.files

import kotlinx.io.core.*
import kotlinx.io.errors.*
import kotlin.contracts.*

class JsFileSystem : FileSystem {

    override fun openDirectory(path: Path): JsDirectory {
        checkCompatible(path)
        return JsDirectory(this, path)
    }

    override val isReadOnly: Boolean get() = false

    override fun deleteDirectory(path: Path): Boolean {
        checkCompatible(path)
        if (!exists(path))
            return false
        deleteRecursively(path)
        return true
    }

    private fun deleteRecursively(path: Path): Unit = openDirectory(path).use { directory ->
        for (child in directory.children) {
            if (isDirectory(child))
                deleteRecursively(child)
            else
                delete(child)
        }
        delete(path)
    }

    override fun isDirectory(path: Path): Boolean {
        checkCompatible(path)
        if (!exists(path))
            return false
        val attributes = fs.lstatSync(path.str())
        return attributes.isDirectory() as Boolean
    }

    override fun isFile(path: Path): Boolean {
        checkCompatible(path)
        if (!exists(path))
            return false
        val attributes = fs.lstatSync(path.str())
        return attributes.isFile() as Boolean
    }

    override fun path(name: String, vararg children: String): UnixPath {
        if (children.isEmpty()) {
            return UnixPath(this, name)
        }
        return UnixPath(this, "$name/${children.joinToString("/")}")
    }

    override fun exists(path: Path): Boolean {
        checkCompatible(path)
        return fs.existsSync(path.str()) as Boolean
    }

    override fun createFile(path: Path): UnixPath {
        checkCompatible(path)
        val fd = fs.openSync(path.str(), "w")
        fs.closeSync(fd)
        return path
    }

    override fun createDirectory(path: Path): UnixPath {
        checkCompatible(path)
        try {
            fs.mkdirSync(path.str())
        } catch (e: dynamic) {
            throw IOException("Failed to create directory: $e")
        }
        return path
    }

    override fun copy(source: Path, target: Path): UnixPath {
        checkCompatible(source)
        checkCompatible(target)
        if (isDirectory(source)) {
            copyDirectoryRecursive(source, target)
        } else {
            try {
                // COPYFILE_EXCL to fail if target exists
                fs.copyFileSync(source.str(), target.str(), fs.constants.COPYFILE_EXCL)
            } catch (e: dynamic) {
                throw IOException("Failed to copy $source to $target: $e")
            }
        }

        return target
    }

    private fun copyDirectoryRecursive(source: Path, target: Path): Unit = openDirectory(source).use { directory ->
        if (!exists(target)) {
            createDirectory(target)
        }

        for (sourceChild in directory.children) {
            val targetChild = UnixPath(this, "$target/${sourceChild.name}")
            if (sourceChild.isDirectory)
                copyDirectoryRecursive(sourceChild, targetChild)
            else
                copy(sourceChild, targetChild)
        }
    }

    override fun move(source: Path, target: Path): UnixPath {
        checkCompatible(source)
        checkCompatible(target)
        if (target.exists()) {
            throw IOException("File $target already exists")
        }

        try {
            // COPYFILE_EXCL to fail if target exists
            fs.renameSync(source.str(), target.str())
        } catch (e: dynamic) {
            throw IOException("Failed to move $source to $target: $e")
        }

        return target
    }

    override fun delete(path: Path): Boolean {
        checkCompatible(path)
        return try {
            if (path.isDirectory) {
                fs.rmdirSync(path.str())
            } else {
                fs.unlinkSync(path.str())
            }
            true
        } catch (e: Throwable) {
            false
        }
    }

    override fun openInput(path: Path): JsFileInput {
        checkCompatible(path)
        try {
            val fd = fs.openSync(path.toString(), "r");
            return JsFileInput(path, fd)
        } catch (e: dynamic) {
            throw IOException("Failed to create an input stream for $path: $e")
        }
    }

    @UseExperimental(ExperimentalIoApi::class)
    override fun openOutput(path: Path): JsFileOutput {
        checkCompatible(path)
        if (path.isDirectory)
            throw IOException("Cannot create output stream for directory")

        try {
            val fd = fs.openSync(path.toString(), "w");
            return JsFileOutput(path, fd)
        } catch (e: dynamic) {
            throw IOException("Failed to create an output stream for $path: $e")
        }
    }

    companion object {
        val Default = JsFileSystem()

        internal val fs: dynamic = require("fs")
    }
}

private fun Path.str(): String = (this as UnixPath).normalizedPath

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
