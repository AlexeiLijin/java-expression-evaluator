plugins {
    kotlin("jvm") version "2.0.20"
}

group = "alexeilijin"
version = "1.0"

repositories {
    mavenCentral()
}

val janinoVersion = "3.1.12"
dependencies {
    implementation("org.codehaus.janino:janino:$janinoVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}