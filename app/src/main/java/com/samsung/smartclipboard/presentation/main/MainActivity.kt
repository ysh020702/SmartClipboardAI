package com.samsung.smartclipboard.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samsung.smartclipboard.presentation.handoff.HandoffLauncher
import com.samsung.smartclipboard.ui.theme.SmartClipboardTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionResults = MutableStateFlow<Map<String, Boolean>?>(null)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionResults.value = permissions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartClipboardTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                // Observe permission results
                LaunchedEffect(Unit) {
                    permissionResults.collect { permissions ->
                        if (permissions != null) {
                            val granted = MainMediaPermissionHelper.hasImageReadPermission(context)
                            viewModel.onIntent(MainIntent.MediaPermissionChanged(granted))
                            if (granted) {
                                viewModel.onIntent(MainIntent.ImportRecentScreenshots)
                            }
                        }
                    }
                }

                // Check initial permission status
                LaunchedEffect(Unit) {
                    val granted = MainMediaPermissionHelper.hasImageReadPermission(context)
                    viewModel.onIntent(MainIntent.MediaPermissionChanged(granted))
                    if (granted) {
                        viewModel.onIntent(MainIntent.ImportRecentScreenshots)
                    }
                }

                MainScreen(
                    uiState = uiState,
                    onIntent = viewModel::onIntent,
                    onRequestMediaPermission = {
                        val permissions = MainMediaPermissionHelper.requiredMediaPermissions()
                        permissionLauncher.launch(permissions)
                    },
                    onShareDraft = { title, body ->
                        val result = HandoffLauncher.launchShareText(context, title, body)
                        viewModel.onIntent(MainIntent.HandoffActionCompleted(result.message))
                    },
                    onCreateCalendarDraft = { title, description ->
                        val result = HandoffLauncher.launchCalendarInsert(context, title, description)
                        viewModel.onIntent(MainIntent.HandoffActionCompleted(result.message))
                    }
                )
            }
        }
    }
}