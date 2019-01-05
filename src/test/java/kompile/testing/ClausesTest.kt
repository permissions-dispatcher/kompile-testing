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
        val clause = SuccessfulCompilationClause(root).generatedFile(fileName)
        clause.hasSourceEquivalentTo(source)
    }

    @Test
    fun hasSourceEquivalentToStringFailed() {
        expectedException.expect(ComparisonFailure::class.java)
        val root = temporaryFolder.root
        val fileName = "test.txt"
        val source = "this is a txt file."
        addFile(root, fileName, source)
        val clause = SuccessfulCompilationClause(root).generatedFile(fileName)
        clause.hasSourceEquivalentTo("unrelated content.")
    }

    @Test
    fun hasSourceEquivalentToFile() {
        val root = temporaryFolder.root
        val fileName = "test.txt"
        val source = "this is a txt file."
        addFile(root, fileName, source)
        val clause = SuccessfulCompilationClause(root).generatedFile(fileName)
        clause.hasSourceEquivalentTo(File(root, fileName))
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
        val clause = SuccessfulCompilationClause(root).generatedFile(fileName)
        clause.hasSourceEquivalentTo(arrayOf(
                "this is a txt file",
                "which contains break line."
        ))
    }

    @Test
    fun withErrorContaining() {
        val message = "this is an error message."
        val error = "$message You should check it out!"
        val clause = UnsuccessfulCompilationClause(error)
        clause.withErrorContaining(message)
    }

    @Test
    fun withErrorContainingFailure() {
        expectedException.expect(ComparisonFailure::class.java)
        val message = "Unrelated message."
        val error = "This is an error message. You should check it out!"
        val clause = UnsuccessfulCompilationClause(error)
        clause.withErrorContaining(message)
    }
}