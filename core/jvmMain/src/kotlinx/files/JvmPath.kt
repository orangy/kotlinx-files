package kotlinx.files

import java.nio.file.Path as JavaPath

class JvmPath(override val fileSystem: JvmFileSystem, internal val platformPath: JavaPath) : Path {
    override val parent: Path?
        get() = platformPath.parent?.let { JvmPath(fileSystem, it) }

    override val name: String?
        get() = platformPath.fileName?.toString()

    override val isAbsolute: Boolean
        get() = platformPath.isAbsolute

    override val componentCount: Int
        get() = platformPath.nameCount

    override fun component(index: Int): String = platformPath.getName(index).toString()
    
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
