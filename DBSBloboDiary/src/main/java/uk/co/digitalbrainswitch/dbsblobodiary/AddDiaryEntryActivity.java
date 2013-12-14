package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.digitalbrainswitch.dbsblobodiary.location.TimeLocation;

public class AddDiaryEntryActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    Typeface font;
    TextView tvDiaryDate, tvDiaryTime, tvDiaryLocation;
    EditText etDiaryText;
    Button bDiaryAdd;

    boolean isAddFunction = true; //true add, false for update

    private String _diaryDate = "";
    private String _diaryTime = "";
    private String _diaryLocation = "";
    private String _diaryContent = "";
    private String _diaryLatitude = "";
    private String _diaryLongitude = "";
    private String _diaryCreatedtime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_diary_entry);
        font = ((MyApplication) getApplication()).getCustomTypeface();
        this.initialise();
        this.getIntentExtras();
        this.initialiseAddButton();
    }

    private void getIntentExtras() {
        Bundle bundle = getIntent().getExtras();
        _diaryDate = bundle.getString(getString(R.string.intent_extra_diary_entry_date));
        _diaryTime = bundle.getString(getString(R.string.intent_extra_diary_entry_time));
        _diaryLocation = bundle.getString(getString(R.string.intent_extra_diary_entry_location));
        _diaryContent = bundle.getString(getString(R.string.intent_extra_diary_entry_content));
        isAddFunction = bundle.getBoolean(getString(R.string.intent_extra_diary_entry_add_or_update));
        etDiaryText.setText(_diaryContent);
        _diaryLatitude = bundle.getString(getString(R.string.intent_extra_diary_entry_location_latitude));
        _diaryLongitude = bundle.getString(getString(R.string.intent_extra_diary_entry_location_longitude));
        _diaryCreatedtime = bundle.getString(getString(R.string.intent_extra_diary_entry_created_time));

        tvDiaryDate.setText(processDateForDisplay(_diaryDate));
        tvDiaryTime.setText(processTimeForDisplay(_diaryTime));
        tvDiaryLocation.setText(_diaryLocation);
    }

    private String processDateForDisplay(String dateString) {
        return dateString.replaceAll("_", "/");
    }

    private String processTimeForDisplay(String timeString) {
        return timeString.replaceAll("\\.", ":");
    }

    private void initialise() {
        ((TextView) findViewById(R.id.tvDiaryDateLabel)).setTypeface(font);
        ((TextView) findViewById(R.id.tvDiaryTimeLabel)).setTypeface(font);
        ((TextView) findViewById(R.id.tvDiaryLocationLabel)).setTypeface(font);
        tvDiaryDate = (TextView) findViewById(R.id.tvDiaryDate);
        tvDiaryDate.setTypeface(font);
        tvDiaryTime = (TextView) findViewById(R.id.tvDiaryTime);
        tvDiaryTime.setTypeface(font);
        tvDiaryLocation = (TextView) findViewById(R.id.tvDiaryLocation);
        tvDiaryLocation.setTypeface(font);
        tvDiaryLocation.setSelected(true);
        tvDiaryLocation.setOnClickListener(this);
        tvDiaryLocation.setOnLongClickListener(this);
        etDiaryText = (EditText) findViewById(R.id.etDiaryText);
        etDiaryText.setTypeface(font);
    }

    private void initialiseAddButton() {
        bDiaryAdd = (Button) findViewById(R.id.bDiaryAdd);
        bDiaryAdd.setTypeface(font);
        bDiaryAdd.setOnClickListener(this);
        bDiaryAdd.setOnLongClickListener(this);
        Drawable drawable = getResources().getDrawable((isAddFunction) ? R.drawable.plus : R.drawable.update);
        float scale = 0.8f;
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * scale), (int) (drawable.getIntrinsicHeight() * scale));
        bDiaryAdd.setText(getString((isAddFunction) ? R.string.diary_button_add_string : R.string.diary_button_update_string));
        bDiaryAdd.setCompoundDrawables(null, drawable, null, null);
    }

    private String writeUsingJSON(String diaryDate, String diaryTime, String diaryLocation, String diaryContent, String createdTime, String diaryLatitude, String diaryLongitude) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(getString(R.string.diary_data_key_date), diaryDate);
        jsonObject.put(getString(R.string.diary_data_key_time), diaryTime);
        jsonObject.put(getString(R.string.diary_data_key_location), diaryLocation);
        jsonObject.put(getString(R.string.diary_data_key_content), diaryContent);
        long currentTime = System.currentTimeMillis();
        jsonObject.put(getString(R.string.diary_data_key_last_updated_time), currentTime);
        long createdTimeLong = Long.parseLong(createdTime); //convert createdTime back to a long value
        jsonObject.put(getString(R.string.diary_data_key_created_time), (isAddFunction) ? currentTime : createdTimeLong);

        jsonObject.put(getString(R.string.diary_data_key_location_latitude), diaryLatitude);
        jsonObject.put(getString(R.string.diary_data_key_location_longitude), diaryLongitude);

        return jsonObject.toString();
    }

    //display a confirmation dialog before saving diary entry
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bDiaryAdd:
                if (!etDiaryText.getText().toString().matches("")) { //check if diary is empty
                    confirmEntry();
                } else {
                    showAlertMessage(getString(R.string.diary_empty_alert_title), getString(R.string.diary_empty_alert_message));
                }
                break;
            case R.id.tvDiaryLocation:
