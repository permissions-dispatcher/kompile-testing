package kompile.testing

import com.google.auto.service.AutoService
import okio.buffer
import okio.sink
import java.io.File
import java.lang.RuntimeException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

fun addFile(dir: File, fileName: String, source: String) {
    val sourceFile = File(dir, fileName)
    sourceFile.parentFile.mkdirs()
    sourceFile.sink().buffer().use {
        it.writeUtf8(source)
    }
}

@Target(AnnotationTarget.CLASS)
annotation class TestAnnotation

open class TestAnnotationProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    open fun generatedFileName() = "generated.kt"

    open fun generatedFileContent() = """
            import kompile.testing.TestAnnotation

            @TestAnnotation
            class TestClass
            """.trimIndent()

    open fun throwExceptionIfNeeded() {
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes() = hashSetOf(TestAnnotation::class.java.canonicalName)

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.")
            return false
        }
        throwExceptionIfNeeded()
        File(kaptKotlinGeneratedDir, generatedFileName()).apply {
            parentFile.mkdirs()
            writeText(generatedFileContent())
        }
        return true
    }
}

@AutoService(Processor::class)
class ValidProcessor : TestAnnotationProcessor()

@AutoService(Processor::class)
class ThrowExceptionProcessor : TestAnnotationProcessor() {
    companion object {
        const val errorMessage = "Throw an exception deliberately."
    }

    override fun throwExceptionIfNeeded() {
        throw RuntimeException(errorMessage)
    }
}