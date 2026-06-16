package com.simprints.sample.ui.models

import com.simprints.sample.SimFaceDestination

data class SimFaceUiState(
    val result1: FaceResult? = null,
    val result2: FaceResult? = null,
    val result3: FaceResult? = null,
    val result4: FaceResult? = null,
    val capturedImage1: FaceResult? = null,
    val capturedImage2: FaceResult? = null,
    val comparisonResult: String? = null,
    val isProcessing: Boolean = false,
    val isComparing: Boolean = false,
    val backStack: List<SimFaceDestination> = listOf(SimFaceDestination.Main),
    val cameraTarget: CameraTarget = CameraTarget.FACE_1,
)
