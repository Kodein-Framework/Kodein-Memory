package org.kodein.memory.file

import java.io.File
import java.lang.IllegalStateException

actual object FileSystem {
    internal actual val pathSeparator: String = File.separator

    actual val tempDirectory: Path get() = Path(System.getProperty("java.io.tmpdir"))

    actual var currentDirectory: Path
        get() = Path(System.getProperty("user.dir") ?: throw IllegalStateException("No user.dir system property"))
        set(value) { System.setProperty("user.dir", value.path) }

    actual val roots: List<Path> = File.listRoots().map { Path(it.absolutePath) }
}
