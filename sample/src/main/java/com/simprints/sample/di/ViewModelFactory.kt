package com.simprints.sample.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.biometrics.spoofing.SimAntiSpoofing
import com.simprints.sample.ui.screens.camera.SimFaceCameraViewModel
import com.simprints.sample.ui.screens.image.SimFaceTestImageViewModel
import com.simprints.sample.wrappers.SampleImageLoader
import com.simprints.sample.wrappers.SimAntiSpoofingWrapper
import com.simprints.sample.wrappers.SimFaceWrapper

class SimFaceCameraViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimFaceCameraViewModel::class.java)) {
            return SimFaceCameraViewModel(
                simFace = SimFaceWrapper(application.applicationContext),
                antiSpoofing = SimAntiSpoofingWrapper(application.applicationContext),
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
