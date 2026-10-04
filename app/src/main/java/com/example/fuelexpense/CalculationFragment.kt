package com.example.fuelexpense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.DecimalFormat

class CalculationFragment : Fragment() {

    private lateinit var tvConsumPerKm: TextView
    private lateinit var tvConsumPer100: TextView
    private lateinit var tvCostPerKm: TextView
    private lateinit var tvCostPer100: TextView
    private lateinit var tvStatTotalMileage: TextView
    private lateinit var tvStatCount: TextView
    private lateinit var tvStatTotalFuel: TextView
    private lateinit var tvStatTotalCost: TextView
    private lateinit var tvStatAvgPrice: TextView
    private lateinit var tvEmpty: TextView

    private val num = DecimalFormat("#.##")
    private val numMoney = DecimalFormat("#,##0.00")

    private val db: FuelDatabase
        get() = (activity as MainActivity).db

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_calculation, container, false)
        tvConsumPerKm = v.findViewById(R.id.tvConsumPerKm)
        tvConsumPer100 = v.findViewById(R.id.tvConsumPer100)
        tvCostPerKm = v.findViewById(R.id.tvCostPerKm)
        tvCostPer100 = v.findViewById(R.id.tvCostPer100)
        tvStatTotalMileage = v.findViewById(R.id.tvStatTotalMileage)
        tvStatCount = v.findViewById(R.id.tvStatCount)
        tvStatTotalFuel = v.findViewById(R.id.tvStatTotalFuel)
        tvStatTotalCost = v.findViewById(R.id.tvStatTotalCost)
        tvStatAvgPrice = v.findViewById(R.id.tvStatAvgPrice)
        tvEmpty = v.findViewById(R.id.tvEmpty)
        return v
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        // Сортируем по пробегу (восходяще), так как расход считаем по паре заправок.
        val records = db.getAll().sortedBy { it.mileageKm }
        if (records.size < 2) {
            showEmpty()
            return
        }

        tvEmpty.visibility = View.GONE

        val results = mutableListOf<ConsumptionResult>()
        for (i in 1 until records.size) {
            FuelCalculator.consumption(records[i - 1], records[i])?.let { results.add(it) }
        }

        if (results.isEmpty()) {
            showEmpty()
            return
        }

        val totalDistance = results.sumOf { it.distanceKm }
        val totalFuel = results.sumOf { it.fuelUsedLiters }
        val totalCost = results.sumOf { it.distanceKm * it.costPerKm }

        val litersPerKm = totalFuel / totalDistance
        val litersPer100 = litersPerKm * 100
        val costPerKm = totalCost / totalDistance
        val costPer100 = costPerKm * 100

        tvConsumPerKm.text = "${num.format(litersPerKm)} л"
        tvConsumPer100.text = "${num.format(litersPer100)} л"
        tvCostPerKm.text = "${numMoney.format(costPerKm)} ₽"
        tvCostPer100.text = "${numMoney.format(costPer100)} ₽"

        tvStatTotalMileage.text =
            requireContext().getString(R.string.stat_total_mileage) + ": ${num.format(totalDistance)} км"
        tvStatCount.text =
            requireContext().getString(R.string.stat_count) + ": ${records.size}"
        tvStatTotalFuel.text =
            requireContext().getString(R.string.stat_total_fuel) + ": ${num.format(totalFuel)} л"
        tvStatTotalCost.text =
            requireContext().getString(R.string.stat_total_cost) + ": ${numMoney.format(totalCost)} ₽"

        val avgPrice = records.map { it.pricePerLiter }.average()
        tvStatAvgPrice.text =
            requireContext().getString(R.string.stat_avg_price) + ": ${num.format(avgPrice)} ₽"
    }

    private fun showEmpty() {
        tvConsumPerKm.text = "—"
        tvConsumPer100.text = "—"
        tvCostPerKm.text = "—"
        tvCostPer100.text = "—"
        tvStatTotalMileage.text = ""
        tvStatCount.text = ""
        tvStatTotalFuel.text = ""
        tvStatTotalCost.text = ""
        tvStatAvgPrice.text = ""
        tvEmpty.visibility = View.VISIBLE
    }
}