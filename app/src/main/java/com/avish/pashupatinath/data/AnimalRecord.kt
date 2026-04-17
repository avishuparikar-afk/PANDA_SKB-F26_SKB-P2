package com.avish.pashupatinath.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animal_records")
data class AnimalRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val diseaseName: String?,
    val confidence: Int,
    val symptoms: String, // Comma separated symptoms
    val riskScore: Int
)
