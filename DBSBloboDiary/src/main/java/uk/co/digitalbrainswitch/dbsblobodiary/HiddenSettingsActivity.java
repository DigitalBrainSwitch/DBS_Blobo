package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class HiddenSettingsActivity extends Activity implements View.OnClickListener {

    Typeface font;

    EditText etHiddenSettingsMinimum, etHiddenSettingsMaximum, etHiddenSettingsSMAWindowSize;
    Button bHiddenSettingsSave, bHiddenSettingsReset;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hidden_settings);
        font = ((MyApplication) getApplication()).getCustomTypeface();
        this.initialise();
    }

    private void initialise() {
        TextView tv = (TextView) findViewById(R.id.tvHiddenSettingsSMAWindowSize);
        tv.setTypeface(font);
        tv = (TextView) findViewById(R.id.tvHiddenSettingsMinimum);
        tv.setTypeface(font);
        tv = (TextView) findViewById(R.id.tvHiddenSettingsMaximum);
        tv.setTypeface(font);

        sharedPref = getDefaultSharedPreferences(getApplicationContext());
        int SMAWindowSize = sharedPref.getInt(getString(R.string.SMA_window_size),
                getResources().getInteger(R.integer.SMA_window_size_default_value));
        int pressureMinimum = sharedPref.getInt(getString(R.string.pressure_min),
                getResources().getInteger(R.integer.pressure_min_default_value));
        int pressureMaximum = sharedPref.getInt(getString(R.string.pressure_max),
                getResources().getInteger(R.integer.pressure_max_default_value));

        etHiddenSettingsSMAWindowSize = (EditText) findViewById(R.id.etHiddenSettingsSMAWindowSize);
        etHiddenSettingsSMAWindowSize.setTypeface(font);
        etHiddenSettingsSMAWindowSize.setText(Integer.toString(SMAWindowSize));
        etHiddenSettingsMinimum = (EditText) findViewById(R.id.etHiddenSettingsMinimum);
        etHiddenSettingsMinimum.setTypeface(font);
        etHiddenSettingsMinimum.setText(Integer.toString(pressureMinimum));
        etHiddenSettingsMaximum = (EditText) findViewById(R.id.etHiddenSettingsMaximum);
        etHiddenSettingsMaximum.setTypeface(font);
        etHiddenSettingsMaximum.setText(Integer.toString(pressureMaximum));

        bHiddenSettingsSave = (Button) findViewById(R.id.bHiddenSettingsSave);
        bHiddenSettingsSave.setTypeface(font);
        bHiddenSettingsSave.setOnClickListener(this);

        bHiddenSettingsReset = (Button) findViewById(R.id.bHiddenSettingsReset);
        bHiddenSettingsReset.setTypeface(font);
        bHiddenSettingsReset.setOnClickListener(this);

        TextView titleBar = (TextView) getWindow().findViewById(android.R.id.title);
        titleBar.setTypeface(font);
    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor = sharedPref.edit();

        switch (v.getId()) {
            case R.id.bHiddenSettingsSave:
                int SMAWindowSize, pressureMinimum, pressureMaximum;

                //Check if value is integer. If not, display an alert dialog
                try {
                    SMAWindowSize = Integer.parseInt(etHiddenSettingsSMAWindowSize.getText().toString());
                } catch (NumberFormatException e) {
                    showAlertMessage(getString(R.string.settings_error_title), "Please enter an integer number for SMA Window Size");
                    break;
                }


                try {
                    pressureMinimum = Integer.parseInt(etHiddenSettingsMinimum.getText().toString());
                } catch (NumberFormatException e) {
                    showAlertMessage(getString(R.string.settings_error_title), "Please enter an integer number for Minimum");
                    break;
                }

                try {
                    pressureMaximum = Integer.parseInt(etHiddenSettingsMaximum.getText().toString());
                } catch (NumberFormatException e) {
                    showAlertMessage(getString(R.string.settings_error_title), "Please enter an integer number for Maximum");
                    break;
                }

                if (pressureMinimum >= pressureMaximum) {
                    showAlertMessage(getString(R.string.settings_error_title), "Minimum must be smaller than Maximum");
                    break;
                }

                if (SMAWindowSize < 1) {
                    showAlertMessage(getString(R.string.settings_error_title), "SMA Window Size must be bigger than 0");
                    break;
                }

                //Write to preferences storage
                editor.putInt(getString(R.string.SMA_window_size), SMAWindowSize);
                editor.putInt(getString(R.string.pressure_min), pressureMinimum);
                editor.putInt(getString(R.string.pressure_max), pressureMaximum);
                editor.commit();

                finish();
                break;
            case R.id.bHiddenSettingsReset:
                SMAWindowSize = getResources().getInteger(R.integer.SMA_window_size_default_value);
                pressureMinimum = getResources().getInteger(R.integer.pressure_min_default_value);
                pressureMaximum = getResources().getInteger(R.integer.pressure_max_default_value);
                etHiddenSettingsSMAWindowSize.setText(Integer.toString(SMAWindowSize));
                etHiddenSettingsMinimum.setText(Integer.toString(pressureMinimum));
                etHiddenSettingsMaximum.setText(Integer.toString(pressureMaximum));
                break;
            default:
                break;
        }
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
}
