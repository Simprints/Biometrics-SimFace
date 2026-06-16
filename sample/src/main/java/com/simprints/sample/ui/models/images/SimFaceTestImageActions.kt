package com.simprints.sample.ui.models.images

data class SimFaceTestImageActions(
    val onLoadObama1: () -> Unit,
    val onLoadObama2: () -> Unit,
    val onLoadBush: () -> Unit,
    val onLoadLowQuality: () -> Unit,
    val onCompareObamaToObama: () -> Unit,
    val onCompareObamaToBush: () -> Unit,
)
