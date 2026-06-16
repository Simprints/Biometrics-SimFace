package com.simprints.sample.ui.screens.image

import com.simprints.sample.ui.models.FaceResult

data class SimFaceTestImageUiState(
    val result1: FaceResult? = null,
    val result2: FaceResult? = null,
    val result3: FaceResult? = null,
    val result4: FaceResult? = null,
    val comparisonResult: String? = null,
    val isProcessing: Boolean = false,
    val isComparing: Boolean = false,
)
