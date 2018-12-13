package wpi.epplevinsky.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class OnDevicePerformanceActivity extends AppCompatActivity {

    // Initialize the Line Chart View
    private LineChartView lineChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_device_performance);

        drawLineChart();
    }

    // Code for Menu Starts Here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.home_item) {
            // Go to Take Picture Activity
            startActivity(new Intent(OnDevicePerformanceActivity.this, MainActivity.class));
        }
        else if (item.getItemId() == R.id.on_device_performance_item) {
            // Go to Gallery Activity
            startActivity(new Intent(OnDevicePerformanceActivity.this, OnDevicePerformanceActivity.class));
        }
        else if (item.getItemId() == R.id.off_device_performance_item) {
            // Go to Gallery Activity
            startActivity(new Intent(OnDevicePerformanceActivity.this, OffDevicePerformanceActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
    // Code for Menu Ends Here

    // This function creates the Line Chart using static data points
    public void drawLineChart() {

        lineChartView = (LineChartView) findViewById(R.id.on_device_line_chart);

        List<PointValue> values = new ArrayList<>();

        Long highestLatency = Long.MIN_VALUE;

        SharedPreferences prefs = getSharedPreferences(MainActivity.ON_SHARED_PREFS_FILE, MODE_PRIVATE);
        Map<String, ?> preferences = prefs.getAll();
        for(int i = 1; i <= preferences.size(); i++) {
            Long latency = (Long) preferences.get("inference" + i);
            values.add(new PointValue(i, latency));
            if(latency > highestLatency) {
                highestLatency = latency;
            }
        }

        Line line = new Line(values)
                .setColor(Color.BLUE)
                .setHasPoints(true)
                .setHasLabels(true);

        List<Line> lines = new ArrayList<>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        AxisValue tempAxisValue;

        // Create X-Axis data points from 0 to 10
        List<AxisValue> axisValuesForX = new ArrayList<>();
        for (int i = 0; i <= preferences.size(); i++){
            tempAxisValue = new AxisValue(i);
            tempAxisValue.setLabel(""+i);
            axisValuesForX.add(tempAxisValue);
        }

        // Create Y-Axis data points from 0 to 10
        List<AxisValue> axisValuesForY = new ArrayList<>();
        for (int i = 0; i <= highestLatency; i++){
            tempAxisValue = new AxisValue(i);
            tempAxisValue.setLabel(""+i);
            axisValuesForY.add(tempAxisValue);
        }

        // Create X & Y Axis and initialize with data points generated above
        Axis xAxis = new Axis(axisValuesForX);
        Axis yAxis = new Axis(axisValuesForY);

        // Set the Orientation and the Names of the X & Y Axises
        data.setAxisXBottom(xAxis);
        data.setAxisYLeft(yAxis);
        xAxis.setName("Inference Number");
        yAxis.setName("Inference Time (in MilliSeconds)");

        lineChartView.setLineChartData(data);
    }

}