SimFace is an Android library (written in Kotlin) for face recognition and quality assessment on
edge devices. It provides face detection, embedding creation, and biometric matching capabilities.
The library is built
around [Google's MLKit](https://developers.google.com/ml-kit/vision/face-detection) for face
detection and [EdgeFace](https://github.com/otroshi/edgeface) for embedding (template) creation. It
consists of the following modules:

1. Face Detection and Quality Assessment: This module is used to locate faces within an image and
   assess their quality.
2. Embedding Creation: This module is used to generate a 512-float vector representation of face
   images.
3. Matching and Identification: Used for verification and identification purposes.

# Initialization and usage

For more details on how you can use the library, take a look at the test cases in
```src/androidTest```.

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

2. Import the dependency in ```build.graddle.kts```:

```kotlin
implementation("com.simprints:biometrics_simface:2024.4.3")
```

### Option 2

1. Download the ```.aar``` package and put it in an ```app/libs``` folder.
2. Add the folloowing dependencies in ```build.graddle.kts```.

```kotlin
// SimFace Support
implementation(files("libs/biometrics_simface-2024.4.3.aar"))
implementation("com.google.mlkit:face-detection:16.1.6")
implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
```

## Implement the functionality.

### Non-blocking (Recommended)

```kotlin
// Initialize library configuration
val simFaceConfig = SimFaceConfig(context)
SimFaceFacade.initialize(simFaceConfig)
val simFace = SimFaceFacade.getInstance()

// Load a bitmap image for processing
val faceImage: Bitmap =
    BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_good_face)

// Note that this can be better handled with callbacks or coroutines
simFace.faceDetectionProcessor.detectFace(faceImage, onSuccess = { faces ->
    val face = faces[0]
    if (faces.size != 1 || face.quality < 0.6) throw Exception("Quality not sufficient")

    // Align and crop the image to the face bounding box
    val alignedFace = simFace.faceDetectionProcessor.alignFace(faceImage, face.absoluteBoundingBox)

    // Generate an embedding from the image
    val probe = simFace.embeddingProcessor.getEmbedding(alignedFace)

    // Verify the embedding against itself
    val score = simFace.matchProcessor.verificationScore(probe, probe)
})
```

### Blocking

```kotlin
// Initialize library configuration
val simFaceConfig = SimFaceConfig(context)
SimFaceFacade.initialize(simFaceConfig)
val simFace = SimFaceFacade.getInstance()

// Load a bitmap image for processing
val alignedFace: Bitmap =
    BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_good_face)

lifecycleScope.launch {
    val faces = simFace.faceDetectionProcessor.detectFaceBlocking(alignedFace)
    val face = faces[0]
    if (faces.size != 1 || face.quality < 0.6) throw Exception("Quality not sufficient")

    // Align and crop the image to the face bounding box
    val alignedFace = simFace.faceDetectionProcessor.alignFace(faceImage, face.absoluteBoundingBox)

    // Generate an embedding from the image
    val probe = simFace.embeddingProcessor.getEmbedding(alignedFace)

    // Verify the embedding against itself
    val score = simFace.matchProcessor.verificationScore(probe, probe)
}
```

# Repository tree directory and Class overview

In this section we provide a description of the ```SimFace``` Library. The ```src``` folder contains
two subfolders:

- ```src/main```, containing the main library source code.
- ```src/androidTest```, which contains a set of tests for the functionality of the library.

## Data classes

- ```data class SimFaceConfig(val context: Context)```: Contains configuration data required for
  initializing the library.
- ```data class SimFace(val sourceWidth: Int, val sourceHeight: Int, val absoluteBoundingBox: Rect, val yaw: Float, var roll: Float, val quality: Float)```:
Contains data about Faces extracted from images.

## Components and Function Definitions

- SimFaceFacade: used as a centralized point to initialize and access the library.
- MLModelManager: used to initialize and load the machine learning models required for running the
  library. At the moment these are EdgeFace and MLKit.
- Face Detection Processor: used for detection of faces and determining the quality of the capture.
    - ```fun detectFace(image: Bitmap, onSuccess: (List<SimFace>) -> Unit,onFailure: (Exception) -> Unit = {},onCompleted: () -> Unit = {})```
    - ```fun detectFaceBlocking(image: Bitmap): List<SimFace>```
    - ```fun alignFace(bitmap: Bitmap, faceBoundingBox: Rect): Bitmap```
- Embedding Processor: used for creating vector embeddings for face images.
    - ```fun getEmbedding(bitmap: Bitmap): ByteArray```
- Match Processor: used for comparing biometric embeddings (templates).
    - ```fun verificationScore(probe: ByteArray, matchAgainst: ByteArray): Float```
    - ```fun identificationScore(probe: ByteArray, matchReferences: List<ByteArray>): Map<ByteArray, Float>```

# Workflow

In this section we report the sequence diagrams are included for integrating SimFace in production
systems.

## Face Detection and Embedding

We first initialize the library and get an instance of the facade. Then we can use the
```faceDetectionProcessor.detectFace``` method to detect faces in images and evaluate their quality.
We can repeat this process multiple times until a sufficiently good face image is selected.
Afterwards, we can use the ```embeddingProcessor.getEmbedding``` method to obtain a vector template
from the selected image. The embedding is represented by a 512 float array.

![alt text](diagrams/Face%20Detection%20and%20Embedding%20Sequence%20Diagram.png)

## Verification and Identification

The same steps are taken to initialize the library and get an instance of the facade. Then, the
matching of two templates is carried out using the ```matchProcessor.verificationScore``` method,
which returns a score in the [0, 1] range, being 1 a perfect match. Identification can be carried
using the ```matchProcessor.identificationScore``` method which returns a mapping of the
referenceVectors to the the score with respect to the probe. Both methods use the cosine similarity
between vectors as a measure of the score.

![alt text](diagrams/Verification%20and%20Identification.png)

# System Requirements

The library works with a minimum version of Android 7.0 (API Level 24). It has been tested and runs
smoothly on *Samsung Galaxy A03 Core* which has the following specifications:

- Android 11
- 1.6GHz Octa-core
- 2GB RAM
- 8MP f/2.0 Camera
- 32GB Storage



