SimFace is an Android library (written in Kotlin) for face recognition and quality assessment on the edge. It provides face detection, embedding creation, and biometric matching capabilities. The library is built around [Google's MLKit](https://developers.google.com/ml-kit/vision/face-detection) for face detection and [EdgeFace](https://github.com/otroshi/edgeface) for embedding (template) creation. It consists of the following modules:

1. Face Detection and Quality Assessment: This module is used to locate faces and assess their quality in an image.
2. Embedding Creation: This module is used to generate a 512-float vector representation of faces.
3. Matching and Identification: Used for verification and identification purposes.

# Repository tree directory

The ```src``` folder contains two subfolders:
- ```src/main```, containing the main library source code.
- ```src/androidTest``, which contains a set of tests for the functionality of the library.

# Class overview 

In this section we provide a description of the ```SimFace``` Library.

## Data classes

- ```data class SimFaceConfig(val context: Context)```: Contains configuration data required for initializing the library.
- ```data class SimFace(val sourceWidth: Int, val sourceHeight: Int, val absoluteBoundingBox: Rect, val yaw: Float, var roll: Float, val quality: Float)```: Contains data about Faces extracted from images.

## Components and Function Definitions

- SimFaceFacade: used as a centralized point to initialize and access the library.
- MLModelManager: used to initialize and load the machine learning models required for running the library. At the moment these are EdgeFace and MLKit.
- Face Detection Processor: used for detection of faces and determining the quality of the capture.
  - ```suspend fun detectFace(bitmap: Bitmap): List<SimFace>```
- Embedding Processor: used for creating vector embeddings for face images.
  - ```fun getEmbedding(bitmap: Bitmap): List<Float>```
- Match Processor: used for comparing biometric embeddings (templates).
  - ```fun verificationScore(probe: FloatArray, matchAgainst: FloatArray): Float```
  - ```fun identificationScore(probe: FloatArray, matchReferences: List<FloatArray>): Map<FloatArray, Float>```

# Workflow

In this section we report the sequence diagrams are included for integrating SimFace in production systems.

## Face Detection and Embedding

We first initialize the library and get an instance of the facade. Then we can use the ```faceDetectionProcessor.detectFace``` method to detect faces in images and evaluate their quality. We can repeat this process multiple times until a sufficiently good face image is selected. Afterwards, we can use the ```embeddingProcessor.getEmbedding``` method to obtain a vector template from the selected image. The embedding is represented by a 512 float array.

![alt text](diagrams/Face%20Detection%20and%20Embedding%20Sequence%20Diagram.png)

## Verification and Identification

The same steps are taken to initialize the library and get an instance of the facade. Then, the matching of two templates is carried out using the ```matchProcessor.verificationScore``` method, which returns a score in the [0, 1] range, being 1 a perfect match. Identification can be carried using the ```matchProcessor.identificationScore``` method which returns a mapping of the referenceVectors to the the score with respect to the probe. Both methods use the cosine similarity between vectors as a measure of the score.

![alt text](diagrams/Verification%20and%20Identification.png)

# Initializing and usage

For more details on how you can use the library, take a look at the test cases in ```src/androidTest```.

```
// Initialize library configuration
val simFaceConfig = SimFaceConfig(context)
SimFaceFacade.initialize(simFaceConfig)
val simFace = SimFaceFacade.getInstance()

// Load a bitmap image for processing
val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_good_face)

// Detect faces and assess quality
val faces = simFace.qualityProcessor.detectFace(bitmap)
if (faces.size != 1 || faces[0].quality < 0.6) throw Exception("Quality not sufficient")

// Generate an embedding from the image
val probe = simFace.embeddingProcessor.getEmbedding(bitmap)

// Verify the embedding against itself as an example
val score = simFace.matchProcessor.verificationScore(probe, probe)
println("Verification score: $score")
```