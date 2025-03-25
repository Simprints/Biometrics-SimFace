package com.simprints.simface.core

import com.simprints.simface.embedding.TensorFlowEmbeddingProcessor
import com.simprints.simface.embedding.EmbeddingProcessor
import com.simprints.simface.matcher.MatchProcessor
import com.simprints.simface.matcher.CosineDistanceMatchProcessor
import com.simprints.simface.quality.MlKitFaceDetectionProcessor
import com.simprints.simface.quality.FaceDetectionProcessor

class SimFaceFacade private constructor(private val config: SimFaceConfig) {

    // Internal processors
    val embeddingProcessor: EmbeddingProcessor
    val matchProcessor: MatchProcessor
    val faceDetectionProcessor: FaceDetectionProcessor

    init {
        try {
            // Initialize the model manager with the given config
            MLModelManager.loadModels(config.context)

            // Initialize processors
            embeddingProcessor = TensorFlowEmbeddingProcessor()
            matchProcessor = CosineDistanceMatchProcessor()
            faceDetectionProcessor = MlKitFaceDetectionProcessor()
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize SimFaceFacade: ${e.message}", e)
        }
    }

    companion object {
        @Volatile
        private var instance: SimFaceFacade? = null

        fun initialize(config: SimFaceConfig) {
            synchronized(this) {
                try {
                    instance ?: SimFaceFacade(config).also { instance = it }
                } catch (e: Exception) {
                    throw RuntimeException("Failed to initialize SimFaceFacade: ${e.message}", e)
                }
            }
        }

        fun getInstance(): SimFaceFacade {
            return instance ?: throw IllegalStateException("Library not initialized. Call initialize() first.")
        }

        fun release() {
            try {
                MLModelManager.close()
            } catch (e: Exception) {
                println("Error releasing MLModelManager: ${e.message}")
            } finally {
                instance = null
            }
        }
    }
}
