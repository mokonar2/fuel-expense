package com.example.fuelexpense

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FuelDatabase(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "fuel.db"
        const val DB_VERSION = 1
        const val TABLE_REFUEL = "refueling"
        const val COL_ID = "id"
        const val COL_DATE = "date"
        const val COL_MILEAGE = "mileage"
        const val COL_FUEL_TYPE = "fuel_type"
        const val COL_AMOUNT = "amount"
        const val COL_PRICE = "price"
        const val COL_TOTAL = "total"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_REFUEL (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_DATE TEXT NOT NULL, " +
                "$COL_MILEAGE REAL NOT NULL, " +
                "$COL_FUEL_TYPE TEXT NOT NULL, " +
                "$COL_AMOUNT REAL NOT NULL, " +
                "$COL_PRICE REAL NOT NULL, " +
                "$COL_TOTAL REAL NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REFUEL")
        onCreate(db)
    }

    fun insert(r: Refueling): Long {
        val cv = ContentValues().apply {
            put(COL_DATE, r.date)
            put(COL_MILEAGE, r.mileageKm)
            put(COL_FUEL_TYPE, r.fuelType)
            put(COL_AMOUNT, r.fuelAmountLiters)
            put(COL_PRICE, r.pricePerLiter)
            put(COL_TOTAL, r.totalCost)
        }
        return writableDatabase.insert(TABLE_REFUEL, null, cv)
    }

    fun delete(id: Long): Int {
        return writableDatabase.delete(TABLE_REFUEL, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun getAll(): List<Refueling> {
        val list = mutableListOf<Refueling>()
        val cursor = readableDatabase.query(
            TABLE_REFUEL,
            null, null, null, null, null,
            "$COL_DATE DESC, $COL_ID DESC"
        )
        cursor.use {
            val idxId = it.getColumnIndexOrThrow(COL_ID)
            val idxDate = it.getColumnIndexOrThrow(COL_DATE)
            val idxMile = it.getColumnIndexOrThrow(COL_MILEAGE)
            val idxType = it.getColumnIndexOrThrow(COL_FUEL_TYPE)
            val idxAmount = it.getColumnIndexOrThrow(COL_AMOUNT)
            val idxPrice = it.getColumnIndexOrThrow(COL_PRICE)
            val idxTotal = it.getColumnIndexOrThrow(COL_TOTAL)
            while (it.moveToNext()) {
                list.add(
                    Refueling(
                        id = it.getLong(idxId),
                        date = it.getString(idxDate),
                        mileageKm = it.getDouble(idxMile),
                        fuelType = it.getString(idxType),
                        fuelAmountLiters = it.getDouble(idxAmount),
                        pricePerLiter = it.getDouble(idxPrice),
                        totalCost = it.getDouble(idxTotal)
                    )
                )
            }
        }
        return list
    }
}