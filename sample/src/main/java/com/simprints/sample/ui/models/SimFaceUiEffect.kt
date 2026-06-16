package com.simprints.sample.ui.models

sealed interface SimFaceUiEffect {
    data class ImageProcessingError(
        val source: ImageSource,
        val message: String,
    ) : SimFaceUiEffect

    data class ComparisonError(val message: String) : SimFaceUiEffect

    enum class ImageSource {
        CAPTURE,
        TEST_IMAGE,
    }

}
