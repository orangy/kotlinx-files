package kotlinx.files

import java.nio.file.*
import java.util.stream.*
import kotlin.contracts.*
import java.nio.file.FileSystem as JavaFileSystem
import java.nio.file.FileSystems as JavaFileSystems
import java.nio.file.Path as JavaPath

class JvmFileSystem(internal val platformFileSystem: JavaFileSystem) : FileSystem {
    override val isReadOnly: Boolean
        get() = platformFileSystem.isReadOnly

    override fun isDirectory(path: Path): Boolean {
        checkCompatible(path)
        return Files.isDirectory(path.platformPath)
    }

    override fun isFile(path: Path): Boolean {
        checkCompatible(path)
        return Files.isRegularFile(path.platformPath)
    }

    override fun createFile(path: Path): Path {
        checkCompatible(path)
        Files.createFile(path.platformPath)
        return path
    }

    override fun deleteFile(path: Path): Boolean {
        checkCompatible(path)
        return Files.deleteIfExists(path.platformPath)
    }

    override fun move(source: Path, target: Path): Path {
        checkCompatible(source)
        checkCompatible(target)
        Files.move(source.platformPath, target.platformPath)
        return target
    }

    override fun copy(source: Path, target: Path): Path {
        checkCompatible(source)
        checkCompatible(target)
        Files.copy(source.platformPath, target.platformPath)
        return target
    }

    override fun path(name: String, vararg children: String): Path {
        return JvmPath(this, platformFileSystem.getPath(name, *children))
    }

    override fun exists(path: Path): Boolean {
        checkCompatible(path)
        return Files.exists(path.platformPath)
    }

    override fun openInput(path: Path): FileInput {
        checkCompatible(path)
        return JvmFileInput(path, Files.newByteChannel(path.platformPath))
    }

    override fun openOutput(path: Path): FileOutput {
        checkCompatible(path)
        return JvmFileOutput(
            path,
            Files.newByteChannel(
                path.platformPath,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        )
    }

    override fun openDirectory(path: Path): Directory = TODO()
    override fun createDirectory(path: Path): Path {
        checkCompatible(path)
        return JvmPath(this, Files.createDirectories(path.platformPath))
    }

    override fun deleteDirectory(path: Path): Boolean {
        checkCompatible(path)
        if (!Files.exists(path.platformPath))
            return false
        deleteRecursively(path.platformPath)
        return true
    }

    private fun deleteRecursively(path: JavaPath) {
        val children = Files.list(path).collect(Collectors.toList())
        children.forEach {
            if (Files.isDirectory(it)) {
                deleteRecursively(it)
            } else {
                Files.delete(it)
            }
        }

        Files.delete(path)
    }

    override fun toString() = "JvmFileSystem for '${platformFileSystem.provider().scheme}'"

    companion object {
        private val defaultPlatformFileSystem = JavaFileSystems.getDefault()

        // TBD: might need caching file systems, e.g. for mass ZIP processing via a Java lib
        fun fromPlatform(fileSystem: JavaFileSystem): JvmFileSystem {
            if (fileSystem == defaultPlatformFileSystem)
                return Default
            return JvmFileSystem(fileSystem)
        }

        fun fromPlatform(path: java.nio.file.Path): JvmPath {
            return JvmPath(fromPlatform(path.fileSystem), path)
        }

        val Default = JvmFileSystem(defaultPlatformFileSystem)
    }
}

@UseExperimental(ExperimentalContracts::class)
private fun JvmFileSystem.checkCompatible(path: Path) {
    contract {
        returns() implies (path is JvmPath)
    }
    if (path is JvmPath && path.fileSystem.platformFileSystem == platformFileSystem)
        return
    throw IllegalStateException("FileSystem ${path.fileSystem} for path $path is not compatible with $this")
}
