plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.intellij") version "1.1.6"
}



group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set("2021.1.2")
    type.set("IC") // IntelliJ Community Edition
    plugins.set(listOf("git4idea","org.sonarlint.idea"))
}

tasks {
    patchPluginXml {
        version.set(project.version.toString())
        sinceBuild.set("211")
        untilBuild.set("211.*")
    }
}
