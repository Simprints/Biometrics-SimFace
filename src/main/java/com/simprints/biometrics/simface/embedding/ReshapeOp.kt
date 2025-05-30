package com.simprints.biometrics.simface.embedding

import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat

internal class ReshapeOp : TensorOperator {
    override fun apply(tensorBuffer: TensorBuffer): TensorBuffer {
        val (height, width, dims) = tensorBuffer.shape

        val reshapedArray = FloatArray(height * width * dims)
        val inputArray = tensorBuffer.floatArray
        for (h in 0 until height) {
            for (w in 0 until width) {
                for (d in 0 until dims) {
                    reshapedArray[d * height * width + h * width + w] =
                        inputArray[h * width * dims + w * dims + d]
                }
            }
        }

        val reshapedTensor =
            TensorBufferFloat.createFixedSize(intArrayOf(dims, height, width), DataType.FLOAT32)
        reshapedTensor.loadArray(reshapedArray)
        return reshapedTensor
    }
}
