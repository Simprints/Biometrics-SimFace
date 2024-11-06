package com.simprints.simface.quality

import android.graphics.Bitmap
import com.simprints.simface.core.SimFace

interface IFaceDetectionProcessoor {
    suspend fun detectFace(bitmap: Bitmap): List<SimFace>
}