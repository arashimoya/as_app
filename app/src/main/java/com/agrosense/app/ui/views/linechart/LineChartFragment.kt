package com.agrosense.app.ui.views.linechart

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.agrosense.app.R
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.ui.views.measurementlist.MeasurementListFragment.Companion.measurementKey
import com.agrosense.app.viewmodelfactory.LineChartViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val READINGS_PER_PAGE = 100

class LineChartFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var exportButton: Button
    private val decimalFormat = DecimalFormat("0.00") // 初始化 decimalFormat
    private lateinit var measurement: Measurement

    private val lineChartViewModel: LineChartViewModel by viewModels {
        LineChartViewModelFactory(AgroSenseDatabase.getDatabase(requireContext()).measurementDao())
    }

    companion object {
        fun newInstance() = LineChartFragment()
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_linechart_fragment1, container, false)
        lineChart = rootView.findViewById(R.id.lineChart)
        exportButton = rootView.findViewById(R.id.exportButton)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val measurementId = arguments?.getLong(measurementKey) ?: throw IllegalArgumentException("No ID Provided")
            measurement = lineChartViewModel.getMeasurement(measurementId)

            observeTemperatureReadings()
            initExportButton()
        }

    }

    private fun initExportButton() {
        exportButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE
                )
            } else {
                lifecycleScope.launch {
                    val temperatureReadings =
                        lineChartViewModel.getTemperatureReadings(measurement.measurementId!!)
                            .firstOrNull()
                    if (temperatureReadings != null) {
                        exportDataToPdf(requireContext(), temperatureReadings)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No temperature readings available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun observeTemperatureReadings() {
        viewLifecycleOwner.lifecycleScope.launch {
            lineChartViewModel.getTemperatureReadings(measurement.measurementId!!).collect { temperatureReadings ->
                if(temperatureReadings.isNotEmpty()){
                    updateChart(temperatureReadings)
                }
            }
        }
    }

    private fun updateChart(temperatureReadings: List<TemperatureReading>){
        val earliestTime = temperatureReadings.first().recordedAt.millis
        val latestTime = temperatureReadings.last().recordedAt.millis
        val entries = temperatureReadings.map {
            val timeOffsetHours = (it.recordedAt.millis - earliestTime) / (1000 * 60 * 60).toFloat()
            Entry(timeOffsetHours, it.value.toFloat())
        }

        val dataSet = LineDataSet(entries, measurement.name)
        styleDataSet(dataSet)

        val lineData = LineData(dataSet)
        configureChart(lineData, temperatureReadings)
        configureXAxis(earliestTime, latestTime )
    }

    private fun styleDataSet(dataSet: LineDataSet){
        val primary = ContextCompat.getColor(requireContext(), R.color.primary)
        dataSet.color = primary
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.accent)
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(primary)
        dataSet.fillColor = primary
        dataSet.setDrawCircleHole(false)
    }

    private fun configureChart(lineData: LineData, readings: List<TemperatureReading>){
        configureYAxis(readings, lineData)
        configureZoom()

        addThresholdLines()
        lineChart.invalidate()
    }

    private fun configureZoom() {
        lineChart.setTouchEnabled(true)
        lineChart.setScaleEnabled(true)
        lineChart.isScaleYEnabled = true
        lineChart.isScaleXEnabled = true
        lineChart.isDoubleTapToZoomEnabled = true

        lineChart.setScaleMinima(0.25f, 0.25f)
        lineChart.zoom(1f, 0.1f, 0f, 0f)

    }

    private fun configureYAxis(
        readings: List<TemperatureReading>,
        lineData: LineData
    ) {
        val yMin = readings.minBy { it.value }.value.toFloat()
        val yMax = readings.maxBy { it.value }.value.toFloat()

        lineChart.data = lineData
        lineChart.axisLeft.axisMinimum = yMin - 1
        lineChart.axisLeft.axisMaximum = yMax + 1
    }

    private fun addThresholdLines(){
        lineChart.axisRight.isEnabled = false
        measurement.minValue?.let { minValue ->
            val lowerLimit = LimitLine(minValue.toFloat(), "Min Temp")
            lowerLimit.lineColor = Color.BLUE
            lowerLimit.lineWidth = 2f
            lowerLimit.textColor = Color.BLUE
            lowerLimit.textSize = 12f
            lineChart.axisLeft.addLimitLine(lowerLimit)
        }

        measurement.maxValue?.let { maxValue ->
            val upperLimit = LimitLine(maxValue.toFloat(), "Max Temp")
            upperLimit.lineColor = Color.RED
            upperLimit.lineWidth = 2f
            upperLimit.textColor = Color.RED
            upperLimit.textSize = 12f
            lineChart.axisLeft.addLimitLine(upperLimit)
        }
    }

    private fun configureXAxis( earliestTime: Long, latestTime: Long){
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.TOP
        xAxis.setLabelCount(5, true)
        xAxis.valueFormatter = object: ValueFormatter(){
            private val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

            override fun getFormattedValue(value: Float): String {
                val dateValue = earliestTime + (value* 3600 * 1000).toLong()
                return dateFormatter.format(Date(dateValue))
            }
        }

        xAxis.axisMinimum = 0f  // This is correct, keep it as 0
        val totalHours = ((latestTime - earliestTime) / (3600 * 1000.0)).toFloat()
        xAxis.axisMaximum = totalHours
    }

    private fun exportDataToPdf(context: Context, temperatureReadings: List<TemperatureReading>) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "temperature_readings_$timeStamp.pdf"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            val pdfDocument = PdfDocument()
            var totalReadings = 0

            // 分页绘制数据
            while (totalReadings < temperatureReadings.size) {
                val pageInfo = PdfDocument.PageInfo.Builder(600, 800, pdfDocument.pages.size + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // 添加标题
                val titlePaint = Paint().apply {
                    textSize = 24f
                }
                canvas.drawText("Temperature Readings", 80f, 50f, titlePaint)

                // 添加表格标题
                val tableTitlePaint = Paint().apply {
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText("Time", 50f, 100f, tableTitlePaint)
                canvas.drawText("Temperature", 300f, 100f, tableTitlePaint)

                // 添加温度和时间数据到表格中
                val readingsToDraw = temperatureReadings.subList(totalReadings, minOf(totalReadings + READINGS_PER_PAGE, temperatureReadings.size))
                totalReadings += drawTemperatureData(canvas, readingsToDraw)

                pdfDocument.finishPage(page)
            }

            // 保存 PDF 文件
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Toast.makeText(context, "PDF saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawTemperatureData(canvas: Canvas, temperatureReadings: List<TemperatureReading>): Int {
        var yPosition = 120f
        val paint = Paint().apply {
            textSize = 16f
        }

        var drawnReadingsCount = 0

        for (reading in temperatureReadings) {
            val temperature = decimalFormat.format(reading.value) // 格式化温度数据
            canvas.drawText(reading.recordedAt.toString(), 50f, yPosition, paint)
            canvas.drawText("$temperature℃", 300f, yPosition, paint)
            yPosition += 30f

            drawnReadingsCount++

            // 检查是否超出页面的绘制范围
            if (yPosition > 750f) {
                // 如果超出页面范围，返回已经绘制的条目数
                break
            }
        }

        return drawnReadingsCount
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Do nothing here, as exporting will be triggered on button click after permission is granted.
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
