package kotlinx.files

class PosixFileAttributes(
    isDirectory: Boolean,
    isFile: Boolean,
    isSymbolicLink: Boolean,
    creationTimeUs: Long,
    lastAccessTimeUs: Long,
    lastModifiedTimeUs: Long,
    sizeBytes: Long,
    val permissions: Set<PosixFilePermissions>
) :
    FileAttributes(
        isDirectory,
        isFile,
        isSymbolicLink,
        creationTimeUs,
        lastAccessTimeUs,
        lastModifiedTimeUs,
        sizeBytes
    )

enum class PosixFilePermissions(internal val weight: Int) {
    OWNER_READ(4),
    OWNER_WRITE(2),
    OWNER_EXECUTE(1),


    GROUP_READ(4),
    GROUP_WRITE(2),
    GROUP_EXECUTE(1),

    OTHERS_READ(4),
    OTHERS_WRITE(2),
    OTHERS_EXECUTE(1),

    SETUID(4),
    SETGID(2),
    STICKY_BIT(1);

    companion object
}


fun Set<PosixFilePermissions>.toOctString(): String {
    val owner = filter { it.name.startsWith("OWNER_") }.sumBy { it.weight }
    val group = filter { it.name.startsWith("GROUP_") }.sumBy { it.weight }
    val other = filter { it.name.startsWith("OTHER_") }.sumBy { it.weight }
    val special = filter { it.name.startsWith("SET") || it.name.startsWith("STICKY") }.sumBy { it.weight }
    return "$special$owner$group$other"
}

fun Set<PosixFilePermissions>.toHumanReadableString(): String {
    val sb = StringBuilder(12)

    if (contains(PosixFilePermissions.STICKY_BIT)) {
        sb.append('d')
    }

    writeGroup(
        sb,
        contains(PosixFilePermissions.OWNER_READ),
        contains(PosixFilePermissions.OWNER_WRITE),
        contains(PosixFilePermissions.OWNER_EXECUTE)
    )

    if (contains(PosixFilePermissions.SETGID)) {
        sb.append('s')
    }

    writeGroup(
        sb,
        contains(PosixFilePermissions.GROUP_READ),
        contains(PosixFilePermissions.GROUP_WRITE),
        contains(PosixFilePermissions.GROUP_EXECUTE)
    )

    if (contains(PosixFilePermissions.SETUID)) {
        sb.append('s')
    }

    writeGroup(
        sb,
        contains(PosixFilePermissions.OTHERS_READ),
        contains(PosixFilePermissions.OTHERS_WRITE),
        contains(PosixFilePermissions.OTHERS_EXECUTE)
    )


    return sb.toString()
}

private fun writeGroup(sb: StringBuilder, r: Boolean, w: Boolean, x: Boolean) {
    if (r) {
        sb.append('r')
    } else {
        sb.append('-')
    }

    if (w) {
        sb.append('w')
    } else {
        sb.append('-')
    }

    if (x) {
        sb.append('x')
    } else {
        sb.append('-')
    }
}