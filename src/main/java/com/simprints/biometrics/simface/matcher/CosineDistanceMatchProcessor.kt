package com.simprints.biometrics.simface.matcher

import com.simprints.biometrics.simface.Utils
import kotlin.math.sqrt

internal class CosineDistanceMatchProcessor : MatchProcessor {
    @Throws(IllegalArgumentException::class)
    override fun verificationScore(
        probe: ByteArray,
        matchAgainst: ByteArray,
    ): Double {
        require(probe.size == matchAgainst.size) { "Arrays must be of the same size." }

        val array1 = Utils.byteArrayToFloatArray(probe)
        val array2 = Utils.byteArrayToFloatArray(matchAgainst)

        var dotProduct = 0.0
        var magnitude1 = 0.0
        var magnitude2 = 0.0

        for (i in array1.indices) {
            dotProduct += array1[i] * array2[i]
            magnitude1 += array1[i] * array1[i]
            magnitude2 += array2[i] * array2[i]
        }

        check(magnitude1 > 0.0 && magnitude2 > 0.0) { "Arrays must not be zero vectors." }
        val cosineSimilarity = dotProduct / (sqrt(magnitude1) * sqrt(magnitude2))
        val cosineDistance = 1.0 - cosineSimilarity

        return (2.0 - cosineDistance) / 2
    }

    override fun identificationScore(
        probe: ByteArray,
        matchReferences: List<ByteArray>,
    ): List<Pair<ByteArray, Double>> = matchReferences
        .map { matchAgainst -> probe to verificationScore(probe, matchAgainst) }
        .sortedByDescending { it.second }
}
