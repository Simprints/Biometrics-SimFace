package com.simprints.simq.shadows

import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(org.opencv.imgproc.Imgproc::class)
class ShadowImgproc {
    
    companion object {
        const val COLOR_BGR2GRAY = 6
        const val COLOR_RGB2GRAY = 7
        
        @JvmStatic
        @Implementation
        fun cvtColor(src: Any, dst: Any, code: Int) {
            // Mock implementation
        }
        
        @JvmStatic
        @Implementation
        fun Laplacian(src: Any, dst: Any, ddepth: Int, ksize: Int = 1, scale: Double = 1.0, delta: Double = 0.0) {
            // Mock implementation
        }
    }
}
