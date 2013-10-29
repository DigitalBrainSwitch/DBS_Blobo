package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import uk.co.digitalbrainswitch.dbsblobodiary.location.TimeLocation;

public class ShowTimeDataActivity extends Activity implements View.OnClickListener {

    Typeface font;
    String selectedFileName = "NULL";
    // chart container
    private LinearLayout layout;
    private GraphicalView mChartView = null;

    TreeMap<Long, TimeLocation> data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_time_data);

        font = ((MyApplication) getApplication()).getCustomTypeface();

        //get selected file name from intent extra bundle
        Bundle extra = getIntent().getExtras();
        selectedFileName = extra.getString(getString(R.string.intent_extra_selected_file_name));

        //set up UI
        TextView txt = (TextView) findViewById(R.id.tvShowTimeDate);
        txt.setTypeface(font);
        txt.append(selectedFileName);

        this.initialise();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initialise() {
        data = new TreeMap<Long, TimeLocation>();
        readDataFromFile(selectedFileName);

        layout = (LinearLayout) findViewById(R.id.layoutShowTimeChart);
        if(mChartView != null)
            layout.removeView(mChartView);
        mChartView = ChartFactory.getTimeChartView(this, getDateDataset(), getRenderer(), null);
        mChartView.setOnClickListener(this);
        layout.addView(mChartView);

        if(data.size() == 0){
            showAlertMessage("Error", "Error reading data from file: " + selectedFileName);
            return;
        }
    }

    private void readDataFromFile(String fileName) {
        File root = Environment.getExternalStorageDirectory();
        File storedDirectory = new File(root, getString(R.string.stored_data_directory));
        File file = new File(storedDirectory, fileName);
        try {
            FileInputStream inputStream = new FileInputStream(file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString;

                //Read every line from file. Discard pressure values that are lower than the threshold.
                while ((receiveString = bufferedReader.readLine()) != null) {
                    long timeInMillisecond = -1;
                    double latitude = -1;
                    double longitude = -1;
                    try {
                        StringTokenizer st = new StringTokenizer(receiveString, ";");
                        String timeString = st.nextToken();
                        String locationString = st.nextToken();
                        StringTokenizer stLocation = new StringTokenizer(locationString, ",");
                        String latitudeString = stLocation.nextToken();
                        String longitudeString = stLocation.nextToken();
                        timeInMillisecond = Long.parseLong(timeString);
                        latitude = Double.parseDouble(latitudeString);
                        longitude = Double.parseDouble(longitudeString);

                        TimeLocation timeLocation = new TimeLocation(timeInMillisecond, latitude, longitude);
                        data.put(timeInMillisecond, timeLocation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
    }

    @Override
    public void onClick(View v) {
        SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
        double[] xy = mChartView.toRealPoint(0);
        if (seriesSelection != null) {
            //When user touched a point on the graph
            Intent intent = new Intent(this, MapActivity.class);
            long key = (long) seriesSelection.getXValue();
            TimeLocation selectedTimeLocation = data.get(key);
            intent.putExtra(getString(R.string.intent_extra_time_location), selectedTimeLocation);
            intent.putExtra(getString(R.string.intent_extra_number_of_map_points), getString(R.string.single_map_point));
            startActivity(intent);
//            Toast.makeText(
//                    ShowTimeDataActivity.this, "Clicked point value X=" + getDate((long) xy[0], "yyyy-MM-dd HH:mm:ss.SSS"), Toast.LENGTH_SHORT).show();
        }
    }

    private static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        DateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private XYMultipleSeriesRenderer getRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        //renderer.setBackgroundColor(Color.BLACK);

        renderer.setAxisTitleTextSize(20);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(25);
        //renderer.setLegendTextSize(20);
        renderer.setShowLegend(false);
        renderer.setPointSize(25f);
        renderer.setYAxisMax(1.5f);
        renderer.setYAxisMin(0.5f);
        renderer.setZoomEnabled(true, false);
        renderer.setZoomButtonsVisible(true);

        renderer.setPanEnabled(true, false); //Enable panning for X axis, but not Y axis
        renderer.setClickEnabled(true); //Make chart points clickable
        renderer.setMargins(new int[]{100, 20, 150, 10}); //top, left, bottom, right

        XYSeriesRenderer r = new XYSeriesRenderer();

        final int DBS_BLUE_COLOR = Color.rgb(19, 164, 210); //DBS Blue rgb(19, 164, 210)
        r.setColor(DBS_BLUE_COLOR);
        r.setPointStyle(PointStyle.SQUARE);
        r.setFillPoints(true);

        renderer.addSeriesRenderer(r);
        renderer.setAxesColor(Color.DKGRAY);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setShowGridY(true);
        renderer.setGridColor(Color.LTGRAY);
        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(Color.WHITE);
        renderer.setMarginsColor(Color.WHITE);
        renderer.setYLabels(0);
        return renderer;
    }

    private XYMultipleSeriesDataset getDateDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        TimeSeries series = new TimeSeries("Sensor Data");

        Set<Long> keySet = data.keySet();
        Long [] keys = keySet.toArray(new Long [keySet.size()]);
        for (long key : keys){
            series.add(new Date(key), 1);
        }
        dataset.addSeries(series);

        return dataset;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_time_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case (R.id.menu_show_map_from_time):
                showMap();
                break;
            default:
                break;
        }
        return true;
    }

    private void showMap(){
        Intent intent = new Intent(ShowTimeDataActivity.this, MapActivity.class);
        intent.putExtra(getString(R.string.intent_extra_selected_file_name), selectedFileName);
        intent.putExtra(getString(R.string.intent_extra_number_of_map_points), getString(R.string.multiple_map_points));
        startActivity(intent);
    }

    //Method for displaying a popup alert dialog
    private void showAlertMessage(String title, String Message) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
        popupBuilder.setTitle(title);
        popupBuilder.setMessage(Message);
        popupBuilder.setPositiveButton("OK", null);
        popupBuilder.show();
    }
}
