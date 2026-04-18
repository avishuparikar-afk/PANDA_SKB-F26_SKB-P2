package com.avish.pashupatinath.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.avish.pashupatinath.ui.theme.*
import com.avish.pashupatinath.viewmodel.MainViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val riskLevel by viewModel.currentRiskLevel.collectAsState()
    val immunityScore by viewModel.immunityScore.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Pashupatinath",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Text("Livestock Health Intelligence", fontSize = 14.sp, color = Color.Gray)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatusCard(immunityScore, riskLevel)
            }

            item {
                ActionButtons(navController)
            }

            item {
                AlertBanner(riskLevel)
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun StatusCard(immunity: Int, risk: RiskLevel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edge AI Active", color = SecondaryGreen, fontWeight = FontWeight.Bold)
                Text("$immunity%", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            LinearProgressIndicator(
                progress = { immunity / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(vertical = 4.dp),
                color = SecondaryGreen,
                trackColor = Color.LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Herd Immunity Score", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ActionButtons(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MenuButton(
            title = "Scan Animal",
            subtitle = "AI Disease Detection",
            icon = Icons.Default.CameraAlt,
            color = PrimaryGreen
        ) {
            navController.navigate("scan")
        }
        MenuButton(
            title = "Symptoms Input",
            subtitle = "Add Illness Details",
            icon = Icons.Default.Assignment,
            color = Color(0xFF558B2F)
        ) {
            navController.navigate("symptoms")
        }
        MenuButton(
            title = "Spread Simulator",
            subtitle = "Predict Outbreak",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF689F38)
        ) {
            navController.navigate("simulator")
        }
    }
}

@Composable
fun MenuButton(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AlertBanner(risk: RiskLevel) {
    if (risk == RiskLevel.LOW) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(risk.color))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "${risk.label} Outbreak Risk!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Action required to prevent spread",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
