# kompile-testing

A library for testing `kotlinc` compilation with or kotlin annotation processors(kapt).

## Installation

```groovy
dependencies {
  testImplementation "org.permissionsdispatcher:kompile-testing:0.1.0"
}
```

## Usage

A simple example that tests compiling a source file succeeded and  make assertions about generated file is:

```kotlin
kotlinc()
    .withProcessors(YourProcessor())
    .addKotlin("input.kt", """
        import kompile.testing.TestAnnotation

        @TestAnnotation
        class TestClass
        """.trimIndent())
        .compile()
        .succeededWithoutWarnings()
        .generatedFile("generatedKtFile.kt")
        .hasSourceEquivalentTo("""
            class GeneratedKtFile
        """.trimIndent())
```

You can also test that errors or warnings were reported. The following tests compiling a source file with an annotation processor reported an error:

```kotlin
kotlinc()
    .withProcessors(YourProcessor())
    .addKotlin("input.kt", """
        import kompile.testing.TestAnnotation

        @TestAnnotation
        class TestClass
        """.trimIndent())
        .compile()
        .failed()
        .withErrorContaining("Expected error message here.")
```

## License

```
Copyright 2019 Shintaro Katafuchi

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
