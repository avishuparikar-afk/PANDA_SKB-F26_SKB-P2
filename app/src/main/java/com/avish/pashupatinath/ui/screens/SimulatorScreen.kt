package com.avish.pashupatinath.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.avish.pashupatinath.ui.theme.PrimaryGreen
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(navController: NavController) {
    var animalCount by remember { mutableFloatStateOf(1f) }
    var days by remember { mutableFloatStateOf(7f) }
    
    // Simple spread formula: infected * (1.2 ^ days)
    val predictedSpread = (animalCount * (1.2).pow(days.toDouble())).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spread Simulator") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Simulation Parameters", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Infected Animals: ${animalCount.toInt()}")
                    Slider(
                        value = animalCount,
                        onValueChange = { animalCount = it },
                        valueRange = 1f..20f,
                        steps = 19,
                        colors = SliderDefaults.colors(thumbColor = PrimaryGreen, activeTrackColor = PrimaryGreen)
                    )
                    
                    Text("Time Period: ${days.toInt()} Days")
                    Slider(
                        value = days,
                        onValueChange = { days = it },
                        valueRange = 1f..30f,
                        steps = 29,
                        colors = SliderDefaults.colors(thumbColor = PrimaryGreen, activeTrackColor = PrimaryGreen)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PREDICTED SPREAD", color = Color.White, fontSize = 14.sp)
                    Text(
                        "$predictedSpread",
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("Animals likely to be affected", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
            
            Text(
                "Warning: This is a simulation based on standard contagion models for LSD/FMD. Isolate infected animals immediately.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
