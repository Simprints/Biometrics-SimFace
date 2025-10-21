package com.simprints.simq.shadows

import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(org.opencv.core.MatOfDouble::class)
class ShadowMatOfDouble {
    
    private var values: DoubleArray = doubleArrayOf(50.0) // Default standard deviation for mock
    
    @Implementation
    fun __constructor__() {
        // Mock constructor
    }
    
    @Implementation
    fun toArray(): DoubleArray {
        return values
    }
    
    @Implementation
    fun release() {
        // Mock implementation - does nothing
    }
}
