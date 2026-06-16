package com.simprints.sample

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.sample.ui.SimFaceCameraViewModel
import com.simprints.sample.ui.SimFaceTestImageViewModel
import com.simprints.sample.wrappers.SampleImageLoader
import com.simprints.sample.wrappers.SimFaceWrapper

class SimFaceCameraViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimFaceCameraViewModel::class.java)) {
            return SimFaceCameraViewModel(
                repository = SimFaceWrapper(application.applicationContext),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class SimFaceTestImageDemoViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimFaceTestImageViewModel::class.java)) {
            return SimFaceTestImageViewModel(
                repository = SimFaceWrapper(application.applicationContext),
                imageLoader = SampleImageLoader(application.resources),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
