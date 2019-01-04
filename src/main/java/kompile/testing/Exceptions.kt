package kompile.testing

class CompilationWithWarningException(warnings: String) : RuntimeException("Compilation has been done with the following warnings.\n$warnings")
