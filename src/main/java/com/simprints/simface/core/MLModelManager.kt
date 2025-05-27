package com.simprints.simface.core

import android.content.Context
import com.simprints.biometrics.simface.ml.EdgefaceSGamma05

internal class MLModelManager {
    private lateinit var faceEmbeddingModel: EdgefaceSGamma05

    fun loadModels(context: Context) {
        // Load Face Embedding Model
        faceEmbeddingModel = EdgefaceSGamma05.newInstance(context)
    }

    fun getFaceEmbeddingModel(): EdgefaceSGamma05 = faceEmbeddingModel

    fun close() {
        faceEmbeddingModel.close()
    }
}
