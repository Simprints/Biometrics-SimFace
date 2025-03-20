package com.simprints.simface.matcher

import com.simprints.simface.core.Utils

internal class CosineDistanceMatchProcessor() : MatchProcessor {

    @Throws(IllegalArgumentException::class)
    override fun verificationScore(probe: ByteArray, matchAgainst: ByteArray): Float {
        val floatProbe = Utils.byteArrayToFloatArray(probe)
        val floatMatchAgainst = Utils.byteArrayToFloatArray(matchAgainst)

        require(floatProbe.size == floatMatchAgainst.size) { "Arrays must be of the same length" }

        var dotProduct = 0.0
        var magnitude1 = 0.0
        var magnitude2 = 0.0

        for (i in floatProbe.indices) {
            dotProduct += floatProbe[i] * floatMatchAgainst[i]
            magnitude1 += floatProbe[i] * floatProbe[i]
            magnitude2 += floatMatchAgainst[i] * floatMatchAgainst[i]
        }

        magnitude1 = kotlin.math.sqrt(magnitude1)
        magnitude2 = kotlin.math.sqrt(magnitude2)

        // Prevent division by zero
        if (magnitude1 == 0.0 || magnitude2 == 0.0) return 0.0f

        // Return between 0-1
        return 1 - (1 - dotProduct / (magnitude1 * magnitude2)).toFloat() / 2
    }

    override fun identificationScore(
        probe: ByteArray,
        matchReferences: List<ByteArray>
    ): Map<ByteArray, Float> {
        return matchReferences.associateWith { matchAgainst ->
            verificationScore(probe, matchAgainst)
        }.toList()
            .sortedByDescending { (_, distance) -> distance }
            .toMap()
    }
}
