package com.simprints.simq.shadows

import android.graphics.Bitmap
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(org.opencv.android.Utils::class)
class ShadowOpenCVUtils {
    
    companion object {
        @JvmStatic
        @Implementation
        fun bitmapToMat(bitmap: Bitmap, mat: Any) {
            // Mock implementation
        }
        
        @JvmStatic
        @Implementation
        fun matToBitmap(mat: Any, bitmap: Bitmap) {
            // Mock implementation
        }
    }
}
