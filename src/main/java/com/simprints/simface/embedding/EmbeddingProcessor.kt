package com.simprints.simface.embedding

import android.graphics.Bitmap

internal interface EmbeddingProcessor {
    fun getEmbedding(bitmap: Bitmap): ByteArray
}
