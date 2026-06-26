package com.simprints.sample.wrappers

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class SampleImageLoader(
    private val resources: Resources,
) {
    fun load(imageRes: Int): Bitmap? = BitmapFactory.decodeResource(resources, imageRes)
}
