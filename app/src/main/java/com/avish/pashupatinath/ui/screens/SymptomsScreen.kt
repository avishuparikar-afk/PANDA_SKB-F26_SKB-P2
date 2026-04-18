package com.avish.pashupatinath.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.avish.pashupatinath.data.AnimalRecord
import com.avish.pashupatinath.logic.HealthEngine
import com.avish.pashupatinath.ui.theme.PrimaryGreen
import com.avish.pashupatinath.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsScreen(navController: NavController, viewModel: MainViewModel) {
    val symptomList = listOf("Fever", "Weight loss", "Reduced milk", "Skin lesions", "Swelling", "Blisters")
    val selectedSymptoms = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Symptoms Input") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Select observed symptoms:", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(symptomList) { symptom ->
                    val isSelected = symptom in selectedSymptoms
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedSymptoms.remove(symptom)
                            else selectedSymptoms.add(symptom)
                        },
                        label = { Text(symptom) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val diagnosis = HealthEngine.diagnose(selectedSymptoms)
                    viewModel.addRecord(
                        AnimalRecord(
                            diseaseName = diagnosis.first?.displayName ?: "Unknown",
                            confidence = diagnosis.second,
                            symptoms = selectedSymptoms.joinToString(", "),
                            riskScore = HealthEngine.calculateRiskScore(selectedSymptoms)
                        )
                    )
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = selectedSymptoms.isNotEmpty()
            ) {
                Text("GENERATE RISK SCORE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
