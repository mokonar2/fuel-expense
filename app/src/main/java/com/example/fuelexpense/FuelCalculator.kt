package com.example.fuelexpense

/**
 * Расчёт расхода топлива методом "полного бака":
 * расход между двумя заправками = топливо, залитое во второй раз,
 * делённое на пройденное расстояние между заправками.
 */
data class ConsumptionResult(
    val litersPerKm: Double,     // л/км
    val litersPer100Km: Double,  // л/100км
    val costPerKm: Double,       // руб/км
    val costPer100Km: Double,    // руб/100км
    val distanceKm: Double,      // пройденное расстояние
    val fuelUsedLiters: Double   // израсходованное топливо
)

object FuelCalculator {

    fun consumption(previous: Refueling, current: Refueling): ConsumptionResult? {
        val distance = current.mileageKm - previous.mileageKm
        if (distance <= 0) return null

        // Топливо, потраченное между заправками, равно количеству,
        // залитому на текущей заправке (по методу полного бака).
        val liters = current.fuelAmountLiters
        if (liters <= 0) return null

        val litersPerKm = liters / distance
        val litersPer100Km = litersPerKm * 100.0
        val costPerKm = current.totalCost / distance
        val costPer100Km = costPerKm * 100.0

        return ConsumptionResult(
            litersPerKm = litersPerKm,
            litersPer100Km = litersPer100Km,
            costPerKm = costPerKm,
            costPer100Km = costPer100Km,
            distanceKm = distance,
            fuelUsedLiters = liters
        )
    }
}