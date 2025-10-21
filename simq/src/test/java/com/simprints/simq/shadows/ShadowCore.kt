package com.simprints.simq.shadows

import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(org.opencv.core.Core::class)
class ShadowCore {
    
    companion object {
        @JvmStatic
        @Implementation
        fun meanStdDev(src: Any, mean: Any, stddev: Any) {
            // The MatOfDouble shadows will handle returning default values
        }
        
        @JvmStatic
        @Implementation
        fun mean(src: Any): Any {
            // Return a mock scalar
            return Any()
        }
    }
}
