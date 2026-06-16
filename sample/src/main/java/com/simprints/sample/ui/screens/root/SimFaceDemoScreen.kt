package com.simprints.sample.ui.screens.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.simprints.sample.ui.screens.camera.SimFaceCameraDemoScreen
import com.simprints.sample.ui.screens.image.SimFaceTestImageDemoScreen

@Composable
fun SimFaceDemoScreen(
    modifier: Modifier = Modifier,
    selectedTab: DemoTab,
    snackbarHostState: SnackbarHostState,
    onSelectTab: (DemoTab) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
            NavigationBarItem(
                selected = selectedTab == DemoTab.CAMERA,
                onClick = { onSelectTab(DemoTab.CAMERA) },
                label = { Text("Camera") },
                icon = { Text("1") },
            )
            NavigationBarItem(
                selected = selectedTab == DemoTab.TEST_IMAGES,
                onClick = { onSelectTab(DemoTab.TEST_IMAGES) },
                label = { Text("Test Images") },
                icon = { Text("2") },
            )
        }
        },
    ) { innerPadding ->
        when (selectedTab) {
            DemoTab.CAMERA -> {
                SimFaceCameraDemoScreen(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    snackbarHostState = snackbarHostState,
                )
            }

            DemoTab.TEST_IMAGES -> {
                SimFaceTestImageDemoScreen(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    snackbarHostState = snackbarHostState,
                )
            }
        }
    }
}
