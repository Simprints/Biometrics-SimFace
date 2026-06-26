package com.simprints.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.simprints.sample.ui.screens.root.DemoTab
import com.simprints.sample.ui.screens.root.SimFaceDemoScreen
import com.simprints.sample.ui.theme.SimFaceTesterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            var selectedTab by remember { mutableStateOf(DemoTab.CAMERA) }

            SimFaceTesterTheme {
                SimFaceDemoScreen(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it },
                    snackbarHostState = snackbarHostState,
                )
            }
        }
    }
}
