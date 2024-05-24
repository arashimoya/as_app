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
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.viewmodelfactory.LineChartViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

    private val lineChartViewModel: LineChartViewModel by viewModels {
        LineChartViewModelFactory(AgroSenseDatabase.getDatabase(requireContext()).measurementDao())
    }

    companion object {
        fun newInstance() = LineChartFragment()
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 101
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

        observeTemperatureReadings()

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
                    val temperatureReadings = lineChartViewModel.temperatureReading.firstOrNull()
                    if (temperatureReadings != null) {
                        exportDataToPdf(requireContext(), temperatureReadings)
                    } else {
                        Toast.makeText(requireContext(), "No temperature readings available", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun observeTemperatureReadings() {
        viewLifecycleOwner.lifecycleScope.launch {
            lineChartViewModel.temperatureReading.collect { temperatureReadings ->
                // 获取第一个数据点的时间戳
                val referenceTime = temperatureReadings.firstOrNull()?.recordedAt?.toDate()?.time ?: 0L
                val entries = temperatureReadings.mapIndexed { _, reading ->
                    // 将时间戳转换为相对于第一个数据点的时间偏移量，并将单位转换为小时
                    val offsetXAxis = (reading.recordedAt.toDate().time - referenceTime) / (1000 * 60 * 60).toFloat()
                    Entry(offsetXAxis, reading.value.toFloat())
                }

                val dataSet = LineDataSet(entries, "Temperature")
                dataSet.color = ContextCompat.getColor(requireContext(), R.color.black)
                val lineData = LineData(dataSet)

                lineChart.data = lineData

                // 设置X轴标签
                val xAxis = lineChart.xAxis
                xAxis.position = XAxis.XAxisPosition.TOP
                xAxis.valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        // 返回与value相对应的标签，这里可以根据需要自定义
                        return "${value.toInt()}H" // 例如，返回每个小时的时间
                    }
                }

                val lowThreshold = LimitLine(20.34f, "Min Threshold")
                lowThreshold.lineWidth = 2f
                lowThreshold.lineColor = Color.BLUE
                lowThreshold.textColor = Color.BLUE
                lowThreshold.textSize = 12f

                val highThreshold = LimitLine(21.43f, "Max Threshold")
                highThreshold.lineWidth = 2f
                highThreshold.lineColor = Color.RED
                highThreshold.textColor = Color.RED
                highThreshold.textSize = 12f

                // 设置左侧Y轴标签
                val leftYAxis = lineChart.axisLeft
                leftYAxis.axisMinimum = 20.27f // 设置左侧 Y 轴最小值
                leftYAxis.axisMaximum = 21.45f // 设置左侧 Y 轴最大值
                leftYAxis.addLimitLine(lowThreshold)
                leftYAxis.addLimitLine(highThreshold)
                leftYAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        // 返回与value相对应的标签，这里可以根据需要自定义
                        return "%.2f℃".format(value)
                    }
                }

                // 设置右侧Y轴标签
                val rightYAxis = lineChart.axisRight
                rightYAxis.axisMinimum = 20.27f // 设置右侧 Y 轴最小值
                rightYAxis.axisMaximum = 21.45f // 设置右侧 Y 轴最大值
                rightYAxis.addLimitLine(lowThreshold)
                rightYAxis.addLimitLine(highThreshold)
                rightYAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        // 返回与value相对应的标签，这里可以根据需要自定义
                        return "%.2f℃".format(value)
                    }
                }
                leftYAxis.setDrawTopYLabelEntry(true)
                rightYAxis.setDrawTopYLabelEntry(true)
                dataSet.valueTextSize = 2f
                dataSet.setValueTextColor(Color.BLACK)
                dataSet.setDrawValues(true)
                lineChart.invalidate()
            }
        }
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
