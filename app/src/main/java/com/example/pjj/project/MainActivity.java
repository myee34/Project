package com.example.pjj.project;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager objSMG;
    private Sensor sensor_Accelerometer;

    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;

    private BarChart barChart;
    private int chartLine;

    //그래프 그리기 위한 ArrayList
    ArrayList<BarEntry> entries = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<String>();

    //뒤로가기 버튼 Delay
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    //가속도 센서 Delay
    private static final int SHAKE_THRESHOLD = 800;

    WalkData walkData;
    SQLiteDatabase sql;

    TextView walkCount;

    int moveCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        objSMG = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor_Accelerometer = objSMG.getDefaultSensor(TYPE_ACCELEROMETER);

        walkCount = (TextView)findViewById(R.id.walkCount);
        barChart = (BarChart)findViewById(R.id.chart);

        walkData = new WalkData(this);

        SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM-dd");
        String date = fm1.format(new Date());

        sql = walkData.getWritableDatabase();

         Cursor cursor;
        cursor = sql.rawQuery("SELECT * FROM WalkHistory ORDER BY WalkHistory._id DESC Limit 7;", null);


        while (cursor.moveToNext())
        {
            entries.add(new BarEntry(chartLine, cursor.getInt(1)));
            labels.add(cursor.getString(2));
            chartLine++;

            if(date.matches(cursor.getString(2)))
            {
                walkCount.setText("" + cursor.getString(1));
                moveCount = Integer.parseInt(cursor.getString(1));
            }
        }

        cursor.close();
        sql.close();

        BarDataSet barDataSet = new BarDataSet(entries, null);
        barDataSet.setDrawValues(false);
        barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        barDataSet.setDrawValues(!barDataSet.isDrawValuesEnabled());
        barDataSet.setValueTextSize(15);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(14);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        YAxis yLAxis = barChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);
        yLAxis.setTextSize(14);
        yLAxis.setAxisMinimum(0f);
        yLAxis.setDrawGridLines(true);

        YAxis yRAxis = barChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);


        Description description = new Description();
        description.setText("");

        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDescription(description);
        barChart.setNoDataText("걸음걸이 데이터가 없습니다.");
        barChart.setNoDataTextColor(Color.BLACK);
        barChart.animateY(2000, Easing.EasingOption.EaseInCubic);
        barChart.invalidate();
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();

            SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM-dd",Locale.KOREA);
            String date = fm1.format(new Date());
            sql = walkData.getWritableDatabase();
            sql.execSQL("INSERT INTO WalkHistory SELECT NULL,0,'"+date+"' WHERE NOT EXISTS(SELECT WalkHistory.Testdate FROM WalkHistory WHERE WalkHistory.Testdate='"+date+"');");

            sql.execSQL("UPDATE WalkHistory SET walk_cnt="+Integer.parseInt(""+walkCount.getText())+" WHERE WalkHistory.Testdate='"+date+"';");

            sql.close();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        objSMG.registerListener(this, sensor_Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        objSMG.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == TYPE_ACCELEROMETER)
        {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            if (gabOfTime > 100) {
                lastTime = currentTime;

                speed = Math.abs(event.values[0] + event.values[1] + event.values[2] - lastX - lastY - lastZ) / gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    moveCount++;
                    walkCount.setText(""+moveCount);
                }

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
