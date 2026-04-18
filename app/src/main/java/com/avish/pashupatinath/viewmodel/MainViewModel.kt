package com.avish.pashupatinath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.avish.pashupatinath.data.AnimalDao
import com.avish.pashupatinath.data.AnimalRecord
import com.avish.pashupatinath.data.AppDatabase
import com.avish.pashupatinath.logic.HealthEngine
import com.avish.pashupatinath.logic.RiskLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: AnimalDao = AppDatabase.getDatabase(application).animalDao()

    val allRecords: StateFlow<List<AnimalRecord>> = dao.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentRiskLevel = MutableStateFlow(RiskLevel.LOW)
    val currentRiskLevel: StateFlow<RiskLevel> = _currentRiskLevel

    private val _immunityScore = MutableStateFlow(85)
    val immunityScore: StateFlow<Int> = _immunityScore

    init {
        viewModelScope.launch {
            allRecords.collect { records ->
                _currentRiskLevel.value = HealthEngine.getOutbreakRisk(records)
                // Simulated immunity score logic
                _immunityScore.value = (100 - (records.size * 5)).coerceIn(0, 100)
            }
        }
    }

    fun addRecord(record: AnimalRecord) {
        viewModelScope.launch {
            dao.insertRecord(record)
        }
    }

    fun clearData() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }
}
