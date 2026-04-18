package com.avish.pashupatinath.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.avish.pashupatinath.logic.RiskLevel
import com.avish.pashupatinath.ui.theme.PrimaryGreen
import com.avish.pashupatinath.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionScreen(navController: NavController, viewModel: MainViewModel) {
    val riskLevel by viewModel.currentRiskLevel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recommended Actions") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RiskStatusHeader(riskLevel)

            Text("Immediate Steps:", fontWeight = FontWeight.Bold, fontSize = 20.sp)

            when (riskLevel) {
                RiskLevel.LOW -> {
                    ActionItem(Icons.Default.Visibility, "Monitor health daily")
                    ActionItem(Icons.Default.CleaningServices, "Keep surroundings clean")
                }
                RiskLevel.MEDIUM -> {
                    ActionItem(Icons.Default.Groups, "Isolate sick animals", true)
                    ActionItem(Icons.Default.Security, "Limit access to visitors")
                    ActionItem(Icons.Default.Phone, "Consult local vet")
                }
                RiskLevel.HIGH -> {
                    ActionItem(Icons.Default.Groups, "Strict Quarantine", true)
                    ActionItem(Icons.Default.BugReport, "Disinfect entire area", true)
                    ActionItem(Icons.Default.Emergency, "Emergency Vet Call", true)
                    ActionItem(Icons.Default.Announcement, "Alert neighboring farms")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("DONE")
            }
        }
    }
}

@Composable
fun RiskStatusHeader(risk: RiskLevel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(risk.color))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${risk.label} Risk Alert", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text("Follow these steps to protect your herd.", color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
fun ActionItem(icon: ImageVector, text: String, urgent: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (urgent) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (urgent) Color.Red else PrimaryGreen,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, fontWeight = if (urgent) FontWeight.Bold else FontWeight.Normal)
    }
}
