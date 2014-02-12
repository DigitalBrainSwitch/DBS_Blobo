package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.PanListener;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;

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
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import uk.co.digitalbrainswitch.dbsblobodiary.location.TimeLocation;

public class ShowTimeDataActivity extends Activity implements View.OnClickListener, View.OnLongClickListener, PanListener, ZoomListener {

    Typeface font;
    Button bShowAllOnMap;
    String selectedFileName = "NULL";
    // chart container
    private LinearLayout layout;
    private GraphicalView mChartView = null;
    XYMultipleSeriesRenderer renderer;

    public static double panXAxisMin = 0, panXAxisMax = 0, initPanXAxisMin = 0, initPanXAxisMax = 0;

    public static double [] PAN_LIMITS;

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
        txt.append(translateFileNameToDate(selectedFileName));

        this.initialise();
    }

    //fileName format YYYY-MM-DD_<day of week> to YYYY/MM/DD <day of week>
    private String translateFileNameToDate(String fileNameString) {
        StringTokenizer st = new StringTokenizer(fileNameString, ".");
        return st.nextToken().replaceAll("-", "/").replaceAll("_", " ");
    }

    //not the most efficient way, but it works
    private Date convertFileNameToSystemDate(String fileNameString) {
        StringTokenizer st = new StringTokenizer(fileNameString, "_");
        StringTokenizer dateST = new StringTokenizer(st.nextToken(), "-");
        String yearString = dateST.nextToken();
        int year = Integer.parseInt(yearString);
        String monthString = dateST.nextToken();
        int month = Integer.parseInt(monthString);
        String dayString = dateST.nextToken();
        int day = Integer.parseInt(dayString);

        GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month - 1, day); // month - 1 because it start at 0. i.e. 0 => January
        return gregorianCalendar.getTime();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChartView != null) {
            layout.removeView(mChartView);
            mChartView = null;
        }

        data = new TreeMap<Long, TimeLocation>();
        readDataFromFile(selectedFileName);

        XYMultipleSeriesDataset dateDataset = getDateDataset();
        renderer = getRenderer(dateDataset.getSeriesCount());

        if (panXAxisMin != 0 && panXAxisMax != 0) {
            renderer.setXAxisMin(panXAxisMin);
            renderer.setXAxisMax(panXAxisMax);
        }

        mChartView = ChartFactory.getTimeChartView(this, dateDataset, renderer, null);
        mChartView.setOnClickListener(this);
        mChartView.setOnLongClickListener(this);
        mChartView.addPanListener(this);
        mChartView.addZoomListener(this, true, true);
        layout.addView(mChartView);
        if (initPanXAxisMin == 0 && initPanXAxisMax == 0) {
            initPanXAxisMin = renderer.getXAxisMin();
            initPanXAxisMax = renderer.getXAxisMax();
        }
    }

    private boolean mMeasure = false;

    private void initialise() {
        bShowAllOnMap = (Button) findViewById(R.id.bShowAllOnMap);
        bShowAllOnMap.setTypeface(font);
        bShowAllOnMap.setOnClickListener(this);
        bShowAllOnMap.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mMeasure) {
                    int scaleLength = (int) (bShowAllOnMap.getLineHeight() * 1.5); //(int) getResources().getDimension(R.dimen.textview_font_size);
                    Drawable drawable = getResources().getDrawable(R.drawable.map_200x200);
                    drawable.setBounds(0, 0, scaleLength, scaleLength);
                    ScaleDrawable sd = new ScaleDrawable(drawable, 0, 0, 0);
                    bShowAllOnMap.setCompoundDrawables(sd.getDrawable(), null, null, null);
                    mMeasure = true;
                }
            }
        });

        //set custom title bar http://stackoverflow.com/a/8748802
        this.getActionBar().setDisplayShowCustomEnabled(true);
        this.getActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.titleview, null);
        //if you need to customize anything else about the text, do it here.
        //I'm using a custom TextView with a custom font in my layout xml so all I need to do is set title
        TextView tvTitle = (TextView) v.findViewById(R.id.title);
        tvTitle.setText(this.getTitle());
        tvTitle.setTextColor(getResources().getColor(android.R.color.white));
        tvTitle.setTypeface(font);
        //assign the view to the actionbar
        this.getActionBar().setCustomView(v);

        data = new TreeMap<Long, TimeLocation>();
        readDataFromFile(selectedFileName);

        layout = (LinearLayout) findViewById(R.id.layoutShowTimeChart);

        if (data.size() == 0) {
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
        switch (v.getId()) {
            case R.id.bShowAllOnMap:
                showMap();
                return;
            default:
                break;
        }
        SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
//        double[] xy = mChartView.toRealPoint(0);
        if (seriesSelection != null) {
            vibrate(100);
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

    private XYMultipleSeriesRenderer getRenderer(int numOfSeries) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        //renderer.setBackgroundColor(Color.BLACK);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        renderer.setTextTypeface(font);
        renderer.setAxisTitleTextSize(20);
        renderer.setChartTitleTextSize(20);
//        renderer.setLabelsTextSize(25);
//        renderer.setLegendTextSize(20);
        renderer.setShowLegend(false);
        renderer.setLabelsTextSize(0.035F * ((size.x < size.y) ? size.x : size.y));

        renderer.setPointSize(0.05F * ((size.x < size.y) ? size.x : size.y));
//        renderer.setPointSize(50f);
        renderer.setYAxisMax(1.5f);
        renderer.setYAxisMin(0.5f);
        renderer.setZoomEnabled(true, false);
        renderer.setZoomButtonsVisible(true);

        renderer.setPanEnabled(true, false); //Enable panning for X axis, but not Y axis
        renderer.setClickEnabled(true); //Make chart points clickable
        renderer.setMargins(new int[]{100, 20, 150, 10}); //top, left, bottom, right

        XYSeriesRenderer r;

        final int COLORS[] = {Color.rgb(0, 191, 255), Color.rgb(30, 144, 255), Color.rgb(75, 75, 187)};
//        final int DBS_BLUE_COLOR = Color.rgb(19, 164, 210); //DBS Blue rgb(19, 164, 210)
        for (int i = 0; i < numOfSeries; i++) {
            r = new XYSeriesRenderer();
            r.setColor(COLORS[i % COLORS.length]);
            r.setPointStyle(PointStyle.CIRCLE);
            r.setStroke(BasicStroke.SOLID);
            r.setFillPoints(true);
            r.setPointStrokeWidth(0.015F * ((size.x < size.y) ? size.x : size.y));
            r.setAnnotationsColor(Color.DKGRAY);
            r.setAnnotationsTextSize(0.03F * ((size.x < size.y) ? size.x : size.y));
            r.setAnnotationsTextAlign(Paint.Align.CENTER);

            renderer.addSeriesRenderer(r);
        }

        renderer.setAxesColor(Color.DKGRAY);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setShowGridY(true);
        renderer.setGridColor(Color.LTGRAY);
        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(Color.WHITE);
        renderer.setMarginsColor(Color.WHITE);
        renderer.setYLabels(0);

        double PanXAxisMin = (double) convertFileNameToSystemDate(selectedFileName).getTime() - 21600000; //minus 6 hours
        double PanXAxisMax = PanXAxisMin + 86400000 + 21600000 + 21600000; //plus 24 + 6 hours
        PAN_LIMITS = new double[]{PanXAxisMin, PanXAxisMax, renderer.getYAxisMin(), renderer.getYAxisMax()};
        renderer.setPanLimits(PAN_LIMITS);

        return renderer;
    }

    private XYMultipleSeriesDataset getDateDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        TimeSeries series = new TimeSeries("Sensor Data");

        Set<Long> keySet = data.keySet();
        Long[] keys = keySet.toArray(new Long[keySet.size()]);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final float POINT_SIZE = 0.04F * ((size.x < size.y) ? size.x : size.y);
        double ANNOTATION_Y_OFFSET = 0.03F * ((size.x < size.y) ? size.x : size.y);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
        int COUNTER = 0;
        Date prevDate = null;

        float Y_OFFSET = 0;
        int Y_OFFSET_SERIES_COUNTER = 0;
        final int NUM_CONSECUTIVE_STEPS = 0;

        for (long key : keys) {
            Date date = new Date(key);
            if (prevDate != null) {
                if ((date.getTime() - prevDate.getTime()) < 600000) { //if event dates are within 10min of each other
                    if (Y_OFFSET_SERIES_COUNTER < NUM_CONSECUTIVE_STEPS) { //change NUM_CONSECUTIVE_STEPS to increase the height for consecutive points that are within 10min
                        Y_OFFSET += 3 / (POINT_SIZE);
                        Y_OFFSET_SERIES_COUNTER++;
                    }
                } else {
                    //reset the Y_OFFSET
                    Y_OFFSET = 0;
                    Y_OFFSET_SERIES_COUNTER = 0;
                    dataset.addSeries(series);
                    series = new TimeSeries("Sensor Data");
                }
            }
            series.add(date, 1 + Y_OFFSET);
            //add time string as annotation
            series.addAnnotation(sdf.format(date), date.getTime(), 1 + Y_OFFSET - 1 / (ANNOTATION_Y_OFFSET * 2) + 5 / POINT_SIZE * ((COUNTER % 2 == 0) ? 1 : -1));//  *
            COUNTER++;
            prevDate = date;
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
            case (R.id.menu_show_pressure_values):
                showPressure();
                break;
            default:
                break;
        }
        return true;
    }

    private void showPressure() {
        Intent intent = new Intent(ShowTimeDataActivity.this, ShowPressureActivity.class);
        intent.putExtra(getString(R.string.intent_extra_selected_file_name), selectedFileName);
        panXAxisMin = renderer.getXAxisMin();
        panXAxisMax = renderer.getXAxisMax();
        startActivity(intent);
    }

    private void showMap() {
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

    private void vibrate(long time) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(time);
    }

    @Override
    public boolean onLongClick(View v) {
        this.onClick(v);
        return false;
    }

    @Override
    public void panApplied() {
        panXAxisMin = renderer.getXAxisMin();
        panXAxisMax = renderer.getXAxisMax();
    }

    @Override
    public void zoomApplied(ZoomEvent zoomEvent) {
        panXAxisMin = renderer.getXAxisMin();
        panXAxisMax = renderer.getXAxisMax();
    }

    @Override
    public void zoomReset() {
//        renderer.setXAxisMin(initPanXAxisMin);
//        renderer.setXAxisMax(initPanXAxisMax);
        renderer.setXAxisMin(renderer.getPanLimits()[0]);
        renderer.setXAxisMax(renderer.getPanLimits()[1]);
        panXAxisMin = renderer.getXAxisMin();
        panXAxisMax = renderer.getXAxisMax();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        panXAxisMin = 0;
        panXAxisMax = 0;
        initPanXAxisMin = 0;
        initPanXAxisMax = 0;
        finish();
    }
}
