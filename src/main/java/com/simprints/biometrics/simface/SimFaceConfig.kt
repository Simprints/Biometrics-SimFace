package com.simprints.biometrics.simface

import android.content.Context
import com.simprints.simq.QualityParameters
import com.simprints.simq.QualityWeights
import java.io.File

data class SimFaceConfig(
        val applicationContext: Context,
        /**
         * Custom model file to use instead of the bundled one. If not set, the bundled model will
         * be used. The custom model's inputs and outputs vectors must match the default SimFace
         * model.
         */
        val customModel: CustomModel? = null,
        /**
         * Custom quality weights for face quality assessment. If not set, default weights will be
         * used.
         */
        val qualityWeights: QualityWeights? = null,
        /**
         * Custom quality parameters for face quality assessment. If not set, default parameters
         * will be used.
         */
        val qualityParameters: QualityParameters? = null,
) {
        data class CustomModel(
                val file: File,
                val templateVersion: String,
        )
}
