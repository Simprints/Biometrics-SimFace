package com.simprints.simface.embedding

import android.graphics.Bitmap

interface IEmbeddingProcessor {
    fun getEmbedding(bitmap: Bitmap): List<Float>
}