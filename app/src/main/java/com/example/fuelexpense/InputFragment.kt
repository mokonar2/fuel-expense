package com.example.fuelexpense

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InputFragment : Fragment() {

    private lateinit var etDate: TextInputEditText
    private lateinit var etMileage: TextInputEditText
    private lateinit var etFuelType: AutoCompleteTextView
    private lateinit var etAmount: TextInputEditText
    private lateinit var etPrice: TextInputEditText
    private lateinit var etTotal: TextInputEditText
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RecordAdapter

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val numberFormat = DecimalFormat("#.##")

    private val db: FuelDatabase
        get() = (activity as MainActivity).db

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_input, container, false)

        etDate = v.findViewById(R.id.etDate)
        etMileage = v.findViewById(R.id.etMileage)
        etFuelType = v.findViewById(R.id.etFuelType)
        etAmount = v.findViewById(R.id.etAmount)
        etPrice = v.findViewById(R.id.etPrice)
        etTotal = v.findViewById(R.id.etTotal)
        recycler = v.findViewById(R.id.recyclerRecords)
        val btnSave = v.findViewById<Button>(R.id.btnSave)
        val btnClear = v.findViewById<Button>(R.id.btnClear)

        setupDatePicker()
        setupFuelTypeDropdown()

        btnSave.setOnClickListener { saveRecord() }
        btnClear.setOnClickListener { clearForm() }

        adapter = RecordAdapter { record -> confirmDelete(record) }
        recycler.adapter = adapter

        return v
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private fun setupDatePicker() {
        etDate.setText(dateFormatter.format(Calendar.getInstance().time))
        etDate.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val chosen = Calendar.getInstance().apply {
                    set(y, m, d)
                }
                etDate.setText(dateFormatter.format(chosen.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun setupFuelTypeDropdown() {
        val types = resources.getStringArray(R.array.fuel_types)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        etFuelType.setAdapter(adapter)
        etFuelType.setText(resources.getString(R.string.fuel_ai95), false)
    }

    private fun saveRecord() {
        val mileage = etMileage.text.toString().toDoubleOrNull()
        val amount = etAmount.text.toString().toDoubleOrNull()
        val price = etPrice.text.toString().toDoubleOrNull()
        val total = etTotal.text.toString().toDoubleOrNull()

        if (mileage == null || mileage < 0 ||
            amount == null || amount <= 0 ||
            price == null || price <= 0 ||
            total == null || total <= 0
        ) {
            Toast.makeText(requireContext(), getString(R.string.error_fill), Toast.LENGTH_LONG).show()
            return
        }

        val record = Refueling(
            id = 0,
            date = etDate.text.toString(),
            mileageKm = mileage,
            fuelType = etFuelType.text.toString(),
            fuelAmountLiters = amount,
            pricePerLiter = price,
            totalCost = total
        )
        db.insert(record)
        Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
        clearForm()
        loadRecords()
    }

    private fun clearForm() {
        etMileage.setText("")
        etAmount.setText("")
        etPrice.setText("")
        etTotal.setText("")
        etDate.setText(dateFormatter.format(Calendar.getInstance().time))
    }

    private fun loadRecords() {
        val records = db.getAll()
        adapter.submit(records)
    }

    private fun confirmDelete(record: Refueling) {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.delete_confirm))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                db.delete(record.id)
                loadRecords()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}

class RecordAdapter(
    private val onDelete: (Refueling) -> Unit
) : RecyclerView.Adapter<RecordAdapter.VH>() {

    private val list = mutableListOf<Refueling>()
    private val num = DecimalFormat("#.##")

    class VH(val view: View) : RecyclerView.ViewHolder(view)

    fun submit(newList: List<Refueling>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = list.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(o: Int, n: Int) = list[o].id == newList[n].id
            override fun areContentsTheSame(o: Int, n: Int) = list[o] == newList[n]
        })
        list.clear()
        list.addAll(newList)
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return VH(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = list[position]
        holder.view.findViewById<TextView>(R.id.tvDate).text = r.date
        holder.view.findViewById<TextView>(R.id.tvFuelType).text = r.fuelType
        holder.view.findViewById<TextView>(R.id.tvMileage).text =
            "Пробег: ${num.format(r.mileageKm)} км"
        holder.view.findViewById<TextView>(R.id.tvAmountPrice).text =
            "${num.format(r.fuelAmountLiters)} л × ${num.format(r.pricePerLiter)} ₽/л"
        holder.view.findViewById<TextView>(R.id.tvTotal).text =
            "Итого: ${num.format(r.totalCost)} ₽"
        holder.view.findViewById<android.widget.ImageView>(R.id.ivDelete)
            .setOnClickListener { onDelete(r) }
    }
}