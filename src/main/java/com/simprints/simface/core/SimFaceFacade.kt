package com.simprints.simface.core

import com.simprints.simface.embedding.EmbeddingProcessor
import com.simprints.simface.embedding.IEmbeddingProcessor
import com.simprints.simface.matcher.IMatchProcessor
import com.simprints.simface.matcher.MatchProcessor
import com.simprints.simface.quality.FaceDetectionProcessor
import com.simprints.simface.quality.IFaceDetectionProcessoor

class SimFaceFacade private constructor(private val config: SimFaceConfig) {

    // Internal processors
    val embeddingProcessor: IEmbeddingProcessor
    val matchProcessor: IMatchProcessor
    val faceDetectionProcessor: IFaceDetectionProcessoor

    init {
        // Initialize the model manager with the given config
        MLModelManager.loadModels(config.context)

        // Initialize processors
        embeddingProcessor = EmbeddingProcessor()
        matchProcessor = MatchProcessor()
        faceDetectionProcessor = FaceDetectionProcessor()
    }

    companion object {
        @Volatile
        private var instance: SimFaceFacade? = null

        fun initialize(config: SimFaceConfig) {
            instance ?: synchronized(this) {
                instance ?: SimFaceFacade(config).also { instance = it }
            }
        }

        fun getInstance(): SimFaceFacade {
            return instance ?: throw Exception("Library not initialized. Call initialize() first.")
        }

        fun release() {
            MLModelManager.close()
            instance = null
        }
    }
}