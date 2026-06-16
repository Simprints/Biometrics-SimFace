package com.simprints.sample

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.sample.wrappers.SampleImageLoader
import com.simprints.sample.wrappers.SimFaceWrapper

class SimFaceDemoViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimFaceDemoViewModel::class.java)) {
            return SimFaceDemoViewModel(
                repository = SimFaceWrapper(application.applicationContext),
                imageLoader = SampleImageLoader(application.resources),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
