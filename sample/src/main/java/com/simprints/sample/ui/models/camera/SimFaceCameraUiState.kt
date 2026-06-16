package com.simprints.sample.ui.models.camera

import com.simprints.sample.ui.models.FaceResult

data class SimFaceCameraUiState(
    val cameraTarget: CameraTarget = CameraTarget.FACE_1,
    val capturedImage1: FaceResult? = null,
    val capturedImage2: FaceResult? = null,
    val comparisonResult: String? = null,
    val isProcessing: Boolean = false,
    val isComparing: Boolean = false,
)
