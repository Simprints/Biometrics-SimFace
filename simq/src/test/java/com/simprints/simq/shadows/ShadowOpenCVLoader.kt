package com.simprints.simq.shadows

import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(org.opencv.android.OpenCVLoader::class)
class ShadowOpenCVLoader {
    
    companion object {
        @JvmStatic
        @Implementation
        fun initLocal(): Boolean {
            // Return true to simulate successful OpenCV initialization
            return true
        }
        
        @JvmStatic
        @Implementation
        fun initDebug(): Boolean {
            return true
        }
    }
}
