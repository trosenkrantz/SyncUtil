plugins {
    java
    alias(libs.plugins.bnd) // To add OSGi support
    alias(libs.plugins.errorprone) // To be able to apply Error Prone for lint checking
}

allprojects {
    version = "1.0.8"

    repositories {
        mavenCentral()
    }

    plugins.withType<JavaBasePlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
                vendor.set(JvmVendorSpec.AZUL)
            }
        }

        tasks.withType<Jar> {
            manifest {
                attributes(
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "RAPTOR"
                )
            }
        }

        // Apply the Error Prone plugin
        pluginManager.apply(libs.plugins.errorprone.get().pluginId)
        dependencies {
            errorprone(libs.errorprone.core)
        }

        tasks.withType<JavaCompile>().configureEach {
            options.compilerArgs.add("-Werror") // Converts all Java compiler warnings into errors, to accept no warnings
            options.release.set(8)
        }
    }
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
