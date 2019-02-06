package kotlinx.files

import java.nio.file.*
import java.nio.file.Path as JavaPath

class JvmPath(override val fileSystem: JvmFileSystem, internal val platformPath: JavaPath) : Path {
    override val parent: Path?
        get() = platformPath.parent?.let { JvmPath(fileSystem, it) }

    override val name: Path?
        get() = platformPath.fileName?.let { JvmPath(fileSystem, it) }

    override val isAbsolute: Boolean
        get() = platformPath.isAbsolute

    override val componentCount: Int
        get() = platformPath.nameCount

    override fun component(index: Int): Path = JvmPath(fileSystem, platformPath.getName(index))
    
    fun toJavaPath() = platformPath

    override fun toString(): String = platformPath.toString()
}

// TODO: better name
fun JavaPath.toKotlinPath() = JvmFileSystem.fromPlatform(this)
