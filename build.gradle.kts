plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    `maven-publish`
}

val projectGroupId = "com.simprints.biometrics"
val projectArtifactId = "simface"
val projectVersion = "2025.1.4"

android {

    namespace = "$projectGroupId.$projectArtifactId"
    compileSdk = 35

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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        mlModelBinding = true
    }
}

dependencies {

    // Tensorflow versions that works with Edgeface
    api(libs.tensorflow.lite.support)
    api(libs.tensorflow.lite.metadata)
    api(libs.tensorflow.lite)

    // Face Detection and quality
    api(libs.face.detection)

    // For face alignment
    api(libs.ejml.simple)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
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
