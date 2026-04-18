package com.avish.pashupatinath

import com.avish.pashupatinath.logic.Disease
import com.avish.pashupatinath.logic.HealthEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthEngineTest {

    @Test
    fun `diagnose returns correct disease with full symptoms`() {
        val symptoms = listOf("Skin lesions", "Fever", "Reduced milk")
        val result = HealthEngine.diagnose(symptoms)
        assertEquals(Disease.LUMPY_SKIN, result.first)
        assertEquals(100, result.second)
    }

    @Test
    fun `diagnose returns best match for partial symptoms`() {
        val symptoms = listOf("Blisters", "Fever")
        val result = HealthEngine.diagnose(symptoms)
        assertEquals(Disease.FOOT_AND_MOUTH, result.first)
        // FMD has 3 symptoms: Fever, Blisters, Weight loss. 2/3 = 66%
        assertEquals(66, result.second)
    }

    @Test
    fun `calculateRiskScore capped at 100`() {
        val symptoms = listOf("A", "B", "C", "D", "E")
        val score = HealthEngine.calculateRiskScore(symptoms)
        assertEquals(100, score)
    }

    @Test
    fun `calculateRiskScore correct for fewer symptoms`() {
        val symptoms = listOf("A", "B")
        val score = HealthEngine.calculateRiskScore(symptoms)
        assertEquals(50, score)
    }
}
