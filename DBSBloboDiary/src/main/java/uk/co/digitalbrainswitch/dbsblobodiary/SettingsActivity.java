package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class SettingsActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    Typeface font;

    EditText etSettingsSensitivity, etSettingsThreshold, etSettingsLongSqueezeDuration;
    Button bSettingsSave, bSettingsCancel;
    LinearLayout llSettingsCalTestTopLine, llSettingsCalTestBottomLine;
    Switch sCalibrationTest;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        font = ((MyApplication) getApplication()).getCustomTypeface();
        this.initialise();
    }

    private void initialise() {
//        Typeface font = ((MyApplication) getApplication()).getCustomTypeface();
        TextView textView;
//        textView = (TextView) findViewById(R.id.tvSettingsMinimum);
//        textView.setTypeface(font);
//        textView = (TextView) findViewById(R.id.tvSettingsMaximum);
//        textView.setTypeface(font);
        textView = (TextView) findViewById(R.id.tvSettingsSensitivity);
        textView.setTypeface(font);
        textView = (TextView) findViewById(R.id.tvSettingsThreshold);
        textView.setTypeface(font);
        textView = (TextView) findViewById(R.id.tvSettingsLongSqueezeDuration);
        textView.setTypeface(font);

        //Retrieve stored preferences' values
        sharedPref = getDefaultSharedPreferences(getApplicationContext());
//        int pressureMinimum = sharedPref.getInt(getString(R.string.pressure_min),
//                getResources().getInteger(R.integer.pressure_min_default_value));
//        int pressureMaximum = sharedPref.getInt(getString(R.string.pressure_max),
//                getResources().getInteger(R.integer.pressure_max_default_value));
        int sensitivity = sharedPref.getInt(getString(R.string.sensitivity),
                getResources().getInteger(R.integer.sensitivity_default_value));
        int pressureThreshold = sharedPref.getInt(getString(R.string.pressure_threshold),
                getResources().getInteger(R.integer.pressure_threshold_default_value));
        int longSqueezeDuration = sharedPref.getInt(getString(R.string.long_squeeze_duration),
                getResources().getInteger(R.integer.long_squeeze_duration_default_value));
        boolean calibrationTest = sharedPref.getBoolean(getString(R.string.calibration_test),
                getResources().getBoolean(R.bool.calibration_test_default_value));

        //Set font and set text from preferences
//        etSettingsMinimum = (EditText) findViewById(R.id.etSettingsMinimum);
//        etSettingsMinimum.setText(Integer.toString(pressureMinimum));
//        etSettingsMinimum.setTypeface(font);
//        etSettingsMaximum = (EditText) findViewById(R.id.etSettingsMaximum);
//        etSettingsMaximum.setText(Integer.toString(pressureMaximum));
//        etSettingsMaximum.setTypeface(font);
        etSettingsThreshold = (EditText) findViewById(R.id.etSettingsThreshold);
        etSettingsThreshold.setText(Integer.toString(pressureThreshold));
        etSettingsThreshold.setTypeface(font);
        etSettingsLongSqueezeDuration = (EditText) findViewById(R.id.etSettingsLongSqueezeDuration);
        etSettingsLongSqueezeDuration.setText(Integer.toString(longSqueezeDuration));
        etSettingsLongSqueezeDuration.setTypeface(font);
        etSettingsSensitivity = (EditText) findViewById(R.id.etSettingsSensitivity);
        etSettingsSensitivity.setText(Integer.toString(sensitivity));
        etSettingsSensitivity.setTypeface(font);

        bSettingsCancel = (Button) findViewById(R.id.bSettingsCancel);
        bSettingsCancel.setTypeface(font);
        bSettingsCancel.setOnClickListener(this);

        bSettingsSave = (Button) findViewById(R.id.bSettingsSave);
        bSettingsSave.setTypeface(font);
        bSettingsSave.setOnClickListener(this);

        sCalibrationTest = (Switch) findViewById(R.id.sCalibrationTest);
        sCalibrationTest.setTypeface(font);
        sCalibrationTest.setChecked(calibrationTest);
//        sCalibrationTest.setOnClickListener(this);
        sCalibrationTest.setOnCheckedChangeListener(this);

        llSettingsCalTestTopLine = (LinearLayout) findViewById(R.id.llSettingsCalTestTopLine);
        llSettingsCalTestBottomLine = (LinearLayout) findViewById(R.id.llSettingsCalTestBottomLine);

        if (sCalibrationTest.isChecked()) {
            sCalibrationTest.setTextColor(getResources().getColor(R.color.yellow_8));
            sCalibrationTest.setThumbResource(R.color.yellow_8);
            llSettingsCalTestTopLine.setBackgroundColor(getResources().getColor(R.color.yellow_8));
            llSettingsCalTestBottomLine.setBackgroundColor(getResources().getColor(R.color.yellow_8));
        } else {
            sCalibrationTest.setTextColor(getResources().getColor(R.color.gray));
            sCalibrationTest.setThumbResource(R.color.gray);
            llSettingsCalTestTopLine.setBackgroundColor(getResources().getColor(R.color.gray));
            llSettingsCalTestBottomLine.setBackgroundColor(getResources().getColor(R.color.gray));
        }

        //Check http://xjaphx.wordpress.com/2011/09/20/colorizing-the-title-bar/ for changing title bar
        TextView titleBar = (TextView) getWindow().findViewById(android.R.id.title);
        titleBar.setTypeface(font);

    }

    @Override
    public void onClick(View v) {
        //Set to shared preferences
        SharedPreferences.Editor editor = sharedPref.edit();
        int sensitivity_value, pressure_threshold_value, long_squeeze_duration_value;

        switch (v.getId()) {
            case R.id.bSettingsCancel:
                finish();
                break;
            case R.id.bSettingsSave:
                //Check if value is integer. If not, display an alert dialog
//                try {
//                    pressure_min_value = Integer.parseInt(etSettingsMinimum.getText().toString());
//                } catch (NumberFormatException e) {
//                    showAlertMessage(getString(R.string.settings_error_title), "Please enter an integer number for Minimum");
//                    return;
//                }
//
//                try {
//                    pressure_max_value = Integer.parseInt(etSettingsMaximum.getText().toString());
//                } catch (NumberFormatException e) {
//                    showAlertMessage(getString(R.string.settings_error_title), "Please enter an integer number for Maximum");
//                    return;
//                }

                try {
                    sensitivity_value = Integer.parseInt(etSettingsSensitivity.getText().toString());
                } catch (NumberFormatException e) {
                    showAlertMessage(getString(R.string.settings_error_title), "Please enter an integer number for Sensitivity");
                    return;
                }

                try {
                    pressure_threshold_value = Integer.parseInt(etSettingsThreshold.getText().toString());
                } catch (NumberFormatException e) {
                    showAlertMessage(getString(R.string.settings_error_title), "Please enter an integer number for Threshold");
                    return;
                }

                try {
                    long_squeeze_duration_value = Integer.parseInt(etSettingsLongSqueezeDuration.getText().toString());
                    if (long_squeeze_duration_value < 1) {
                        showAlertMessage(getString(R.string.settings_error_title), "Long Squeeze Duration value must be larger than 0");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlertMessage(getString(R.string.settings_error_title), "Please enter an integer number for Long Squeeze Duration");
                    return;
                }

                if (sensitivity_value < 1) {
                    showAlertMessage(getString(R.string.settings_error_title), "Sensitivity must be bigger than 0");
                    return;
                }

//                if (pressure_min_value > pressure_max_value) {
//                    showAlertMessage(getString(R.string.settings_error_title), "Minimum must be smaller than Maximum");
//                    return;
//                }
//
//                if (pressure_min_value > pressure_threshold_value) {
//                    showAlertMessage(getString(R.string.settings_error_title), "Threshold must be bigger than Minimum");
//                    return;
//                }
//
//                if (pressure_threshold_value > pressure_max_value) {
//                    showAlertMessage(getString(R.string.settings_error_title), "Threshold must be smaller than Maximum");
//                    return;
//                }

                //Write to preferences storage
//                editor.putInt(getString(R.string.pressure_min), pressure_min_value);
//                editor.putInt(getString(R.string.pressure_max), pressure_max_value);
                editor.putInt(getString(R.string.sensitivity), sensitivity_value);
                editor.putInt(getString(R.string.pressure_threshold), pressure_threshold_value);
                editor.putInt(getString(R.string.long_squeeze_duration), long_squeeze_duration_value);
                editor.commit();
                finish();
                break;
            default:
                break;
        }
    }

    private void showAlertMessageWithConfirmation(String title, String Message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(Message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.calibration_test), sCalibrationTest.isChecked());
                editor.commit();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                //reset checked status back to false
                sCalibrationTest.setChecked(false);
                if (sCalibrationTest.isChecked()) {
                    sCalibrationTest.setTextColor(getResources().getColor(R.color.yellow_8));
                    sCalibrationTest.setThumbResource(R.color.yellow_8);
                } else {
                    sCalibrationTest.setTextColor(getResources().getColor(R.color.gray));
                    sCalibrationTest.setThumbResource(R.color.gray);
                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.calibration_test), sCalibrationTest.isChecked());
                editor.commit();
            }
        });
