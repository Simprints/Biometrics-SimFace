package com.simprints.simface.core

import com.simprints.simface.embedding.EmbeddingProcessor
import com.simprints.simface.embedding.IEmbeddingProcessor
import com.simprints.simface.matcher.IMatchProcessor
import com.simprints.simface.matcher.MatchProcessor
import com.simprints.simface.quality.FaceDetectionProcessor
import com.simprints.simface.quality.IFaceDetectionProcessor

class SimFaceFacade private constructor(private val config: SimFaceConfig) {

    // Internal processors
    val embeddingProcessor: IEmbeddingProcessor
    val matchProcessor: IMatchProcessor
    val faceDetectionProcessor: IFaceDetectionProcessor

    init {
        try {
            // Initialize the model manager with the given config
            MLModelManager.loadModels(config.context)

            // Initialize processors
            embeddingProcessor = EmbeddingProcessor()
            matchProcessor = MatchProcessor()
            faceDetectionProcessor = FaceDetectionProcessor()
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