package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import uk.co.digitalbrainswitch.dbsblobodiary.bluetooth.BluetoothChatService;
import uk.co.digitalbrainswitch.dbsblobodiary.bluetooth.DeviceListActivity;
import uk.co.digitalbrainswitch.dbsblobodiary.location.TimeLocation;
import uk.co.digitalbrainswitch.dbsblobodiary.util.LowPassFilter;
import uk.co.digitalbrainswitch.dbsblobodiary.util.SimpleMovingAveragesSmoothing;
import uk.co.digitalbrainswitch.dbsblobodiary.visual.Circle;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends Activity implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    Typeface font;

    private String TAG = "DBS BLOBO DIARY";
    private static final boolean D = true;

    static final int uniqueID = 782347823; // a random ID

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_DEVICE_ADDRESS = 6;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    //default blobo values and threshold
    public static float pressure = 0;
    public static double maxPressure = 24000;
    public static double minPressure = 15000;
    public static double thresholdPressure = 22000;
    public static int longSqueezeDuration = 3; //3 seconds

    public static boolean calibrationTest = false;

    public static boolean eventDetected = false;

    public static double calibrationMark = -1;
    public static int calibrationDifference = 200; //default 200. update from sensitivity shared preference.

    public static Date previousDate;

    //Moving average smooth filter
    private SimpleMovingAveragesSmoothing SMAFilter;
    private static int SMAWindowSize;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    private String mConnectedDeviceAddress = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    //variables needed for handling the user requirements
    private Handler timerHandler = new Handler();

    private float prevPressure = (float) minPressure;
    private int prevNumA = (int) minPressure;

    private int eventCounter = 0;

    //UI Components
    TextView tvDisplay;
    TextView tvConnectionStatus;
    TextView tvCalibrationTestDisplay, tvCalibrationTestDisplay2;

    //location variables
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    boolean mUpdatesRequested = false;

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    public static final int FAST_CEILING_IN_SECONDS = 1;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;
    public Location currentLocation = null;

    NotificationManager nm;

    private static final long TAP_MAX_DELAY = 400L; //max delay in ms
    private TapCounter tapCounter = new TapCounter(TAP_MAX_DELAY, TAP_MAX_DELAY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        font = ((MyApplication) getApplication()).getCustomTypeface();

        this.initialise();

        //Add a circle to layout
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.graph);
        linearLayout.addView(new Circle(getApplicationContext()));

        findViewById(R.id.ivMainDBSLogo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dbs_url))));
            }
        });

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(uniqueID);

        SMAWindowSize = getDefaultSharedPreferences(getApplicationContext()).getInt(getString(R.string.SMA_window_size),
                getResources().getInteger(R.integer.SMA_window_size_default_value));
        SMAFilter = new SimpleMovingAveragesSmoothing(SMAWindowSize); //increasing windows size will cause delay
    }

    private int touch_counter = 0;

    private void initialise() {

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

        tvDisplay = (TextView) findViewById(R.id.tvMainDisplay);
        tvDisplay.setTypeface(font);
        tvConnectionStatus = (TextView) findViewById(R.id.tvMainConnectionStatus);
        tvConnectionStatus.setTypeface(font);
        tvCalibrationTestDisplay = (TextView) findViewById(R.id.tvCalibrationTestDisplay);
        tvCalibrationTestDisplay.setTypeface(font);
        tvCalibrationTestDisplay2 = (TextView) findViewById(R.id.tvCalibrationTestDisplay2);
        tvCalibrationTestDisplay2.setTypeface(font);

        tvCalibrationTestDisplay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tapCounter.resetCounter();
                        touch_counter++;
//                        Log.e(TAG, "TOUCHED: " + touch_counter);
                        if (touch_counter == 5) {
                            vibrate(100);
                            Toast.makeText(getApplicationContext(), touch_counter + "Taps" + ": Hidden Settings.", Toast.LENGTH_SHORT).show();
                            showHiddenSettings();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        tvCalibrationTestDisplay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                eventCounter = 0;
                tvCalibrationTestDisplay.setText(((calibrationTest) ? "Cal_test Mode ON: " + eventCounter + "\t" : ""));
                vibrate(200);
                return false;
            }
        });


        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        //update interval
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        //Create a new location client
        mLocationClient = new LocationClient(this, this, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
        if (!mLocationClient.isConnected())
            mLocationClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }

        //Update threshold values from shared preferences
        SharedPreferences sharedPref = getDefaultSharedPreferences(getApplicationContext());
        SMAWindowSize = sharedPref.getInt(getString(R.string.SMA_window_size),
                getResources().getInteger(R.integer.SMA_window_size_default_value));
        SMAFilter.setWindowSize(SMAWindowSize);
        minPressure = (double) sharedPref.getInt(getString(R.string.pressure_min),
                getResources().getInteger(R.integer.pressure_min_default_value));
        maxPressure = (double) sharedPref.getInt(getString(R.string.pressure_max),
                getResources().getInteger(R.integer.pressure_max_default_value));
        if (calibrationMark != -1)
            calibrationMark = thresholdPressure = (double) sharedPref.getInt(getString(R.string.pressure_threshold),
                    getResources().getInteger(R.integer.pressure_threshold_default_value));
        longSqueezeDuration = sharedPref.getInt(getString(R.string.long_squeeze_duration),
                getResources().getInteger(R.integer.long_squeeze_duration_default_value));
        calibrationDifference = sharedPref.getInt(getString(R.string.sensitivity),
                getResources().getInteger(R.integer.sensitivity_default_value));
        calibrationTest = sharedPref.getBoolean(getString(R.string.calibration_test),
                getResources().getBoolean(R.bool.calibration_test_default_value));

        tvCalibrationTestDisplay.setEnabled(calibrationTest);
        tvCalibrationTestDisplay.setText(((calibrationTest) ? "Cal_test Mode ON: " + eventCounter + "\t" : ""));
        tvCalibrationTestDisplay.setBackgroundColor((calibrationTest) ? getResources().getColor(R.color.yellow_8) : getResources().getColor(android.R.color.transparent));
        tvCalibrationTestDisplay2.setEnabled(calibrationTest);
        tvCalibrationTestDisplay2.setText("");
        tvCalibrationTestDisplay2.setBackgroundColor((calibrationTest) ? getResources().getColor(R.color.yellow_8) : getResources().getColor(android.R.color.transparent));

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    protected void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        //mLocationClient.disconnect();

        super.onStop();
        if (D) Log.e(TAG, "-- ON STOP --");
    }


    @Override
    protected void onDestroy() {
        nm.cancel(uniqueID);
        //showNotification(getString(R.string.app_name) + " App Deactivated", "Click Here to start " + getString(R.string.app_name) + " App");
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if (D) Log.e(TAG, "--- ON DESTROY ---");
    }

    @Override
    public void onBackPressed() {
        //Disable the function for a user pressing the back button on the main activity.
        //This prevents the app from disconnecting a connected Bluetooth connection.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    /*
    * Handle menu actions
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case (R.id.menu_connect):
                connectBluetoothBlobo();
                break;

            case (R.id.menu_show_events):
                showEvents();
                break;

            case (R.id.menu_reflection):
                showReflection();
                break;

            case (R.id.menu_settings):
                showSettings();
                break;

            case (R.id.menu_about):
                showAbout();
                break;

            default:
                return false;
        }
        return true;
    }

    private void connectBluetoothBlobo() {
        //connect blobo via bluetooth
        //Connect to bluetooth and display read data on tvDisplay
        //tvDisplay.setText("");
        Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
    }

    private void showEvents() {
        Intent intent = new Intent(this, ShowEventsActivity.class);
        startActivity(intent);
    }

    private void showReflection() {
        Intent intent = new Intent(this, ReflectionActivity.class);
        startActivity(intent);
    }

    private void showAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showHiddenSettings() {
        Intent intent = new Intent(this, HiddenSettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;

            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    //Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        //mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    private final void setStatus(int resId) {
        tvConnectionStatus.setText(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        tvConnectionStatus.setText(subTitle);
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    private static int pressureLogValueCounter = 0;

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            String status = getString(R.string.title_connected_to) + " " + mConnectedDeviceName + " (" + mConnectedDeviceAddress + ")";
                            vibrate(300L);
                            setStatus(status);
                            nm.cancel(uniqueID);
//                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            tvDisplay.setText(R.string.no_pressure_value);
                            MainActivity.pressure = 0;
                            showNotification("Blobo Disconnected", "Blobo is not connected to DBS Diary");
                            SMAFilter.resetRecentData();
                            calibrationMark = -1; //reset calibration mark when blobo is disconnected
                            pressureLogValueCounter = 0;
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    break;
                case MESSAGE_READ:
                    //read from blobo device
                    byte[] readBuf = (byte[]) msg.obj;
                    int numA = ((int) readBuf[1] & 0xff) + ((int) readBuf[0] & 0xff) * 256;

                    if (calibrationTest && (numA < minPressure || numA > maxPressure))
                        tvCalibrationTestDisplay2.setText(Integer.toString(numA));

                    if (numA >= minPressure && numA < maxPressure) { //to remove noisy data
                        //apply low pass filter on numA
                        prevNumA = numA = (int) LowPassFilter.filter((float) numA, (float) prevNumA, 0.5f);
//                    } else if (numA < minPressure) {
//                        break;
//                        numA = (int) minPressure;
//                        prevNumA = numA = (int) LowPassFilter.filter((float) numA, (float) prevNumA, 0.5f);
                    } else {
                        //throw away values that are not within the range of min and max pressure
                        break;
                    }
                    //apply simple moving average filter
                    pressure = numA;
                    pressure = SMAFilter.addMostRecentValue(pressure);
//                    if (Math.abs(prevPressure - pressure) > calibrationDifference) {
//                        Log.e(TAG, "" + (int) (prevPressure - pressure));
//                    }
                    prevPressure = pressure;
                    //prevPressure = pressure = LowPassFilter.filter(prevPressure, pressure, 0.5f);
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar calendar = Calendar.getInstance();
                    long systemTimeMillis = System.currentTimeMillis();
                    calendar.setTimeInMillis(systemTimeMillis);
                    final String formattedTime = formatter.format(calendar.getTime());
                    tvDisplay.setText(String.valueOf((int) pressure) + "\t" + formattedTime
                            + " | (" + (int) calibrationMark + ":" + calibrationDifference + ")"
//                                + "\t" + ((calibrationTest)?"Cal_test Mode":"")
                    );
                    if (!calibrationTest && calibrationMark != -1) {
                        pressureLogValueCounter++;
                        if (pressureLogValueCounter == 5) { //log every 5th value (not enough space to log every pressure value)
                            savePressureValueToFile(systemTimeMillis, (int) pressure, (int) calibrationMark, calibrationDifference); //store blobo pressure values
                            pressureLogValueCounter = 0;
                        }
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    mConnectedDeviceAddress = msg.getData().getString(DEVICE_ADDRESS);
//                    setStatus("Connected to " + mConnectedDeviceName);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName + " (" + mConnectedDeviceAddress + ")", Toast.LENGTH_SHORT).show();

                    //update the current start time
//                    startTime = SystemClock.uptimeMillis();
                    timerHandler.postDelayed(optionTimer, 1000);

                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    private void showNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        //set intent filter to be the same as android's launcher, so clicking on the notification resumes the paused state of the app
        //http://stackoverflow.com/questions/5502427/resume-application-and-stack-from-notification
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder n = new NotificationCompat.Builder(this);
        n.setContentIntent(pi);
        n.setSmallIcon(R.drawable.dbs_icon);
        n.setContentTitle(title);
        n.setContentText(body);
        n.setDefaults(Notification.DEFAULT_ALL);

        nm.notify(uniqueID, n.build());
    }

    //    private int minutes, seconds;
//    private int currentSecond = 0;
//    private int lastSecond = 0;
    public static int longSqueezeCounter = 0;
//    public static int hardSqueezeCounter = 0;
//    private int vibrationTime = 150;
//    private double lastPressureValue = 0;

    final Runnable optionTimer = new Runnable() {
        @Override
        public void run() {
            //calculate the difference between times
//            timeInMillies = SystemClock.uptimeMillis() - startTime;

            //update the minutes and seconds of using the blobo
//            seconds = (int) (timeInMillies / 1000);
//            minutes = seconds / 60;
//            seconds = seconds % 60;
//            currentSecond++;

            //Initialize the calibrationMark value
            if (calibrationMark == -1) {
                calibrationMark = maxPressure;
                thresholdPressure = calibrationMark;
                previousDate = new Date();
                previousDate.setTime(System.currentTimeMillis());
                updateSharedPreference(); //also update the calibration value to the stored shared preferences
            }


            //prolonged squeeze of at least <longSqueezeDuration> seconds application logic
            //if (pressure > thresholdPressure)
            if ((int) pressure - (int) calibrationMark > calibrationDifference) {
                longSqueezeCounter++;
                if (longSqueezeCounter == (longSqueezeDuration * 10)) { //longSqueezeDuration times 10 because timerHandler.postDelayed(this, 100) has been reduced from 1000 to 100
                    longSqueezeCounter = 0;

                    //Change the colour of the circle blue and outer ring to pink for a brief moment when an event is detected
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            eventDetected = true;
                            try {
                                sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            eventDetected = false;
                        }
                    };
                    t.start();

                    if (!calibrationTest) {
                        //########################################
                        performAction();
                        //########################################
                    } else {
                        vibrate(300);
                        audioAlert();
                        tvCalibrationTestDisplay.setText("Cal_test Mode ON: " + ++eventCounter + "\t");
                    }
                    //recalibrate after 1 sec of a squeeze event
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(1000);
                                recalibrate();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                }
            } else {
                longSqueezeCounter = 0;

                //Update calibrationMark value after 1.5 sec
                if (previousDate != null) {
                    Date newDate = new Date();
                    newDate.setTime(System.currentTimeMillis());

                    long diff = newDate.getTime() - previousDate.getTime();
                    //long diffSeconds = diff / 1000 % 60;
                    //long diffMinutes = diff / (60 * 1000) % 60;
                    //long diffHours = diff / (60 * 60 * 1000);
                    //int diffInDays = (int) diff / (1000 * 60 * 60 * 24);
                    //if(diffMinutes > 4) //calibration auto update every 5 minutes

                    //auto-recalibration
                    if (diff > 1500) //60000ms = 1min. 180000ms = 3min. 300000 ms = 5min.
                    {
                        recalibrate();
                        previousDate = newDate;
                    }
                }//end if(previousDate!=nil)
            }

            //start the timer
            timerHandler.postDelayed(this, 100);
        }
    };

    private void recalibrate() {
//        double oldCalibrationMark = calibrationMark;
        calibrationMark = pressure;
//        if ((calibrationMark - oldCalibrationMark) > calibrationDifference) {
//            calibrationMark = (oldCalibrationMark + calibrationMark) / 2;
//        }
//        Log.e(TAG, "New CalibrationMark: " + (int) calibrationMark);
        thresholdPressure = calibrationMark;
        updateSharedPreference(); //also update the calibration value to the stored shared preferences
    }

    private void updateSharedPreference() {
        SharedPreferences sharedPref = getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.pressure_threshold), (int) thresholdPressure);
        editor.commit();
    }

    private void audioAlert() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
        }

    }

    private void vibrate(long time) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(time);
    }

    private void vibrate(long pattern[]) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern, -1); //-1 to disable repeat
    }


    private void savePressureValueToFile(long currentTimeInMillies, int pressure, int calibrationValue, int calibrationDiff) {
        File root = Environment.getExternalStorageDirectory();
        //create directory if it does not exist
        File folder = new File(root, getString(R.string.stored_diary_values_directory));
        if (!folder.exists()) {
            boolean success = folder.mkdirs();
            if (!success) {
                //showAlertMessage("Error", "Unable to create " + folder.getAbsolutePath());
            }
        }

        String todayDateString = sdf.format(new Date());
        //create file if it does not exist
        File file = new File(folder, todayDateString + ".txt");
        try {
            if (!file.exists()) {
                boolean success = file.createNewFile();
                if (success) {
                    //System.out.println("SUCCESS");
                } else {
                    //showAlertMessage("Error", "Unable to create " + file.getAbsolutePath());
                    //System.out.println("FAILED");
                }
            }
        } catch (IOException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        }

        try {
            if (root.canWrite()) {
                FileWriter filewriter = new FileWriter(file, true);
                BufferedWriter out = new BufferedWriter(filewriter);
                //Structure: <Time In Millisec>;<Blobo pressure>,<Threshold pressure>
                out.write(currentTimeInMillies + ";" + pressure + ";" + calibrationValue + ";" + calibrationDiff + "\n");
                out.close();
            }
        } catch (IOException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_EEEE"); //e.g. 2013-10-14_Monday

    private void saveTimeAndLocationToFile(long currentTimeInMillies, String data, String pressure, String threshold) {

        File root = Environment.getExternalStorageDirectory();

        //create directory if it does not exist
        File folder = new File(root, getString(R.string.stored_data_directory)); //new File(root + "/Download/data/");
        if (!folder.exists()) {
            boolean success = folder.mkdirs();
            if (!success) {
                showAlertMessage("Error", "Unable to create " + folder.getAbsolutePath());
            }
        }
        else {

            showAlertMessage("Success", "Folder exists");
        }

        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_EEEE"); //e.g. 2013-10-14_Monday
        
        String todayDateString = sdf.format(new Date());
        //create file if it does not exist
        File file = new File(folder, todayDateString + ".txt");
        try {
            if (!file.exists()) {
                boolean success = file.createNewFile();
                if (success) {
                    //System.out.println("SUCCESS");
                } else {
                    showAlertMessage("Error", "Unable to create " + file.getAbsolutePath());
                    //System.out.println("FAILED");
                }
            }
        } catch (IOException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        }

        //discard the second event if it's within 1 min of the previous event
        if (validEvent(currentTimeInMillies, file, 60000L)) { //60000 millisec = 1 min
            try {
                if (root.canWrite()) {
                    FileWriter filewriter = new FileWriter(file, true);
                    BufferedWriter out = new BufferedWriter(filewriter);
                    Date currentDateTime = new Date(currentTimeInMillies);
                    SimpleDateFormat sdfReadableDate = new SimpleDateFormat("yyyy/MM/dd");
                    SimpleDateFormat sdfReadableTime = new SimpleDateFormat("HH:mm:ss"); //e.g. 2013-10-14_Monday
                    String readableDate = sdfReadableDate.format(currentDateTime); //convert to millisec time to readable date format
                    String readableTime = sdfReadableTime.format(currentDateTime); //convert to millisec time to readable time format
                    //Structure: <Time In Millisec>;<Location Lat.>,<Location Long.>;<Blobo pressure>,<Threshold pressure>;<Date>;<Time>
                    out.write(currentTimeInMillies + ";" + data + ";" + pressure + ":" + threshold + ";" + readableDate + ";" + readableTime + "\n");
                    out.close();
                }
            } catch (IOException e) {
                Log.e("TAG", "Could not write file " + e.getMessage());
            }
        }
    }

    //Assumption: A valid event should be at least <timeThreshold> milliseconds after the last event
    //Check if event time is within <timeThreshold> milliseconds of any event saved in the file
    private boolean validEvent(long currentTimeInMillies, File entryFile, long timeThreshold) {
        try {
            FileInputStream inputStream = new FileInputStream(entryFile);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString;

                //Read every line from file. return false if a previous is within <timeThreshold> milliseconds of current event
                while ((receiveString = bufferedReader.readLine()) != null) {
                    long timeInMillisecond = -1;
                    try {
                        StringTokenizer st = new StringTokenizer(receiveString, ";");
                        String timeString = st.nextToken();
                        timeInMillisecond = Long.parseLong(timeString);
                        if (Math.abs(currentTimeInMillies - timeInMillisecond) < timeThreshold) {
                            return false;
                        }
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

        return true;
    }

    private void performAction() {
        startPeriodicUpdates();

        Thread thread = new Thread() {
            @Override
            public void run() {
                long pattern[] = {0, 300, 200, 300, 200, 300, 0};
                vibrate(pattern);
                audioAlert();

                //Toast.makeText(getApplicationContext(), getString(R.string.long_squeeze) + " triggered!", Toast.LENGTH_SHORT).show();
                try {
                    //wait for location update
                    while (currentLocation == null) {
                        sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                saveTimeAndLocationToFile(System.currentTimeMillis(), currentLocation.getLatitude() + "," + currentLocation.getLongitude(), ((int) pressure) + "", ((int) thresholdPressure) + "");

                //stop update after a location is received
                stopPeriodicUpdates();
                //reset current location
                currentLocation = null;
            }
        };
        thread.start();
    }

    //Method for displaying a popup alert dialog
    private void showAlertMessage(String title, String Message) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
        //popupBuilder.setTitle(title);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTypeface(font);
        tvTitle.setTextColor(getResources().getColor(R.color.dbs_blue));
        tvTitle.setPadding(30, 20, 30, 20);
        tvTitle.setTextSize(25);
        popupBuilder.setCustomTitle(tvTitle);

        popupBuilder.setMessage(Message);
        popupBuilder.setPositiveButton("OK", null);
        AlertDialog ad = popupBuilder.show();
        Button b = (Button) ad.findViewById(android.R.id.button1);
        b.setTypeface(font);
        b.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));

        TextView tv = (TextView) ad.findViewById(android.R.id.message);
        tv.setTypeface(font);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected");

        if (mUpdatesRequested) {
            startPeriodicUpdates();
        }
    }

    private void startPeriodicUpdates() {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Disconnected!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            Log.d(TAG, "Error");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location Changed");
        currentLocation = location;
    }

    private class TapCounter extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public TapCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            touch_counter = 0;
        }

        public void resetCounter() {
            start();
        }
    }
}
