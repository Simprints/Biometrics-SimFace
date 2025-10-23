package com.simprints.simq.utils

import android.util.Log
import org.opencv.android.OpenCVLoader as AndroidOpenCVLoader

/**
 * Singleton object to handle OpenCV library loading.
 * Ensures the native library is loaded only once during the application lifecycle.
 */
object OpenCVLoader {
    private const val TAG = "OpenCV"

    fun init() {
        if (!AndroidOpenCVLoader.initLocal()) {
            Log.e(TAG, "OpenCV not loaded!")
        } else {
            Log.d(TAG, "OpenCV loaded successfully!")
        }
    }
}
