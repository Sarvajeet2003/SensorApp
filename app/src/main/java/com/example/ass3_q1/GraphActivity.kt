package com.example.ass3_q1

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GraphActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        val dbHelper = DatabaseHelper(this)
        val orientationDataList = dbHelper.getOrientationData()

        val rollGraph = findViewById<GraphView>(R.id.rollGraph)
        val pitchGraph = findViewById<GraphView>(R.id.pitchGraph)
        val yawGraph = findViewById<GraphView>(R.id.yawGraph)

        plotGraph(
            rollGraph,
            orientationDataList.map { DataPoint(it.timestamp.toDouble(), it.roll.toDouble()) },"Roll")
        plotGraph(
            pitchGraph,
            orientationDataList.map { DataPoint(it.timestamp.toDouble(), it.pitch.toDouble()) },"Pitch")
        plotGraph(
            yawGraph,
            orientationDataList.map { DataPoint(it.timestamp.toDouble(), it.yaw.toDouble()) },"Yaw")

        generateAndSaveCSV(this, orientationDataList, "orientation_data")
    }

private fun plotGraph(graphView: GraphView, dataPoints: List<DataPoint>, axisValue: String) {
    val series = LineGraphSeries(dataPoints.toTypedArray())

    // Customize series appearance
    series.color = Color.BLUE
    series.thickness = 3


    // Add series to graph
    graphView.addSeries(series)

    // Add grid background
    graphView.setBackgroundColor(Color.LTGRAY) // Set your desired background color here

    // Customize grid lines
    graphView.gridLabelRenderer.gridColor = Color.LTGRAY

    // Customize legend
    series.title = axisValue
    graphView.legendRenderer.isVisible = true
    graphView.legendRenderer.align = LegendRenderer.LegendAlign.TOP

    // Smooth lines
    series.isDrawBackground = true
    series.backgroundColor = Color.argb(50, 135, 206, 250) // Light Blue

    // Enable scrolling and zooming
    graphView.viewport.apply {
        isScalable = true
        isScrollable = true
        setScalableY(true)
    }

    // Set custom labels for x-axis (time)
    graphView.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
        override fun formatLabel(value: Double, isValueX: Boolean): String {
            return if (isValueX) {
                SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date(value.toLong()))
            } else {
                super.formatLabel(value, isValueX)
            }
        }
    }

    // Set axis titles
    graphView.gridLabelRenderer.apply {
        horizontalAxisTitle = "Time"
        verticalAxisTitle = axisValue
    }
}

    private fun generateAndSaveCSV(context: Context, orientationDataList: List<OrientationData>, fileName: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val currentTimeStamp = dateFormat.format(Date())
        val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "CSVFiles")
        folder.mkdirs()
        val file = File(folder, "$fileName.csv")
        val fileWriter = FileWriter(file)

        try {
            // Write header
            fileWriter.append("Time,Roll,Pitch,Yaw\n")

            // Write data
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            orientationDataList.forEach { orientationData ->
                val time = timeFormat.format(Date(orientationData.timestamp))
                val roll = orientationData.roll
                val pitch = orientationData.pitch
                val yaw = orientationData.yaw
                fileWriter.append("$time,$roll,$pitch,$yaw\n")
            }

            // Flush and close writer
            fileWriter.flush()
            fileWriter.close()

            // Notify user about file creation
            Log.d("CSV", "CSV file generated and saved at: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}