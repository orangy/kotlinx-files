package kotlinx.files.memory

import kotlinx.files.*
import kotlinx.files.platform.*
import kotlinx.io.errors.*

internal sealed class MemoryCatalogueEntry(
    val fileSystem: MemoryFileSystem,
    val name: String,
    val parent: MemoryCatalogueDirectory?
) {
    private val creationTimestamp = currentTimeMicrosSinceEpoch()
    private var modificationTimestamp = creationTimestamp
    private var accessTimestamp = creationTimestamp

    fun registerModification() {
        modificationTimestamp = currentTimeMicrosSinceEpoch()
        accessTimestamp = modificationTimestamp
    }

    fun registerAccess() {
        accessTimestamp = currentTimeMicrosSinceEpoch()
    }
    
    fun readAttributes(): FileAttributes {
        return FileAttributes(
            this is MemoryCatalogueDirectory,
            this is MemoryCatalogueFile,
            false,
            creationTimestamp, accessTimestamp, modificationTimestamp,
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

    fun get(name: String): MemoryCatalogueEntry? {
        return entries?.get(name)
    }

    fun isEmpty(): Boolean {
        return entries?.isEmpty() ?: true
    }

    fun delete(name: String): Boolean {
        if (entries == null)
            return false // no such item

        val item = entries!![name]
        if (item is MemoryCatalogueDirectory && !item.isEmpty())
            return false

        registerModification()

        return entries!!.remove(name) != null
    }

    fun moveFrom(source: MemoryCatalogueEntry, name: String) {
        when (source) {
            is MemoryCatalogueFile -> {
                val newFile = createFile(name)
                newFile.data = source.data
            }
            is MemoryCatalogueDirectory -> {
                val newFile = createDirectory(name)
                newFile.entries = source.entries
            }
        }

        source.parent?.apply {
            entries?.remove(source.name)
            registerModification()
        }

        registerModification()
    }

    fun createDirectory(name: String): MemoryCatalogueDirectory {
        if (entries == null)
            entries = mutableMapOf()

        val directory = MemoryCatalogueDirectory(fileSystem, name, this)
        entries!!.put(name, directory)
        registerModification()
        return directory
    }

    fun createFile(name: String): MemoryCatalogueFile {
        if (entries == null)
            entries = mutableMapOf()

        val file = MemoryCatalogueFile(fileSystem, name, this)
        entries!!.put(name, file)
        registerModification()
        return file
    }

    fun resolve(path: Path): MemoryCatalogueEntry? {
        var directory: MemoryCatalogueDirectory = this
        val count = path.componentCount
        for (index in 0 until count) {
            directory.registerAccess()
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
        registerAccess()
        return MemoryFileInput(buildPath().toString(), this)
    }

    fun openOutput(): FileOutput {
        registerModification()
        return MemoryFileOutput(buildPath().toString(), this)
    }
}

