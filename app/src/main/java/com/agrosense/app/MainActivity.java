package com.agrosense.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.agrosense.app.linechart.Linechart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LineChart lineChart;

    private List<String> xValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChart = findViewById(R.id.chart);

        Description description = new Description();
        description.setText("Agrosense temperature");
        description.setPosition(150f, 15f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);

        xValues = Arrays.asList("2H", "4H", "6H", "8H");

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(4);
        xAxis.setGranularity(1f);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 10f));
        entries.add(new Entry(1, 10f));
        entries.add(new Entry(2, 15f));
        entries.add(new Entry(3, 45f));

        List<Entry> entries2 = new ArrayList<>();
        entries.add(new Entry(0, 5f));
        entries.add(new Entry(1, 15f));
        entries.add(new Entry(2, 25f));
        entries.add(new Entry(3, 30f));

        LineDataSet dataSet = new LineDataSet(entries, "sensor1");
        dataSet.setColor(Color.RED);

        LineDataSet dataSet2 = new LineDataSet(entries2, "sensor2");
        dataSet2.setColor(Color.BLUE);


        LineData lineData = new LineData(dataSet, dataSet2);

        lineChart.setData(lineData);

        lineChart.invalidate();


    }

}