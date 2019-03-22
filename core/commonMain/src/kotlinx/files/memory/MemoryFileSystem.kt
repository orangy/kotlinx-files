package kotlinx.files.memory

import kotlinx.files.*
import kotlinx.io.core.*
import kotlinx.io.errors.*
import kotlin.contracts.*
import kotlin.reflect.*

class MemoryFileSystem : FileSystem {
    private val root = MemoryCatalogueDirectory(this, pathSeparator, null)

    override val isReadOnly: Boolean
        get() = false

    override val pathSeparator: String
        get() = "/"

    override fun path(base: String, vararg children: String): Path {
        if (children.isEmpty()) {
            return UnixPath(this, base)
        }
        return UnixPath(this, "$base$pathSeparator${children.joinToString(pathSeparator)}")
    }

    override fun exists(path: Path): Boolean {
        checkCompatible(path)
        return root.resolve(path) != null
    }

    override fun openInput(path: Path): FileInput {
        checkCompatible(path)
        val file = root.resolve(path) ?: throw IOException("File not found: $path")
        if (file is MemoryCatalogueFile)
            return file.openInput()
        throw IOException("Cannot open $path for input, because it is a directory")
    }

    override fun openDirectory(path: Path): Directory {
        checkCompatible(path)
        val directory = root.resolve(path) ?: throw IOException("File not found: $path")
        if (directory is MemoryCatalogueDirectory)
            return MemoryDirectoryListing(directory, path)
        throw IOException("Cannot open $path for listing, because it is not a directory")
    }

    override fun openOutput(path: Path): FileOutput {
        checkCompatible(path)
        if (!exists(path))
            createFile(path)

        when (val file = root.resolve(path)) {
            is MemoryCatalogueFile -> return file.openOutput()
            null -> throw IOException("Cannot open $path for output, because file cannot be created")
            else -> throw IOException("Cannot open $path for output, because it is a directory")
        }
    }

    private inline fun <R> directoryOperation(path: Path, factory: (MemoryCatalogueDirectory, String) -> R): R {
        checkCompatible(path)
        val parentDirectory = path.parent?.let {
            root.resolve(it)
                ?: throw IOException("Cannot process an entry at $path because parent directory doesn't exist")
        } ?: root

        when (parentDirectory) {
            is MemoryCatalogueDirectory -> {
                val name = path.name ?: throw IOException("Cannot process file at $path because name is null")
                return factory(parentDirectory, name)
            }
            is MemoryCatalogueFile -> throw IOException("Cannot process file at $path because parent path is not a directory")
        }
    }

    override fun createFile(path: Path): Path {
        val file = root.resolve(path)
        if (file != null)
            throw IOException("Entry $path already exists")

        directoryOperation(path) { dir, name -> dir.createFile(name) }
        return path
    }

    override fun createDirectory(path: Path): Path {
        val file = root.resolve(path)
        if (file != null)
            throw IOException("Entry $path already exists")

        directoryOperation(path) { dir, name -> dir.createDirectory(name) }
        return path
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : FileAttributes> readAttributes(path: Path, attributesClass: KClass<T>): T {
        val entry = root.resolve(path) ?: throw IOException("File system entry not found: $path")
        return when (attributesClass) {
            FileAttributes::class -> entry.readAttributes() as T
            else -> throw UnsupportedOperationException("Unsupported attributes class '$attributesClass'")
        }
    }

    override fun move(source: Path, target: Path): Path {
        checkCompatible(source)
        checkCompatible(target)

        if (exists(target)) {
            throw IOException("Path $target already exists")
        }

        val sourceEntry = root.resolve(source) ?: throw IOException("Cannot resolve $source")

        directoryOperation(target) { dir, name ->
            dir.moveFrom(sourceEntry, name)
        }
        
        return target
    }

    override fun copy(source: Path, target: Path): Path {
        checkCompatible(source)
        checkCompatible(target)

        if (exists(target)) {
            throw IOException("Path $target already exists")
        }

        when {
            source.isDirectory -> {
                // TODO: Copy permissions & ownership
                createDirectory(target)
            }
            source.isFile -> copyFile(source, target)
            else -> throw IOException("Links are not supported by implementation")
        }

        return target
    }

    private fun copyFile(source: Path, target: Path) {
        openInput(source).use { input ->
            openOutput(target).use { output ->
                input.copyTo(output)
            }
        }
    }

    override fun delete(path: Path): Boolean {
        return directoryOperation(path) { dir, name -> dir.delete(name) }
    }
}

@UseExperimental(ExperimentalContracts::class)
private fun MemoryFileSystem.checkCompatible(path: Path) {
    contract {
        returns() implies (path is UnixPath)
    }
    if (path is UnixPath && path.fileSystem == this)
        return
    throw IllegalStateException("FileSystem ${path.fileSystem} for path $path is not compatible with $this")
}
