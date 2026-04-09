plugins {
    java
    alias(libs.plugins.bnd) // To add OSGi support
}

version = "1.0.8"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.AZUL)
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // For testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testRuntimeOnly(libs.junit.launcher) // Declaring test framework explicitly as recommended by Gradle: https://docs.gradle.org/8.5/userguide/upgrading_version_8.html#test_framework_implementation_dependencies
}

tasks.jar {
    bundle {
        bnd(mapOf("-exportcontents" to "com.github.trosenkrantz.sync.util.*"))
    }
}

tasks.test {
    useJUnitPlatform()
}
