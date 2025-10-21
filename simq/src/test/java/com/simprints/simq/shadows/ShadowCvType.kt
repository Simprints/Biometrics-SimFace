package com.simprints.simq.shadows

import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(org.opencv.core.CvType::class)
class ShadowCvType {
    
    companion object {
        const val CV_8U = 0
        const val CV_8S = 1
        const val CV_16U = 2
        const val CV_16S = 3
        const val CV_32S = 4
        const val CV_32F = 5
        const val CV_64F = 6
        const val CV_8UC1 = 0
        const val CV_8UC3 = 16
        const val CV_8UC4 = 24
    }
}
