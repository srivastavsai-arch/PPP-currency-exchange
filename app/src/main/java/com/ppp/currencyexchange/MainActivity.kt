package com.ppp.currencyexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ppp.currencyexchange.data.local.SettingsDataStore
import com.ppp.currencyexchange.ui.home.HomeScreen
import com.ppp.currencyexchange.ui.settings.SettingsScreen
import com.ppp.currencyexchange.ui.theme.PPPCurrencyExchangeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            val themeMode by settingsDataStore.themeMode.collectAsState(initial = "system")
            var showSettings by remember { mutableStateOf(false) }

            PPPCurrencyExchangeTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSettings) {
                        SettingsScreen(
                            onNavigateBack = { showSettings = false }
                        )
                    } else {
                        HomeScreen(
                            onNavigateToSettings = { showSettings = true }
                        )
                    }
                }
            }
        }
    }
}