//        dialog.setIcon(R.drawable.ic_dialog_map);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        AlertDialog ad = dialog.show();
        TextView tv = (TextView) ad.findViewById(android.R.id.message);
        tv.setTypeface(font);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
        Button b = (Button) ad.findViewById(android.R.id.button1);
        b.setTypeface(font);
        b.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
        b.setTextColor(getResources().getColor(R.color.dbs_blue));
        b = (Button) ad.findViewById(android.R.id.button2);
        b.setTypeface(font);
        b.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
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
        //popupBuilder.show();
        AlertDialog ad = popupBuilder.show();
        TextView tvMsg = (TextView) ad.findViewById(android.R.id.message);
        tvMsg.setTypeface(font);
        tvMsg.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
        Button b = (Button) ad.findViewById(android.R.id.button1);
        b.setTypeface(font);
        b.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sCalibrationTest:
                if (sCalibrationTest.isChecked()) {
                    showAlertMessageWithConfirmation("Warning!", "No data will be recorded when Calibration Test is ON.");
                    sCalibrationTest.setTextColor(getResources().getColor(R.color.yellow_8));
                    sCalibrationTest.setThumbResource(R.color.yellow_8);
                    llSettingsCalTestTopLine.setBackgroundColor(getResources().getColor(R.color.yellow_8));
                    llSettingsCalTestBottomLine.setBackgroundColor(getResources().getColor(R.color.yellow_8));
                } else {
                    sCalibrationTest.setTextColor(getResources().getColor(R.color.gray));
                    sCalibrationTest.setThumbResource(R.color.gray);
                    llSettingsCalTestTopLine.setBackgroundColor(getResources().getColor(R.color.gray));
                    llSettingsCalTestBottomLine.setBackgroundColor(getResources().getColor(R.color.gray));
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(getString(R.string.calibration_test), sCalibrationTest.isChecked());
                    editor.commit();
                }
                break;
            default:
                break;
        }
    }
}
