package kompile.testing

import org.junit.ComparisonFailure
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder
import java.io.File

class ClausesTest {
    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    @Rule
    @JvmField
    val expectedException = ExpectedException.none()!!

    @Test
    fun hasSourceEquivalentToString() {
        val root = temporaryFolder.root
        val fileName = "test.txt"
        val source = "this is a txt file."
        addFile(root, fileName, source)
        val fileClause = SuccessfulCompilationClause(root).generatedFile(fileName)
        fileClause.hasSourceEquivalentTo(source)
    }

    @Test
    fun hasSourceEquivalentToFile() {
        val root = temporaryFolder.root
        val fileName = "test.txt"
        val source = "this is a txt file."
        addFile(root, fileName, source)
        val fileClause = SuccessfulCompilationClause(root).generatedFile(fileName)
        fileClause.hasSourceEquivalentTo(File(root, fileName))
    }

    @Test
    fun hasSourceEquivalentToArray() {
        val root = temporaryFolder.root
        val fileName = "test.txt"
        val source = """
            this is a txt file
            which contains break line.
        """.trimIndent()
        addFile(root, fileName, source)
        val fileClause = SuccessfulCompilationClause(root).generatedFile(fileName)
        fileClause.hasSourceEquivalentTo(arrayOf(
                "this is a txt file",
                "which contains break line."
        ))
    }

    @Test
    fun withErrorContaining() {
        val message = "this is an error message."
        val error = "$message You should check it out!"
        val fileClause = UnsuccessfulCompilationClause(error)
        fileClause.withErrorContaining(message)
    }

    @Test
    fun withErrorContainingFailure() {
        expectedException.expect(ComparisonFailure::class.java)
        val message = "Unrelated message."
        val error = "This is an error message. You should check it out!"
        val fileClause = UnsuccessfulCompilationClause(error)
        fileClause.withErrorContaining(message)
    }
}