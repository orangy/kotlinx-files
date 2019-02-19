package kotlinx.files

class UnixPath private constructor(
    override val fileSystem: FileSystem,
    path: String,
    offsets: IntArray? = null
) : Path {

    constructor(fileSystem: FileSystem, path: String) : this(fileSystem, path, null)

    companion object {
        private const val pathSeparator = '/'
    }

    // BUG in K/N: this should be before normalizedPath or it will incorrectly reinit the property as "uninited"
    private lateinit var componentOffsets: IntArray

    // If we passed offsets to constructor, the provided path must be normalized
    // Otherwise normalize() will fix the path if needed and calculate offsets
    // Normalized path is the path without empty components.
    internal val normalizedPath: String = if (offsets == null)
        normalize(path)
    else
        path.also { componentOffsets = offsets }


    // TODO: may be lazy?
    override val parent: UnixPath?
        get() {
            if (componentOffsets.isEmpty())
                return null

            /*
             * /file.txt, last offset is 1, should return root
             * file.txt, last offset is 0, should return null
             */

            val end = componentOffsets.last()
            return when (end) {
                1 -> UnixPath(fileSystem, pathSeparator.toString(), intArrayOf())
                0 -> null
                else -> {
                    val offsets = componentOffsets.copyOf(componentOffsets.size - 1)
                    UnixPath(fileSystem, normalizedPath.substring(0, end - 1), offsets)
                }
            }
        }

    // TODO: may be lazy?
    override val name: UnixPath?
        get() {
            if (componentOffsets.isEmpty())
                return null

            val path = normalizedPath.substring(componentOffsets.last(), normalizedPath.length)
            return UnixPath(fileSystem, path, intArrayOf(0))
        }

    override val isAbsolute: Boolean get() = normalizedPath.startsWith(pathSeparator)

    override val componentCount: Int
        get() = componentOffsets.size

    override fun component(index: Int): UnixPath {
        if (index < 0 || index >= componentOffsets.size) {
            throw IllegalArgumentException("Illegal index: $index, should be in [0, ${componentOffsets.size - 1}]")
        }

        val begin = componentOffsets[index]
        val len: Int
        len = if (index == componentOffsets.size - 1) {
            normalizedPath.length - begin
        } else {
            componentOffsets[index + 1] - begin - 1
        }

        val path = normalizedPath.substring(begin, begin + len)
        return UnixPath(fileSystem, path, intArrayOf(0))
    }

    fun resolve(other: Path): UnixPath {
        require(other is UnixPath) {
            "Mixing paths of different classes are forbidden, target class: ${UnixPath::class}, received class: ${other::class}"
        }

        if (other.isAbsolute) {
            return other
        }

        if (normalizedPath.isEmpty()) {
            return other
        }

        if (other.normalizedPath.isEmpty()) {
            return this
        }

        // TODO not the most optimal solution
        // TODO: join known offsets from both paths
        // Beware if `other` path is relative starting with uncollapsed ../../.., can't reuse offsets here
        return UnixPath(fileSystem, "$normalizedPath$pathSeparator${other.normalizedPath}", null)
    }

    override fun toString(): String = normalizedPath

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UnixPath

        if (fileSystem != other.fileSystem) return false
        if (normalizedPath != other.normalizedPath) return false

        return true
    }

    override fun hashCode(): Int {
        return normalizedPath.hashCode()
    }

    private fun normalize(path: String): String {
        val index = path.indexOf("//")
        val normalized = when {
            index != -1 -> normalizeImpl(path, path.length, index)
            path.isEmpty() || path[path.length - 1] != pathSeparator -> path
            else -> normalizeImpl(path, path.length, path.length - 1)
        }
        componentOffsets = calculateOffsets(normalized)
        return normalized
    }

    private fun normalizeImpl(input: String, length: Int, startFrom: Int): String {
        if (length == 0) return input

        // Trim trailing slashes
        var lastIndex = length
        while ((lastIndex > 0) && (input[lastIndex - 1] == pathSeparator)) {
            lastIndex--
        }

        if (lastIndex == 0) {
            return pathSeparator.toString()
        }

        val sb = StringBuilder(input.length + (input.length - lastIndex))

        if (startFrom > 0) {
            sb.append(input, 0, startFrom)
        }

        var previousChar: Char = 0.toChar()
        for (i in startFrom until lastIndex) {
            val c = input[i]
            if (c == pathSeparator && previousChar == pathSeparator) {
                continue
            }

            sb.append(c)
            previousChar = c
        }

        return sb.toString()
    }

    private fun calculateOffsets(path: String): IntArray {
        val result = IntArray(countNameParts(path))
        var count = 0
        var index = 0

        val length = path.length
        while (index < length) {
            val c = path[index]
            if (c == pathSeparator) {
                index++
            } else {
                result[count++] = index++
                while (index < length && path[index] != pathSeparator) {
                    index++
                }
            }
        }

        return result
    }

    private fun countNameParts(path: String): Int {
        if (path.isEmpty()) {
            return 1
        }

        var currentIndex = 0
        var result = 0

        while (currentIndex < path.length) {
            if (path[currentIndex++] != pathSeparator) {
                ++result

                while (currentIndex < path.length && path[currentIndex] != pathSeparator) {
                    ++currentIndex
                }
            }
        }

        return result
    }
}
