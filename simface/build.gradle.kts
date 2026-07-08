import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

val projectGroupId = "com.simprints.biometrics"
val projectArtifactId = "simface"
val projectVersion = "2026.1.0"

group = projectGroupId
version = projectVersion

android {

    namespace = "$projectGroupId.$projectArtifactId"
    compileSdk = 37

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    api(project(":simq"))

    // Tensorflow versions that works with Edgeface
    api(libs.litert)
    api(libs.litert.support)

    // Face Detection and quality
    api(libs.face.detection)

    // For face alignment
    api(libs.ejml.simple)

    androidTestImplementation(libs.test.truth)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.espresso)
    androidTestImplementation(libs.test.coroutine)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/simprints/Biometrics-SimFace")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("ReleaseAar") {
            groupId = projectGroupId
            artifactId = projectArtifactId
            version = projectVersion
            afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }

            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")

                configurations.getByName("api").dependencies.map { dependency ->
                    dependenciesNode.appendNode("dependency").also {
                        it.appendNode("groupId", dependency.group)
                        it.appendNode("artifactId", dependency.name)
                        it.appendNode("version", dependency.version)
                    }
                }
            }
        }
    }
}
