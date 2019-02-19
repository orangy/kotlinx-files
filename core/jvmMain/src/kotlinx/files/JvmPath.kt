package kotlinx.files

import java.nio.file.*
import java.nio.file.Path as JavaPath

class JvmPath(override val fileSystem: JvmFileSystem, internal val platformPath: JavaPath) : Path {
    override val parent: Path?
        get() = platformPath.parent?.let { JvmPath(fileSystem, it) }

    override val name: JvmPath?
        get() = platformPath.fileName?.let { JvmPath(fileSystem, it) }

    override val isAbsolute: Boolean
        get() = platformPath.isAbsolute

    override val componentCount: Int
        get() = platformPath.nameCount

    override fun component(index: Int): Path = JvmPath(fileSystem, platformPath.getName(index))
    
    fun toJavaPath() = platformPath

    override fun toString(): String = platformPath.toString()
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as JvmPath

        if (fileSystem != other.fileSystem) return false
        if (platformPath != other.platformPath) return false

        return true
    }

    override fun hashCode(): Int {
        return platformPath.hashCode()
    }


}

// TODO: better name
fun JavaPath.toKotlinPath() = JvmFileSystem.fromPlatform(this)
