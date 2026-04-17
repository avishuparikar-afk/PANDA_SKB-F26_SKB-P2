package com.avish.pashupatinath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.avish.pashupatinath.ui.screens.*
import com.avish.pashupatinath.ui.theme.PashupatinathTheme
import com.avish.pashupatinath.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PashupatinathTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { HomeScreen(navController, viewModel) }
                        composable("scan") { ScanScreen(navController, viewModel) }
                        composable("symptoms") { SymptomsScreen(navController, viewModel) }
                        composable("simulator") { SimulatorScreen(navController) }
                        composable("actions") { ActionScreen(navController, viewModel) }
                    }
                }
            }
        }
    }
}
