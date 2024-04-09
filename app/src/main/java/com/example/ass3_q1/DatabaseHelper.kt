package com.example.ass3_q1

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_ORIENTATION)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORIENTATION")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "orientation_database"

        const val TABLE_ORIENTATION = "orientation_data"
        private const val KEY_ID = "_id"
        const val KEY_ROLL = "roll"
        const val KEY_PITCH = "pitch"
        const val KEY_YAW = "yaw"
        const val KEY_TIMESTAMP = "timestamp"  // Adding timestamp key


        private const val CREATE_TABLE_ORIENTATION = ("CREATE TABLE $TABLE_ORIENTATION("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_TIMESTAMP INTEGER,"
                + "$KEY_ROLL REAL,"
                + "$KEY_PITCH REAL,"
                + "$KEY_YAW REAL)")

    }
    // In DatabaseHelper.kt
    @SuppressLint("Range")
    fun getOrientationData(): List<OrientationData> {
        val orientationDataList = mutableListOf<OrientationData>()
        val db = this.readableDatabase

        val projection = arrayOf(KEY_TIMESTAMP, KEY_ROLL, KEY_PITCH, KEY_YAW)
        val cursor = db.query(
            TABLE_ORIENTATION,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        cursor.use {
            while (cursor.moveToNext()) {
                val timestamp = cursor.getLong(cursor.getColumnIndex(KEY_TIMESTAMP))
                val roll = cursor.getFloat(cursor.getColumnIndex(KEY_ROLL))
                val pitch = cursor.getFloat(cursor.getColumnIndex(KEY_PITCH))
                val yaw = cursor.getFloat(cursor.getColumnIndex(KEY_YAW))
                orientationDataList.add(OrientationData(timestamp, roll, pitch, yaw))
            }
        }

        db.close()
        return orientationDataList
    }

}
