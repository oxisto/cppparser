plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    antlr
    idea
    `java-library`
}

idea {
    module {
        generatedSourceDirs.add(file("${project.buildDir}/generated-src/antlr/main"))
    }
}

repositories {
    mavenCentral()
}

tasks.getByName<AntlrTask>("generateGrammarSource") {
    outputDirectory = file("${project.buildDir}/generated-src/antlr/main/io/github/oxisto/reticulated/grammar")
    arguments = arguments + listOf("-visitor", "-package", "io.github.oxisto.reticulated.grammar")
}

tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    dependsOn(":generateGrammarSource")
}

dependencies {
    antlr("org.antlr:antlr4:4.8-1")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
