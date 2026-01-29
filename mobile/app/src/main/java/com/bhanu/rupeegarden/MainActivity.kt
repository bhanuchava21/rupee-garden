package com.bhanu.rupeegarden

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bhanu.rupeegarden.audio.LocalSoundManager
import com.bhanu.rupeegarden.audio.SoundManager
import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.ui.navigation.RupeeGardenNavHost
import com.bhanu.rupeegarden.ui.theme.RupeeGardenTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundManager = SoundManager(applicationContext)
        soundManager.initialize()

        enableEdgeToEdge()
        setContent {
            RupeeGardenTheme {
                val navController = rememberNavController()
                var isInitialized by remember { mutableStateOf(false) }

                // Initialize app and observe settings
                LaunchedEffect(Unit) {
                    val dataStore = RupeeGardenDataStore(applicationContext)

                    // Mark app as initialized (for tracking, not for demo data)
                    val isInitialized = dataStore.isAppInitialized.first()
                    if (!isInitialized) {
                        dataStore.setAppInitialized()
                    }

                    // Observe sound enabled setting
                    dataStore.userProgress.collect { progress ->
                        soundManager.setEnabled(progress.soundEnabled)
                    }
                }

                LaunchedEffect(Unit) {
                    isInitialized = true
                }

                if (isInitialized) {
                    CompositionLocalProvider(LocalSoundManager provides soundManager) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets.systemBars.only(
                                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                            )
                        ) { innerPadding ->
                            RupeeGardenNavHost(
                                navController = navController,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}
