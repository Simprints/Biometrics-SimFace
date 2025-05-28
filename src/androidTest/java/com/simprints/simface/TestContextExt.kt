package com.simprints.simface

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Resources IDs are not available via the IDE tooling and therefore to run
 * the tests the identifiers have to be resolved manually.
 */
internal fun Context.loadBitmapFromTestResources(drawableName: String): Bitmap {
    val resourceId = resources.getIdentifier(drawableName, "drawable", packageName)
    if (resourceId == 0) {
        throw IllegalStateException("Test resource '$drawableName' not found in package '$packageName'")
    }
    return BitmapFactory.decodeResource(resources, resourceId)
        ?: throw IllegalStateException("BitmapFactory failed to decode resource '$drawableName'")
}
