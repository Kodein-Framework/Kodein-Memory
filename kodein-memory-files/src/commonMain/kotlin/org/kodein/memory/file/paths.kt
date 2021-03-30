package org.kodein.memory.file


@JvmInline
public value class Path(public val path: String) {

    public constructor(vararg values: String) : this(values.joinToString(separator))

    public companion object {
        public val separator: String get() = FileSystem.pathSeparator
    }

    override fun toString(): String = path
}

public expect fun Path.isAbsolute(): Boolean

public fun Path.resolve(path: Path): Path {
    require(!path.isAbsolute()) { "Cannot resolve absolute path $path on top of $this" }
    return Path(buildString {
        append(this@resolve.path)
        if (!endsWith(Path.separator)) append(Path.separator)
        append(path.path)
    })
}

public fun Path.resolve(vararg values: String): Path = resolve(Path(*values))


public fun Path.toAbsolute(): Path {
    if (isAbsolute()) return this
    return FileSystem.workingDir().resolve(this)
}

public fun Path.normalize(): Path {
    val isAbsolute = isAbsolute()

    val segments = path.split(Path.separator).toMutableList()
    if (segments.size == 1)
        return this

    var removed: Boolean
    do {
        removed = false
        for (i in 0..segments.lastIndex) {
            if (segments[i] == ".") {
                segments.removeAt(i)
                removed = true
                break
            }
            if (i >= 1 && segments[i] == "") {
                segments.removeAt(i)
                removed = true
                break
            }
            if (i >= 1 && segments[i] == ".." && segments[i - 1] != "..") {
                segments.removeAt(i)
                if (i != 1 || !isAbsolute) segments.removeAt(i - 1)
                removed = true
                break
            }
        }
    } while (removed)

    if (segments.isEmpty()) return Path(".")

    if (isAbsolute && segments.size == 1) {
        return Path((segments + "").joinToString(Path.separator))
    }
    return Path(segments.joinToString(Path.separator))
}

public fun Path.parent(): Path = resolve("..").normalize()

public val Path.name: String get() = path.split(Path.separator).last()
