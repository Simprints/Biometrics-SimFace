package com.simprints.sample

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

interface SampleImageLoader {
    fun load(imageRes: Int): Bitmap?
}

class AndroidSampleImageLoader(
    private val resources: Resources,
) : SampleImageLoader {
    override fun load(imageRes: Int): Bitmap? = BitmapFactory.decodeResource(resources, imageRes)
}

