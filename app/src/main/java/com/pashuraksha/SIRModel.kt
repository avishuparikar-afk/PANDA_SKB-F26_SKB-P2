package com.pashuraksha

import kotlin.math.exp
import kotlin.math.min

data class SIRState(
    var susceptible: Int,
    var infected: Int,
    var recovered: Int,
    var totalPopulation: Int
)

class SIRModel(
    private val population: Int,
    private var initialInfected: Int,
    private val beta: Double, // Infection rate
    private val gamma: Double // Recovery rate
) {

    private var currentState: SIRState
    private var day: Int = 0

    init {
        val initialSusceptible = population - initialInfected
        currentState = SIRState(initialSusceptible, initialInfected, 0, population)
    }

    fun step() {
        if (currentState.infected == 0 && day > 0) {
            // No more infected individuals, simulation has ended
            return
        }

        val dS = -beta * currentState.susceptible * currentState.infected / population
        val dI = beta * currentState.susceptible * currentState.infected / population - gamma * currentState.infected
        val dR = gamma * currentState.infected

        currentState.susceptible = (currentState.susceptible + dS).toInt().coerceAtLeast(0)
        currentState.infected = (currentState.infected + dI).toInt().coerceAtLeast(0)
        currentState.recovered = (currentState.recovered + dR).toInt().coerceAtLeast(0)

        // Ensure total population remains constant (due to rounding, there might be slight deviations)
        val currentTotal = currentState.susceptible + currentState.infected + currentState.recovered
        if (currentTotal != population) {
            val difference = population - currentTotal
            // Distribute difference, e.g., add to susceptible
            currentState.susceptible += difference
        }

        day++
    }

    fun getCurrentState(): SIRState = currentState
    fun getCurrentDay(): Int = day

    fun reset() {
        day = 0
        val initialSusceptible = population - initialInfected
        currentState = SIRState(initialSusceptible, initialInfected, 0, population)
    }

    // Simulate spread radius based on SIR model parameters and time
    fun getSpreadRadius(currentDay: Int, maxRadiusKm: Double): Double {
        // A simplified function to show spread, could be more complex
        // The spread should increase over time, but not linearly
        val progress = min(1.0, currentDay / 30.0) // Max spread after ~30 days
        return maxRadiusKm * (1 - exp(-progress * 3.0)) // Exponential growth towards max radius
    }
}
