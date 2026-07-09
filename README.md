# Simprints Face Biometrics SDK

## SimFace Library

An Android library for face recognition and quality assessment on edge devices.
It provides face detection, embedding creation, and biometric matching capabilities.
The library is built around [Google's MLKit](https://developers.google.com/ml-kit/vision/face-detection) for face
detection and [EdgeFace](https://github.com/otroshi/edgeface) for embedding (template) creation. It consists of the following modules:

1. Face Detection and Quality Assessment: This module is used to locate faces within an image and assess their quality.
2. Embedding Creation: This module is used to generate a 512-float vector representation of face images.
3. Matching and Identification: Used for verification and identification purposes.

**📚 [View Full Documentation](simface/README.md)** for installation and usage examples.

## SimQ Library

**SimQ** is a standalone Android library for comprehensive face quality assessment. It can be used independently or as part of SimFace for
enhanced quality evaluation. SimQ provides a quality score from 0.0 to 1.0, with customizable weights for each metric and configurable
thresholds.

**📚 [View Full SimQ Documentation](simq/README.md)** for installation, usage examples, and advanced configuration options.

### Installation

1. Add the repository to your `settings.gradle.kts` under `dependencyResolutionManagement` under `respositories`:

```kotlin
maven {
    url = uri("https://maven.pkg.github.com/Simprints/Biometrics-SimFace")
    credentials {
        username = System.getenv("USERNAME") ?: ""
        password = System.getenv("TOKEN") ?: ""
    }
}
```

Import the dependencies in `build.gradle.kts`:

```kotlin
// To add the full Biometric SDK
implementation("com.simprints.biometrics:simface:2026.2.0")

// Or if only quality assessment is needed
implementation("com.simprints.biometrics:simq:2026.2.0")
```

## SimFace Demo app

The `sample` module demonstrates end-to-end usage of SimFace APIs in a minimal app.
It is intended as a reference implementation for integration and testing.

Usage: Open the repository in Android Studio and run the `sample` module.
