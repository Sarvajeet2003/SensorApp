package com.example.ass3_q1
import android.os.Parcel
import android.os.Parcelable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class OrientationData(
    val timestamp: Long,
    val roll: Float,
    val pitch: Float,
    val yaw: Float
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(timestamp)
        parcel.writeFloat(roll)
        parcel.writeFloat(pitch)
        parcel.writeFloat(yaw)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrientationData> {
        override fun createFromParcel(parcel: Parcel): OrientationData {
            return OrientationData(parcel)
        }

        override fun newArray(size: Int): Array<OrientationData?> {
            return arrayOfNulls(size)
        }
    }
}

