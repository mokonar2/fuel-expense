package com.example.fuelexpense

data class Refueling(
    val id: Long,
    val date: String,
    val mileageKm: Double,      // пробег на спидометре
    val fuelType: String,       // вид топлива
    val fuelAmountLiters: Double, // количество топлива, л
    val pricePerLiter: Double,  // цена за литр, руб
    val totalCost: Double       // общая стоимость, руб
)