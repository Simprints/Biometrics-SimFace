package com.simprints.biometrics.simface.embedding

import android.graphics.Bitmap

internal interface EmbeddingProcessor {
    fun getEmbedding(bitmap: Bitmap): ByteArray
}
