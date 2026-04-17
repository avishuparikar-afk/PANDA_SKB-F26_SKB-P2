package com.avish.pashupatinath.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.avish.pashupatinath.data.AnimalRecord
import com.avish.pashupatinath.logic.HealthEngine
import com.avish.pashupatinath.ui.theme.PrimaryGreen
import com.avish.pashupatinath.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(navController: NavController, viewModel: MainViewModel) {
    var isScanning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Pair<String, Int>?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Animal") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for Camera/Image
                Text("Camera Preview", color = Color.DarkGray)
                if (isScanning) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (result == null) {
                Button(
                    onClick = {
                        scope.launch {
                            isScanning = true
                            delay(2000) // Simulate AI processing
                            isScanning = false
                            result = "Lumpy Skin Disease" to 78
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    enabled = !isScanning
                ) {
                    Text("ANALYZE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Text("Offline Diagnosis Mode Running...", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
            } else {
                DiagnosisResultCard(result!!.first, result!!.second) {
                    viewModel.addRecord(
                        AnimalRecord(
                            diseaseName = result!!.first,
                            confidence = result!!.second,
                            symptoms = "Skin lesions, Fever",
                            riskScore = 75
                        )
                    )
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
fun DiagnosisResultCard(disease: String, confidence: Int, onAddToHerd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("AI Diagnosis", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { confidence / 100f },
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 8.dp,
                        color = PrimaryGreen
                    )
                    Text("$confidence%", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text(disease, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
                    Text("Disease Probability", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAddToHerd,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to Herd Records")
            }
        }
    }
}
