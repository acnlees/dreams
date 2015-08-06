package com.puzzle.dreams;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adam on 06/08/2015.
 */
public class sleepResult extends Activity {

    private LineChart mChart;
    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;
    HashMap<Long, Double> noiseLevels = new HashMap<>();
    HashMap<Long, Float> movementLevels = new HashMap<>();
    String alarmTime;
    String startTime;
    String tone;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_result);


        noiseLevels = (HashMap) getIntent().getSerializableExtra("noise");
        movementLevels = (HashMap) getIntent().getSerializableExtra("movement");
        startTime = getIntent().getStringExtra("start");
        alarmTime = getIntent().getStringExtra("alarm");
        tone = getIntent().getStringExtra("tone");

        int resID = getApplicationContext().getResources().getIdentifier(tone, "raw", getApplicationContext().getPackageName());

        mediaPlayer = MediaPlayer.create(getApplicationContext(), resID);

        mediaPlayer.start();

        Log.d("noiseSize and movement", noiseLevels.size() + "  " + String.valueOf(movementLevels.size()));

        Map.Entry<Long, Double> maxEntry = null;

        for (Map.Entry<Long, Double> entry : noiseLevels.entrySet()) {
            if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                maxEntry = entry;
            }
        }
        GraphView graph = (GraphView) findViewById(R.id.graph);

        DataPoint[] barData = new DataPoint[movementLevels.size()];
        DataPoint[] lineData = new DataPoint[noiseLevels.size()];




        int i = 0;
        for (Map.Entry<Long, Float> entry : movementLevels.entrySet()) {

            long minute = (entry.getKey() / (1000 * 60)) % 60;
            long hour = (entry.getKey() / (1000 * 60 * 60)) % 24;

            String sleepLeft = String.format("%02d.%02d", hour, minute);
            double number = Double.parseDouble(sleepLeft);

            DecimalFormat format = new DecimalFormat("00.00");
            String formatted = format.format(number);



            barData[i] = new DataPoint(Double.valueOf(formatted), entry.getValue()*50);

            i +=1;

        }

        int j = 0;
        for (Map.Entry<Long, Double> entry : noiseLevels.entrySet()) {
            long minute = (entry.getKey() / (1000 * 60)) % 60;
            long hour = (entry.getKey() / (1000 * 60 * 60)) % 24;
            String sleepLeft = String.format("%02d.%02d", hour, minute);

            lineData[j] = new DataPoint(Double.valueOf(sleepLeft), entry.getValue());

            j +=1;

        }
        PointsGraphSeries<DataPoint> series3 = new PointsGraphSeries<DataPoint>(lineData);
        graph.addSeries(series3);

        series3.setShape(PointsGraphSeries.Shape.TRIANGLE);
        series3.setColor(Color.YELLOW);


        BarGraphSeries<DataPoint> series2 = new BarGraphSeries<DataPoint>(barData);

        graph.addSeries(series2);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(lineData);

        //graph.addSeries(series);

        // style
        series.setColor(Color.rgb(255, 120, 120));
        series2.setSpacing(50);

        // legend
        series.setTitle("noise");
        series2.setTitle("movement");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.MIDDLE);

    }






}