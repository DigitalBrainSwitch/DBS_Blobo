package uk.co.digitalbrainswitch.dbsblobodiary;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
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
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

public class ShowTimeDataActivity extends Activity implements View.OnClickListener {

    Typeface font;

    // chart container
    private LinearLayout layout;
    private GraphicalView mChartView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_time_data);

        font = ((MyApplication) getApplication()).getCustomTypeface();
        this.initialise();
    }

    private void initialise() {
        TextView txt = (TextView) findViewById(R.id.tvShowTimeDate);
        txt.setTypeface(font);
        layout = (LinearLayout) findViewById(R.id.layoutShowTimeChart);
        mChartView = ChartFactory.getTimeChartView(this, getDateDataset(), getRenderer(), null);
        mChartView.setOnClickListener(this);
        layout.addView(mChartView);
    }

    @Override
    public void onClick(View v) {
        SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
        double[] xy = mChartView.toRealPoint(0);
        if (seriesSelection != null) {
            //When user touched a point on the graph

            Intent intent = new Intent(this, MapActivity.class);
            //Mock up location: Lancaster 54.047812, -2.801075
            intent.putExtra(getString(R.string.showtimedata_point_location_lat), 54.047812);
            intent.putExtra(getString(R.string.showtimedata_point_location_long), -2.801075);
            startActivity(intent);

            Toast.makeText(
                    ShowTimeDataActivity.this, "Clicked point value X=" + getDate((long) xy[0], "yyyy-MM-dd HH:mm:ss.SSS"), Toast.LENGTH_SHORT).show();
        }
    }

    private static String getDate(long milliSeconds, String dateFormat)
    {
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

        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(25);
        renderer.setLegendTextSize(20);
        renderer.setPointSize(20f);
        renderer.setYAxisMax(1.5f);
        renderer.setYAxisMin(0.5f);
        renderer.setZoomEnabled(true, false);
        renderer.setZoomButtonsVisible(true);
        renderer.setPanEnabled(true, false); //Enable panning for X axis, but not Y axis
        renderer.setClickEnabled(true); //Make chart points clickable
        renderer.setMargins(new int[]{300, 20, 200, 10}); //top, left, bottom, right

        XYSeriesRenderer r = new XYSeriesRenderer();

        final int DBS_BLUE_COLOR = Color.rgb(19, 164, 210); //DBS Blue rgb(19, 164, 210)
        r.setColor(DBS_BLUE_COLOR);
        r.setPointStyle(PointStyle.CIRCLE);
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

        //Read data from file. Format: <int_pressure_value>,<long_time>
        try {
            File root = Environment.getExternalStorageDirectory();
            File file = new File(root, "Download/test_data.txt");
            FileInputStream inputStream = new FileInputStream(file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString;

                //Read every line from file. Discard pressure values that are lower than the threshold.
                while ((receiveString = bufferedReader.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(receiveString, ",");
                    String pressureString = st.nextToken();
                    int pressureVal = Integer.parseInt(pressureString);

                    //threshold cut off value. This value differs and depends on the Blobo. It requires an interface for setting the values.
                    //read threshold value from shared preference
                    if (pressureVal < 22000) {
                        continue;
                    } else {
                        pressureVal = 1;
                    }
                    String timeString = st.nextToken();
                    long time = Long.parseLong(timeString);

                    series.add(new Date(time), pressureVal);
                }
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        dataset.addSeries(series);

        return dataset;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.show_time_data, menu);
//        return true;
//    }
    
}
