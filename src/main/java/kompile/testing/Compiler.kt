/*
 * Copyright (C) 2019 permissions-dispatcher contributors.
 * Copyright (C) 2018 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kompile.testing

import okio.Buffer
import okio.buffer
import okio.sink
import org.apache.commons.io.FileUtils
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.com.google.common.collect.LinkedHashMultimap
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.PrintStream
import java.net.URL
import java.net.URLClassLoader
import java.net.URLDecoder
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.annotation.processing.Processor
import kotlin.reflect.KClass

class Compiler(private val rootDir: File) {
    private val sourcesDir = File(rootDir, "sources")
    private val classesDir = File(rootDir, "classes")
    private val servicesJar = File(rootDir, "services.jar")
    private val services = LinkedHashMultimap.create<KClass<*>, KClass<*>>()!!
    private val classpathFiles by classpathFiles()
    private var classLoader = javaClass.classLoader

    /** Adds a kotlin source to be compiled. */
    fun addKotlin(fileName: String, source: String) = apply {
        val sourceFile = File(sourcesDir, fileName)
        sourceFile.parentFile.mkdirs()
        sourceFile.sink().buffer().use {
            it.writeUtf8(source)
        }
    }

    /** Adds an annotation processor to make available to the compiler. */
    fun withProcessors(vararg processors: Processor) = apply {
        processors.forEach {
            services.put(Processor::class, it::class)
        }
    }

    /** Uses a given classpath instead of the system classpath. */
    fun withClasspath(classLoader: ClassLoader) = apply {
        this.classLoader = classLoader
    }

    /** Compiles Kotlin source files. */
    fun compile(): Compilation {
        val args = mutableListOf<String>()
        args.add("-d")
        args.add(classesDir.toString())
        args.add("-no-stdlib")
        args.add("-classpath")
        args.add(fullClasspath().joinToString(separator = ":"))
        val sourceFiles = sourcesDir.listFiles()
        if (sourceFiles != null && sourceFiles.isNotEmpty()) {
            sourceFiles.forEach {
                args.add(it.toString())
            }
        }
        val sourceDir = File(rootDir, "kapt/sources")
        args.addAll(annotationProcessorArgs(sourceDir))
        val systemErrBuffer = Buffer()
        val exitCode = K2JVMCompiler().exec(errStream = PrintStream(systemErrBuffer.outputStream()), args = *args.toTypedArray())
        return Compilation(systemErrBuffer.readUtf8(), exitCode, sourceDir)
    }

    private fun annotationProcessorArgs(sourceDir: File): List<String> {
        val stubsDir = File(rootDir, "kapt/stubs")
        val kaptArgs = mutableMapOf<String, String>()
        kaptArgs["kapt.kotlin.generated"] = sourceDir.path
        val plugin = classpathFiles.find { it.name.startsWith("kotlin-annotation-processing-embeddable") }
        return listOf(
                "-Xplugin=$plugin",
                "-P", "plugin:org.jetbrains.kotlin.kapt3:sources=$sourceDir",
                "-P", "plugin:org.jetbrains.kotlin.kapt3:classes=$classesDir",
                "-P", "plugin:org.jetbrains.kotlin.kapt3:stubs=$stubsDir",
                "-P", "plugin:org.jetbrains.kotlin.kapt3:apclasspath=$servicesJar",
                "-P", "plugin:org.jetbrains.kotlin.kapt3:aptMode=stubsAndApt",
                "-P", "plugin:org.jetbrains.kotlin.kapt3:correctErrorTypes=true",
                "-P", "plugin:org.jetbrains.kotlin.kapt3:apoptions=${encodeOptions(kaptArgs)}"
        )
    }

    private fun fullClasspath(): List<String> {
        val result = mutableListOf<String>()
        classpathFiles.forEach {
            result.add(it.toString())
        }
        if (!services.isEmpty) {
            writeServicesJar()
            result.add(servicesJar.toString())
        }
        return result.toList()
    }

    private fun writeServicesJar() {
        ZipOutputStream(FileOutputStream(servicesJar)).use { zipOutputStream ->
            for (entry in services.asMap()) {
                zipOutputStream.putNextEntry(ZipEntry("META-INF/services/${entry.key.qualifiedName}"))
                val serviceFile = zipOutputStream.sink().buffer()
                for (implementation in entry.value) {
                    serviceFile.writeUtf8(implementation.qualifiedName!!)
                    serviceFile.writeUtf8("\n")
                }
                serviceFile.emit()
                zipOutputStream.closeEntry()
            }
        }
    }

    private fun classpathFiles(): Lazy<List<File>> = lazy {
        val classLoader = this.classLoader
        if (classLoader !is URLClassLoader) {
            throw UnsupportedOperationException("unable to extract classpath from $classLoader")
        }
        val result = mutableListOf<File>()
        for (url in classLoader.urLs) {
            if (url.protocol != "file") {
                throw UnsupportedOperationException("unable to handle classpath element $url")
            }
            val file = if (url.path.endsWith(".aar")) extractJarFromAar(url) else File(URLDecoder.decode(url.path, "UTF-8"))
            if (file.exists()) {
                result.add(file)
            }
        }
        result.toList()
    }

    private fun extractJarFromAar(url: URL): File {
        val zipFile = ZipFile(url.path)
        val newFileName = url.path
            .substringAfterLast(File.separatorChar)
            .replace(".aar", ".jar")
        val jar = File(File(rootDir, "unzippedAar"), newFileName)
        val sourceInputStream = zipFile.getInputStream(zipFile.getEntry("classes.jar"))
        FileUtils.copyInputStreamToFile(sourceInputStream, jar)
        return jar
    }

    private fun encodeOptions(options: Map<String, String>): String {
        val buffer = Buffer()
        ObjectOutputStream(buffer.outputStream()).use {
            it.writeInt(options.size)
            for ((key, value) in options.entries) {
                it.writeUTF(key)
                it.writeUTF(value)
            }
        }
        return buffer.readByteString().base64()
    }
}
