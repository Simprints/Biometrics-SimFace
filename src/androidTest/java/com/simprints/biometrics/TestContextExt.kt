package com.simprints.biometrics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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

internal fun Context.openTestModelFile(resourceName: String = "edgeface_test"): File {
    val resourceId = resources.getIdentifier(resourceName, "raw", packageName)
    if (resourceId == 0) {
        throw IllegalStateException("Test resource '$resourceName' not found in package '$packageName'")
    }

    val inputStream: InputStream = resources.openRawResource(resourceId)
    val tempFile = File.createTempFile("test_model", ".tflite", cacheDir)
    FileOutputStream(tempFile).use { outputStream -> inputStream.use { input -> input.copyTo(outputStream) } }
    return tempFile
}
