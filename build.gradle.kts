plugins {
  id("org.jetbrains.kotlin.jvm") version "1.3.72"
  antlr
  idea
  `java-library`
  `maven-publish`
  jacoco

  id("org.sonarqube") version "3.0"
}

idea {
  module {
    generatedSourceDirs.add(file("${project.buildDir}/generated-src/antlr/main"))
  }
}

group = "io.github.oxisto"

repositories {
  mavenCentral()
}

tasks.getByName<AntlrTask>("generateGrammarSource") {
  outputDirectory = file("${project.buildDir}/generated-src/antlr/main/io/github/oxisto/cppparser/grammar")
  arguments = arguments + listOf("-visitor", "-package", "io.github.oxisto.cppparser.grammar")
}

tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
  dependsOn(":generateGrammarSource")
}

dependencies {
  antlr("org.antlr:antlr4:4.8-1")

  implementation("io.github.microutils:kotlin-logging:1.8.3")
  implementation("org.slf4j:slf4j-simple:1.7.29")

  // Align versions of all Kotlin components
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  // Use the Kotlin JDK 8 standard library.
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  // Use the Kotlin test library.
  testImplementation("org.jetbrains.kotlin:kotlin-test")

  // Use the Kotlin JUnit integration.
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks.jacocoTestReport {
  reports {
    xml.isEnabled = true
  }
}

tasks {
  test {
    systemProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG")
  }
}

val mavenCentralUri: String
  get() {
    val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
    return if ((version as String).endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
  }

publishing {
  repositories {
    maven {
      url = uri("https://maven.pkg.github.com/oxisto/reticulated-python")

      credentials {
        val gitHubUsername: String? by project
        val gitHubToken: String? by project

        username = gitHubUsername
        password = gitHubToken
      }
    }

    maven {
      url = uri(mavenCentralUri)

      credentials {
        val mavenCentralUsername: String? by project
        val mavenCentralPassword: String? by project

        username = mavenCentralUsername
        password = mavenCentralPassword
      }
    }
  }

  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
    }
  }
}

tasks.withType<GenerateModuleMetadata> {
  enabled = false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.jvmTarget = "11"
}

/*signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications["maven"])
}*/
