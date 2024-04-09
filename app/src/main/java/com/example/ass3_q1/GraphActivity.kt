package com.example.ass3_q1

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GraphActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        val orientationDataList =
            intent.getSerializableExtra("orientation_data") as List<OrientationData>

        val rollGraph = findViewById<GraphView>(R.id.rollGraph)
        val pitchGraph = findViewById<GraphView>(R.id.pitchGraph)
        val yawGraph = findViewById<GraphView>(R.id.yawGraph)

        plotGraph(
            rollGraph,
            orientationDataList.map { DataPoint(it.timestamp.toDouble(), it.roll.toDouble()) })
        plotGraph(
            pitchGraph,
            orientationDataList.map { DataPoint(it.timestamp.toDouble(), it.pitch.toDouble()) })
        plotGraph(
            yawGraph,
            orientationDataList.map { DataPoint(it.timestamp.toDouble(), it.yaw.toDouble()) })
    }

    private fun plotGraph(graphView: GraphView, dataPoints: List<DataPoint>) {
        val filteredDataPoints = dataPoints.filterIndexed { index, _ -> index % 2 == 0 }
        val series = LineGraphSeries(filteredDataPoints.toTypedArray())
        graphView.addSeries(series)

        // Set custom labels for x-axis
        graphView.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date(value.toLong()))
                } else {
                    super.formatLabel(value, isValueX)
                }
            }
        }
    }
}