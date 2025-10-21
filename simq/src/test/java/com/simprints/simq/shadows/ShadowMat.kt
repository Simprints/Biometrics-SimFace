package com.simprints.simq.shadows

import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(org.opencv.core.Mat::class)
class ShadowMat {
    
    @Implementation
    fun __constructor__() {
        // Mock constructor
    }
    
    @Implementation
    fun __constructor__(rows: Int, cols: Int, type: Int) {
        // Mock constructor
    }
    
    @Implementation
    fun release() {
        // Mock implementation - does nothing
    }
    
    @Implementation
    fun rows(): Int {
        return 0
    }
    
    @Implementation
    fun cols(): Int {
        return 0
    }
}
