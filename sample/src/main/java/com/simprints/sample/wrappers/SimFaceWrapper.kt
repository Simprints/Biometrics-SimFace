package com.simprints.sample.wrappers

import android.content.Context
import android.graphics.Bitmap
import com.simprints.biometrics.simface.SimFace
import com.simprints.biometrics.simface.SimFaceConfig
import com.simprints.biometrics.simface.data.FaceDetection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SimFaceWrapper(
    context: Context,
) {
    private val simFace = SimFace().apply { initialize(SimFaceConfig(context)) }

    suspend fun detectFaces(bitmap: Bitmap): List<FaceDetection> =
        withContext(Dispatchers.IO) { simFace.detectFaceBlocking(bitmap) }

    suspend fun getEmbedding(
        face: FaceDetection,
        sourceBitmap: Bitmap,
    ): ByteArray? = withContext(Dispatchers.IO) {
        val alignedFace = face.alignedFaceImage(sourceBitmap)
        simFace.getEmbedding(alignedFace)
    }

    suspend fun verificationScore(
        embedding1: ByteArray,
        embedding2: ByteArray,
    ): Double = withContext(Dispatchers.IO) { simFace.verificationScore(embedding1, embedding2) }

    fun release() {
        simFace.release()
    }
}
