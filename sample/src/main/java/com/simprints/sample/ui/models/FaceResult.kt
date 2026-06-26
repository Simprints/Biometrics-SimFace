package com.simprints.sample.ui.models

import android.graphics.Bitmap
import com.simprints.biometrics.simface.data.FaceDetection

data class FaceResult(
    val bitmap: Bitmap?,
    val success: Boolean,
    val message: String,
    val faces: List<FaceDetection>,
    val embedding: ByteArray? = null,
)
