package uk.co.digitalbrainswitch.dbsblobodiary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import uk.co.digitalbrainswitch.dbsblobodiary.bluetooth.BluetoothChatService;
import uk.co.digitalbrainswitch.dbsblobodiary.bluetooth.DeviceListActivity;
import uk.co.digitalbrainswitch.dbsblobodiary.util.LowPassFilter;
import uk.co.digitalbrainswitch.dbsblobodiary.visual.Circle;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends Activity {

    Typeface font;

    private String TAG = "DBS BLOBO DIARY";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    //latest blobo value
    public static float pressure = 0;
    public static double maxPressure = 23500;
    public static double minPressure = 10000;
    public static double thresholdPressure = 22000;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    //variables needed for handling the user requirements
    private long startTime = 0L;
    long timeInMillies = 0L;
    long timeSwap = 0L;
    long finalTime = 0L;
    //private Handler timerHandler = new Handler();


    private float prevPressure = 0f;

    //UI Components
    TextView tvDisplay;
    TextView tvConnectionStatus;

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
    }

    private void initialise(){
        tvDisplay = (TextView) findViewById(R.id.tvMainDisplay);
        tvDisplay.setTypeface(font);
        tvConnectionStatus = (TextView) findViewById(R.id.tvMainConnectionStatus);
        tvConnectionStatus.setTypeface(font);

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
        minPressure = (double) sharedPref.getInt(getString(R.string.pressure_min),
                getResources().getInteger(R.integer.pressure_min_default_value));
        maxPressure = (double) sharedPref.getInt(getString(R.string.pressure_max),
                getResources().getInteger(R.integer.pressure_max_default_value));
        thresholdPressure = (double) sharedPref.getInt(getString(R.string.pressure_threshold),
                getResources().getInteger(R.integer.pressure_threshold_default_value));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    @Override
    public void onBackPressed() {
        //Disable user pressing the back button on the main activity. This prevents the user disconnecting a connected Bluetooth connection.
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

        switch(item.getItemId()){
            case (R.id.menu_connect):
                connectBluetoothBlobo();
                break;

            case (R.id.menu_show_time_data):
                break;

            case (R.id.menu_show_map):
                break;

            case (R.id.menu_reflection):
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

    private void showAbout(){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
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

    private void connectBluetoothBlobo(){
        //connect blobo via bluetooth
        //Connect to bluetooth and display read data on tvDisplay
        tvDisplay.setText("");
        Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
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

    private void vibrate(long time) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(time);
    }

    private void vibrate(long pattern[]) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern, -1); //-1 to disable repeat
    }

    private void saveDataToFile(String data) {

        File root = Environment.getExternalStorageDirectory();
        File file = new File(root, "Download/data.txt");

        try {
            if (root.canWrite()) {
                FileWriter filewriter = new FileWriter(file, true);
                BufferedWriter out = new BufferedWriter(filewriter);
                out.write(data + "\n");
                out.close();
            }
        } catch (IOException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        }
    }

    private void saveValueToFile(String data) {

        File root = Environment.getExternalStorageDirectory();
        File file = new File(root, "Download/values.txt");

        try {
            if (root.canWrite()) {
                FileWriter filewriter = new FileWriter(file, true);
                BufferedWriter out = new BufferedWriter(filewriter);
                out.write(data + "\n");
                out.close();
            }
        } catch (IOException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        }
    }



    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            String status = getString(R.string.title_connected_to) + " " + mConnectedDeviceName;
                            long pattern[] = {0, 300, 200, 300, 0};
                            vibrate(pattern);
                            setStatus(status);
//                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            MainActivity.pressure = 0;
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    int numA = ((int) readBuf[1] & 0xff) + ((int) readBuf[0] & 0xff) * 256;

                    if (numA > minPressure && numA < maxPressure) {

                        //setStatus(String.valueOf(numA));
                        pressure = numA;
                        prevPressure = pressure = LowPassFilter.filter(prevPressure, pressure, 0.5f);
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        final String formattedTime = formatter.format(calendar.getTime());
                        tvDisplay.setText(String.valueOf((int) pressure) + "\t" + formattedTime);

                        Thread threadOfValues = new Thread() {
                            @Override
                            public void run() {
                                //unix time
                                long unixTime = System.currentTimeMillis();

                                //write values to external file
                                String dataToWrite = String.valueOf((int) (pressure)) + "," + unixTime;
                                //Log.i("SAVING", " - " + pressure);
                                //saveValueToFile(dataToWrite);
                                //tvDisplay.append(dataToWrite);

                                try {
                                    sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        };
                        threadOfValues.start();
                    } else {
                        //Log.i("blobo out of range",String.valueOf(numA));
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                    setStatus("Connected to " + mConnectedDeviceName);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    //update the current start time
                    startTime = SystemClock.uptimeMillis();
                    //timerHandler.postDelayed(optionTimer, 1000);

                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
