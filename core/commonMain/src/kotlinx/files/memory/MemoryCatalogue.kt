package kotlinx.files.memory

import kotlinx.files.*
import kotlinx.io.errors.*

internal fun currentTime(): Long {
    // TODO: mpp current timestamp
    return 1525968223L * 1e6.toLong()
}

internal sealed class MemoryCatalogueEntry(
    val fileSystem: MemoryFileSystem,
    val name: String,
    val parent: MemoryCatalogueDirectory?
) {
    fun readAttributes(): FileAttributes {
        return FileAttributes(
            this is MemoryCatalogueDirectory,
            this is MemoryCatalogueFile,
            false,
            currentTime(), currentTime(), currentTime(),
            if (this is MemoryCatalogueFile) data.size.toLong() else 0
        )
    }

    fun buildPath(): Path {
        if (parent != null)
            return fileSystem.path(parent.buildPath(), name)
        return fileSystem.path(name)
    }
}

internal class MemoryDirectoryListing(val entry: MemoryCatalogueDirectory, val path: Path) : Directory {
    override fun close() {}

    override val children: Iterable<Path>
        get() {
            return entry.entries?.keys?.map { path.resolve(it) } ?: emptyList()
        }
}

// TODO: thread safety
internal class MemoryCatalogueDirectory(fileSystem: MemoryFileSystem, name: String, parent: MemoryCatalogueDirectory?) :
    MemoryCatalogueEntry(fileSystem, name, parent) {
    internal var entries: MutableMap<String, MemoryCatalogueEntry>? = null

    fun get(name: String): MemoryCatalogueEntry? = entries?.get(name)
    fun isEmpty(): Boolean = entries?.isEmpty() ?: true

    fun delete(name: String): Boolean {
        if (entries == null)
            return false // no such item

        val item = entries!![name]
        if (item is MemoryCatalogueDirectory && !item.isEmpty())
            return false

        return entries!!.remove(name) != null
    }

    fun moveFrom(source: MemoryCatalogueEntry, name: String) {
        when (source) {
            is MemoryCatalogueFile -> {
                val newFile = createFile(name)
                newFile.data = source.data
                source.parent?.entries?.remove(source.name)
            }
            is MemoryCatalogueDirectory -> {
                val newFile = createDirectory(name)
                newFile.entries = source.entries
                source.parent?.entries?.remove(source.name)
            }
        }
    }

    fun createDirectory(name: String): MemoryCatalogueDirectory {
        if (entries == null)
            entries = mutableMapOf()

        val directory = MemoryCatalogueDirectory(fileSystem, name, this)
        entries!!.put(name, directory)
        return directory
    }

    fun createFile(name: String): MemoryCatalogueFile {
        if (entries == null)
            entries = mutableMapOf()

        val file = MemoryCatalogueFile(fileSystem, name, this)
        entries!!.put(name, file)
        return file
    }

    fun resolve(path: Path): MemoryCatalogueEntry? {
        var directory: MemoryCatalogueDirectory = this
        val count = path.componentCount
        for (index in 0 until count) {
            val name = path.component(index)
            when (val item = directory.get(name)) {
                is MemoryCatalogueDirectory -> directory = item
                is MemoryCatalogueFile -> {
                    if (index == count - 1)
                        return item
                    else
                        throw IOException("Cannot resolve path $path, because $name is a file and not a directory")
                }
                null -> return null
            }
        }
        return directory
    }
}

internal class MemoryCatalogueFile(fileSystem: MemoryFileSystem, name: String, parent: MemoryCatalogueDirectory?) :
    MemoryCatalogueEntry(fileSystem, name, parent) {

    internal var data: ByteArray = byteArrayOf()

    fun openInput(): FileInput {
        return MemoryFileInput(buildPath().toString(), this)
    }

    fun openOutput(): FileOutput {
        return MemoryFileOutput(buildPath().toString(), this)
    }
}

