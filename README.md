# SimFace

An Android library for face recognition and quality assessment on edge devices.
It provides face detection, embedding creation, and biometric matching capabilities.
The library is built around [Google's MLKit](https://developers.google.com/ml-kit/vision/face-detection) for face
detection and [EdgeFace](https://github.com/otroshi/edgeface) for embedding (template) creation. It consists of the following modules:

1. Face Detection and Quality Assessment: This module is used to locate faces within an image and assess their quality.
2. Embedding Creation: This module is used to generate a 512-float vector representation of face images.
3. Matching and Identification: Used for verification and identification purposes.

## Include the library into the project.

### Option 1 (Recommended)

1. Add the repository to your ```settings.gradle.kts``` under ```dependencyResolutionManagement``` under ```respositories```:

```kotlin
maven {
    url = uri("https://maven.pkg.github.com/Simprints/Biometrics-SimFace")
    credentials {
        username = System.getenv("USERNAME") ?: ""
        password = System.getenv("TOKEN") ?: ""
    }
}
```

2. Import the dependencies in ```build.gradle.kts```:

```kotlin
implementation("com.simprints.biometrics:simface:2025.4.0")
```

## Implement the functionality.

### Coroutines (Recommended)

```kotlin
// Initialize library configuration
val simFace = SimFace()
val simFaceConfig = SimFaceConfig(context)
simFace.initialize(simFaceConfig)

// Load a bitmap image for processing
val faceImage: Bitmap =
    BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_good_face)

lifecycleScope.launch {
    val faces = simFace.detectFaceBlocking(faceImage)
    val face = faces[0]
    if (faces.size != 1 || face.quality < 0.6) throw Exception("Quality not sufficient")

    // Align and crop the image of the face
    val alignedFace = face.alignedFaceImage(bitmap)

    // Generate an embedding from the image
    val probe = simFace.getFaceDetectionProcessor().getEmbedding(alignedFace)

    // Verify the embedding against itself
    val score = simFace.verificationScore(probe, probe)
}
```

### Callbacks

```kotlin
// Initialize library configuration
val simFace = SimFace()
val simFaceConfig = SimFaceConfig(context)
simFace.initialize(simFaceConfig)

// Load a bitmap image for processing
val faceImage: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_good_face)

// Note that this can be better handled with callbacks or coroutines
simFace.getFaceDetectionProcessor().detectFace(faceImage, onSuccess = { faces ->
    val face = faces[0]
    if (faces.size != 1 || face.quality < 0.6) throw Exception("Quality not sufficient")

    // Align and crop the image of the face
    val alignedFace = face.alignedFaceImage(bitmap)

    // Generate an embedding from the image
    val probe = simFace.getEmbedding(alignedFace)

    // Verify the embedding against itself
    val score = simFace.verificationScore(probe, probe)
})
```

## Workflow

### Face Detection and Embedding

We first initialize the library. Then we can use the
```simFace.detectFace``` method to detect faces in images and evaluate their quality.
We can repeat this process multiple times until a sufficiently good face image is selected.

Afterwards, we can use the ```simFace.getEmbedding``` method to obtain a vector template
from the selected image. The embedding is represented by a 512 float array.

### Verification and Identification

The same steps are taken to initialize the library.

Then, the matching of two templates is carried out using the ```simFace.verificationScore``` method,
which returns a score in the [0, 1] range, being 1 a perfect match.

Identification can be carried using the ```simFace.identificationScore``` method which returns a mapping of the
referenceVectors to the the score with respect to the probe.

Both methods use the cosine similarity between vectors as a measure of the score.

## System Requirements

The library works with a minimum version of Android 6.0 (API Level 23). It has been tested and runs
smoothly on *Samsung Galaxy A03 Core* which has the following specifications:

- Android 11
- 1.6GHz Octa-core
- 2GB RAM
- 8MP f/2.0 Camera
- 32GB Storage



