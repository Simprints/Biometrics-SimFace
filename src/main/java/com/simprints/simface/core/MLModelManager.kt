package com.simprints.simface.core

import android.content.Context
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.simprints.biometrics.simface.ml.EdgefaceSGamma05

object MLModelManager {

    private lateinit var faceEmbeddingModel: EdgefaceSGamma05
    private lateinit var faceDetector: FaceDetector

    fun loadModels(context: Context) {
        // Load Face Embedding Model
        faceEmbeddingModel = EdgefaceSGamma05.newInstance(context)

        // Configure and load MLKit face detection model
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.20f)
            .build()

        faceDetector = FaceDetection.getClient(realTimeOpts)
    }

    fun getFaceEmbeddingModel(): EdgefaceSGamma05 = faceEmbeddingModel
    fun getFaceDetector(): FaceDetector = faceDetector

    // Close method to release resources
    fun close() {
        faceEmbeddingModel.close()
        faceDetector.close()
    }

}