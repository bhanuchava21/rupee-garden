package com.bhanu.rupeegarden

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.ui.navigation.RupeeGardenNavHost
import com.bhanu.rupeegarden.ui.theme.RupeeGardenTheme
import com.bhanu.rupeegarden.util.DemoDataSeeder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RupeeGardenTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                var isInitialized by remember { mutableStateOf(false) }

                // Seed demo data if needed (only on first run)
                LaunchedEffect(Unit) {
                    val dataStore = RupeeGardenDataStore(applicationContext)
                    val entries = dataStore.entries.first()

                    if (entries.isEmpty()) {
                        // Seed demo data for first-time users
                        val seeder = DemoDataSeeder(dataStore)
                        seeder.seedDemoData()
                    }

                    isInitialized = true
                }

                if (isInitialized) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
