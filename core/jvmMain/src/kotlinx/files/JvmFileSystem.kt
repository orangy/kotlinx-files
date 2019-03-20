package kotlinx.files

import java.nio.file.*
import java.nio.file.attribute.*
import java.util.concurrent.*
import kotlin.contracts.*
import kotlin.reflect.*
import java.nio.file.FileSystem as JavaFileSystem
import java.nio.file.FileSystems as JavaFileSystems
import java.nio.file.Path as JavaPath

class JvmFileSystem(internal val platformFileSystem: JavaFileSystem) : FileSystem {
    override val pathSeparator: String
        get() = platformFileSystem.separator

    override val isReadOnly: Boolean
        get() = platformFileSystem.isReadOnly

    override fun createFile(path: Path): Path {
        checkCompatible(path)
        Files.createFile(path.platformPath)
        return path
    }

    override fun delete(path: Path): Boolean {
        checkCompatible(path)
        return try {
            Files.deleteIfExists(path.platformPath)
        } catch (e: DirectoryNotEmptyException) {
            false
        }
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
        if (Files.isDirectory(source.platformPath)) {
            // TODO: Copy permissions & ownership
            createDirectory(target)
        } else
            Files.copy(source.platformPath, target.platformPath)
        return target
    }

    override fun path(base: String, vararg children: String): Path {
        return JvmPath(this, platformFileSystem.getPath(base, *children))
    }

    override fun exists(path: Path): Boolean {
        checkCompatible(path)
        return Files.exists(path.platformPath)
    }

    override fun openInput(path: Path): FileInput {
        checkCompatible(path)
        return JvmFileInput(path.toString(), Files.newByteChannel(path.platformPath))
    }

    override fun openOutput(path: Path): FileOutput {
        checkCompatible(path)
        return JvmFileOutput(
            path.toString(),
            Files.newByteChannel(
                path.platformPath,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        )
    }

    override fun openDirectory(path: Path): JvmDirectory {
        checkCompatible(path)
        return JvmDirectory(this, path)
    }

    override fun createDirectory(path: Path): Path {
        checkCompatible(path)
        return JvmPath(this, Files.createDirectory(path.platformPath))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : FileAttributes> readAttributes(path: Path, attributesClass: KClass<T>): T {
        checkCompatible(path)
        return when (attributesClass) {
            FileAttributes::class -> {
                val attributes = Files.readAttributes(path.platformPath, BasicFileAttributes::class.java)
                FileAttributes(
                    isDirectory = attributes.isDirectory,
                    isFile = attributes.isRegularFile,
                    isSymbolicLink = attributes.isSymbolicLink,
                    creationTimeUs = attributes.creationTime().to(TimeUnit.MICROSECONDS),
                    lastAccessTimeUs = attributes.lastAccessTime().to(TimeUnit.MICROSECONDS),
                    lastModifiedTimeUs = attributes.lastModifiedTime().to(TimeUnit.MICROSECONDS),
                    size = attributes.size()
                ) as T
            }

            PosixFileAttributes::class -> {
                val attributes =
                    Files.readAttributes(path.platformPath, java.nio.file.attribute.PosixFileAttributes::class.java)
                PosixFileAttributes(
                    isDirectory = attributes.isDirectory,
                    isFile = attributes.isRegularFile,
                    isSymbolicLink = attributes.isSymbolicLink,
                    creationTimeUs = attributes.creationTime().to(TimeUnit.MICROSECONDS),
                    lastAccessTimeUs = attributes.lastAccessTime().to(TimeUnit.MICROSECONDS),
                    lastModifiedTimeUs = attributes.lastModifiedTime().to(TimeUnit.MICROSECONDS),
                    sizeBytes = attributes.size(),
                    permissions = attributes.permissions().map {
                        permissionsMapping[it]
                            ?: throw UnsupportedOperationException("Unsupported permission type '$it'")
                    }.toSet()
                ) as T
            }
            else -> throw UnsupportedOperationException("Unsupported attributes class '$attributesClass'")
        }
    }

    override fun toString() = "JvmFileSystem[${platformFileSystem.provider().scheme}]"

    companion object {
        @JvmStatic
        private val permissionsMapping = mapOf(
            PosixFilePermission.OWNER_READ to PosixFilePermissions.OWNER_READ,
            PosixFilePermission.OWNER_WRITE to PosixFilePermissions.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE to PosixFilePermissions.OWNER_EXECUTE,


            PosixFilePermission.GROUP_READ to PosixFilePermissions.GROUP_READ,
            PosixFilePermission.GROUP_WRITE to PosixFilePermissions.GROUP_WRITE,
            PosixFilePermission.GROUP_EXECUTE to PosixFilePermissions.GROUP_EXECUTE,

            PosixFilePermission.OTHERS_READ to PosixFilePermissions.OTHERS_READ,
            PosixFilePermission.OTHERS_WRITE to PosixFilePermissions.OTHERS_WRITE,
            PosixFilePermission.OTHERS_EXECUTE to PosixFilePermissions.OTHERS_EXECUTE
        )

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
