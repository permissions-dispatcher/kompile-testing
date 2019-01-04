package kompile.testing

import org.jetbrains.kotlin.cli.common.ExitCode
import org.junit.ComparisonFailure
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder

class CompilationTest {
    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    @Rule
    @JvmField
    val expectedException = ExpectedException.none()!!

    @Test
    fun succeeded() {
        val compilation = Compilation("error message", ExitCode.OK, temporaryFolder.root)
        compilation.succeeded()
    }

    @Test
    fun succeededError() {
        expectedException.expect(ComparisonFailure::class.java)
        val compilation = Compilation("error message", ExitCode.COMPILATION_ERROR, temporaryFolder.root)
        compilation.succeeded()
    }

    @Test
    fun succeededWithoutWarnings() {
        val compilation = Compilation("", ExitCode.OK, temporaryFolder.root)
        compilation.succeededWithoutWarnings()
    }

    @Test
    fun succeededWithoutWarningsError() {
        val warnings = "warning message"
        expectedException.expect(CompilationWithWarningException::class.java)
        expectedException.expectMessage("Compilation has been done with the following warnings.\n$warnings")
        val compilation = Compilation(warnings, ExitCode.OK, temporaryFolder.root)
        compilation.succeededWithoutWarnings()
    }

    @Test
    fun failed() {
        val compilation = Compilation("error message", ExitCode.COMPILATION_ERROR, temporaryFolder.root)
        compilation.failed()
    }

    @Test
    fun failedError() {
        expectedException.expect(ComparisonFailure::class.java)
        val compilation = Compilation("error message", ExitCode.OK, temporaryFolder.root)
        compilation.failed()
    }
}