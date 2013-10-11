package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class SettingsActivity extends Activity implements View.OnClickListener {

    EditText etSettingsMinimum, etSettingsMaximum, etSettingsThreshold, etSettingsLongSqueezeDuration;
    Button bSettingsSave;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        this.initialise();
    }

    private void initialise() {
        Typeface font = ((MyApplication) getApplication()).getCustomTypeface();
        TextView textView = (TextView) findViewById(R.id.tvSettingsMinimum);
        textView.setTypeface(font);
        textView = (TextView) findViewById(R.id.tvSettingsMaximum);
        textView.setTypeface(font);
        textView = (TextView) findViewById(R.id.tvSettingsThreshold);
        textView.setTypeface(font);
        textView = (TextView) findViewById(R.id.tvSettingsLongSqueezeDuration);
        textView.setTypeface(font);

        //Retrieve stored preferences' values
        sharedPref = getDefaultSharedPreferences(getApplicationContext());
        int pressureMinimum = sharedPref.getInt(getString(R.string.pressure_min),
                getResources().getInteger(R.integer.pressure_min_default_value));
        int pressureMaximum = sharedPref.getInt(getString(R.string.pressure_max),
                getResources().getInteger(R.integer.pressure_max_default_value));
        int pressureThreshold = sharedPref.getInt(getString(R.string.pressure_threshold),
                getResources().getInteger(R.integer.pressure_threshold_default_value));
        int longSqueezeDuration = sharedPref.getInt(getString(R.string.long_squeeze_duration),
                getResources().getInteger(R.integer.long_squeeze_duration_default_value));

        //Set font and set text from preferences
        etSettingsMinimum = (EditText) findViewById(R.id.etSettingsMinimum);
        etSettingsMinimum.setText(Integer.toString(pressureMinimum));
        etSettingsMinimum.setTypeface(font);
        etSettingsMaximum = (EditText) findViewById(R.id.etSettingsMaximum);
        etSettingsMaximum.setText(Integer.toString(pressureMaximum));
        etSettingsMaximum.setTypeface(font);
        etSettingsThreshold = (EditText) findViewById(R.id.etSettingsThreshold);
        etSettingsThreshold.setText(Integer.toString(pressureThreshold));
        etSettingsThreshold.setTypeface(font);
        etSettingsLongSqueezeDuration = (EditText) findViewById(R.id.etSettingsLongSqueezeDuration);
        etSettingsLongSqueezeDuration.setText(Integer.toString(longSqueezeDuration));
        etSettingsLongSqueezeDuration.setTypeface(font);

        bSettingsSave = (Button) findViewById(R.id.bSettingsSave);
        bSettingsSave.setTypeface(font);
        bSettingsSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bSettingsSave:
                //Set to shared preferences
                SharedPreferences.Editor editor = sharedPref.edit();
                int pressure_min_value, pressure_max_value, pressure_threshold_value, long_squeeze_duration_value;

                //Check if value is integer. If not, display an alert dialog
                try {
                    pressure_min_value = Integer.parseInt(etSettingsMinimum.getText().toString());
                } catch (NumberFormatException e) {
                    showAlertMessage("Error", "Please enter an integer number for Minimum");
                    return;
                }

                try {
                    pressure_max_value = Integer.parseInt(etSettingsMaximum.getText().toString());
                } catch (NumberFormatException e) {
                    showAlertMessage("Error", "Please enter an integer number for Maximum");
                    return;
                }

                try {
                    pressure_threshold_value = Integer.parseInt(etSettingsThreshold.getText().toString());
                } catch (NumberFormatException e) {
                    showAlertMessage("Error", "Please enter an integer number for Threshold");
                    return;
                }

                try {
                    long_squeeze_duration_value = Integer.parseInt(etSettingsLongSqueezeDuration.getText().toString());
                    if(long_squeeze_duration_value < 2){
                        showAlertMessage("Error", "Long Squeeze Duration value must be larger than 1");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlertMessage("Error", "Please enter an integer number for Long Squeeze Duration");
                    return;
                }

                //Write to preferences storage
                editor.putInt(getString(R.string.pressure_min), pressure_min_value);
                editor.putInt(getString(R.string.pressure_max), pressure_max_value);
                editor.putInt(getString(R.string.pressure_threshold), pressure_threshold_value);
                editor.putInt(getString(R.string.long_squeeze_duration), long_squeeze_duration_value);
                editor.commit();

                finish();
                break;
            default:
                break;
        }
    }

    //Method for displaying a popup alert dialog
    private void showAlertMessage(String title, String Message) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
        popupBuilder.setTitle(title);
        popupBuilder.setMessage(Message);
        popupBuilder.setPositiveButton("OK", null);
        popupBuilder.show();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.settings, menu);
//        return true;
//    }

}
