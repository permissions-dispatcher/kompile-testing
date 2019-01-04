package kompile.testing

import java.io.File
import java.io.InputStream

internal fun InputStream.toFile(file: File) {
    file.outputStream().use { copyTo(it) }
}
