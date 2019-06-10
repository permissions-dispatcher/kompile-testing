package kompile.testing

import org.jetbrains.kotlin.cli.common.ExitCode
import org.junit.ComparisonFailure
import java.io.File

class Compilation(private val error: String, private val exitCode: ExitCode, private val generatedDir: File) {
    fun succeeded(): SuccessfulCompilationClause {
        if (exitCode != ExitCode.OK) {
            throw ComparisonFailure(null, ExitCode.OK.name, exitCode.name)
        }
        return SuccessfulCompilationClause(generatedDir)
    }

    fun succeededWithoutWarnings(): SuccessfulCompilationClause {
        if (error.isNotEmpty()) {
            throw CompilationWithWarningException(error)
        }
        return succeeded()
    }

    fun failed(): UnsuccessfulCompilationClause {
        if (exitCode == ExitCode.OK) {
            throw ComparisonFailure(null, ExitCode.COMPILATION_ERROR.name, exitCode.name)
        }
        return UnsuccessfulCompilationClause(error)
    }
}
