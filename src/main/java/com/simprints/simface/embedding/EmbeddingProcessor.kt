package com.simprints.simface.embedding

import android.graphics.Bitmap

interface EmbeddingProcessor {
    fun getEmbedding(bitmap: Bitmap): ByteArray
}
