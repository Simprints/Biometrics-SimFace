package com.simprints.simface.core

import com.simprints.simface.embedding.EmbeddingProcessor
import com.simprints.simface.embedding.TensorFlowEmbeddingProcessor
import com.simprints.simface.matcher.CosineDistanceMatchProcessor
import com.simprints.simface.matcher.MatchProcessor
import com.simprints.simface.quality.FaceDetectionProcessor
import com.simprints.simface.quality.MlKitFaceDetectionProcessor

class SimFace {
    private val initLock = Any()

    // Internal processors
    private lateinit var modelManager: MLModelManager
    private lateinit var embeddingProcessor: EmbeddingProcessor
    private lateinit var matchProcessor: MatchProcessor
    private lateinit var faceDetectionProcessor: FaceDetectionProcessor

    fun initialize(config: SimFaceConfig): Unit = synchronized(initLock) {
        try {
            // Initialize the model manager with the given config
            modelManager = MLModelManager()
            modelManager.loadModels(config.context)

            // Initialize processors
            embeddingProcessor = TensorFlowEmbeddingProcessor(modelManager)
            matchProcessor = CosineDistanceMatchProcessor()
            faceDetectionProcessor = MlKitFaceDetectionProcessor(modelManager)
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize SimFaceFacade: ${e.message}", e)
        }
    }

    fun release() = synchronized(initLock) {
        try {
            if (this::modelManager.isInitialized) {
                modelManager.close()
            }
        } catch (e: Exception) {
            println("Error releasing MLModelManager: ${e.message}")
        }
    }

    fun getTemplateVersion(): String = TEMPLATE_VERSION

    fun getEmbeddingProcessor(): EmbeddingProcessor {
        if (!this::embeddingProcessor.isInitialized) {
            throw IllegalStateException("SimFace.initialize() should be called first")
        }
        return embeddingProcessor
    }

    fun getMatchProcessor(): MatchProcessor {
        if (!this::matchProcessor.isInitialized) {
            throw IllegalStateException("SimFace.initialize() should be called first")
        }
        return matchProcessor
    }

    fun getFaceDetectionProcessor(): FaceDetectionProcessor {
        if (!this::matchProcessor.isInitialized) {
            throw IllegalStateException("SimFace.initialize() should be called first")
        }
        return faceDetectionProcessor
    }

    companion object {
        private const val TEMPLATE_VERSION = "SIM_FACE_BASE_1"
    }
}