//                tvDiaryLocation.setSelected(!tvDiaryLocation.isSelected());
                showAddressMessage(getString(R.string.add_diary_location_string), tvDiaryLocation.getText().toString());
                break;
            default:
                tvDiaryLocation.setSelected(false);
                break;
        }
    }

    //confirm whether the user wants to save diary entry
    private void confirmEntry() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //dialog.setTitle("Confirmation");
        dialog.setMessage(((isAddFunction) ? "Save" : "Update") + " DBS Diary Entry?");
        dialog.setCancelable(true);
        dialog.setPositiveButton((isAddFunction) ? "Save Entry" : "Update Entry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                saveDiaryEntry();
                finish();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                //do nothing
            }
        });
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


    //use long click to bypass the confirmation dialog
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.bDiaryAdd:
                if (!etDiaryText.getText().toString().matches("")) { //check if diary is empty
                    saveDiaryEntry();
                    finish();
                } else {
                    showAlertMessage(getString(R.string.diary_empty_alert_title), getString(R.string.diary_empty_alert_message));
                }
                break;
            case R.id.tvDiaryLocation:
                showLocationOnMap();
                break;
            default:
                break;
        }
        return true;
    }

    private void saveDiaryEntry() {
        File root = Environment.getExternalStorageDirectory();
        File diaryDirectory = new File(root + getString(R.string.stored_diary_directory) + "/" + _diaryDate);
        if (!diaryDirectory.exists()) {
            boolean success = diaryDirectory.mkdirs();
            if (!success) {
                showAlertMessage("Error", "Unable to create " + diaryDirectory.getAbsolutePath());
            }
        }

        File file = new File(diaryDirectory, _diaryDate + "-" + _diaryTime + ".txt");
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

        try {
            if (file.canWrite()) {
                FileWriter filewriter = new FileWriter(file, false);
                BufferedWriter out = new BufferedWriter(filewriter);
                out.write(
                        writeUsingJSON(tvDiaryDate.getText().toString(),
                                tvDiaryTime.getText().toString(),
                                tvDiaryLocation.getText().toString(),
                                etDiaryText.getText().toString(),
                                _diaryCreatedtime,
                                _diaryLatitude,
                                _diaryLongitude));
                out.close();
                Toast.makeText(getApplicationContext(), "Diary Entry Saved", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        } catch (JSONException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
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

    private void showAddressMessage(String title, String Message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(Message);
        dialog.setCancelable(true);
        if (!(_diaryLatitude.equalsIgnoreCase(getString(R.string.diary_entry_empty_latitude)) ||
                _diaryLongitude.equalsIgnoreCase(getString(R.string.diary_entry_empty_longitude)))) {
            dialog.setPositiveButton("Show Map", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int buttonId) {
                    showLocationOnMap();
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int buttonId) {
                    //do nothing
                }
            });
        } else {
            dialog.setPositiveButton("OK", null);
        }
        dialog.setIcon(R.drawable.ic_dialog_map);
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

    private void showLocationOnMap() {
        if (!(_diaryLatitude.equalsIgnoreCase(getString(R.string.diary_entry_empty_latitude)) ||
                _diaryLongitude.equalsIgnoreCase(getString(R.string.diary_entry_empty_longitude)))) {
            //TO DO
            //Open location in MapActivity
            //http://stackoverflow.com/questions/5306803/how-to-convert-the-following-string-to-date-or-calendar-object
            String dateTimeString = _diaryDate + "-" + _diaryTime;
            String pattern = "yyyy_MM_dd-HH.mm.ss";
            try {
                Date date = new SimpleDateFormat(pattern).parse(dateTimeString);
                double latitudeDouble = Double.parseDouble(_diaryLatitude);
                double longitudeDouble = Double.parseDouble(_diaryLongitude);
                TimeLocation tl = new TimeLocation(date.getTime(), latitudeDouble, longitudeDouble);
                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra(getString(R.string.intent_extra_time_location), tl);
                intent.putExtra(getString(R.string.intent_extra_number_of_map_points), getString(R.string.single_map_point));
                intent.putExtra(getString(R.string.intent_extra_disable_diary_in_map), true);
                startActivity(intent);

                //Toast.makeText(getApplicationContext(), date.toString() + "\n" + _diaryLatitude + "," + _diaryLongitude, Toast.LENGTH_LONG).show();
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Cannot open location in map.", Toast.LENGTH_LONG).show();
            }

        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.add_diary_entry, menu);
//        return true;
//    }

}
