package com.simprints.sample

sealed interface SimFaceDestination {
    data object Main : SimFaceDestination

    data object Camera : SimFaceDestination
}

