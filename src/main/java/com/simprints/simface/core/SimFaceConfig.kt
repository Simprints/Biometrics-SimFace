package com.simprints.simface.core

import android.content.Context
import java.io.File

data class SimFaceConfig(
    val applicationContext: Context,
    /**
     * Custom model file to use instead of the bundled one. If not set, the bundled model will be used.
     * The custom model's inputs and outputs vectors must much the default SimFace model.
     */
    val customModel: CustomModel? = null,
) {
    data class CustomModel(
        val file: File,
        val templateVersion: String,
    )
}
