package com.simprints.sample

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.sample.data.FaceRepository

class SimFaceDemoViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimFaceDemoViewModel::class.java)) {
            return SimFaceDemoViewModel(
                repository = FaceRepository(application.applicationContext),
                imageLoader = AndroidSampleImageLoader(application.resources),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
