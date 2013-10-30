package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    private String writeUsingJSON(String diaryDate, String diaryTime, String diaryLocation, String diaryContent) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(getString(R.string.diary_data_key_date), diaryDate);
        jsonObject.put(getString(R.string.diary_data_key_time), diaryTime);
        jsonObject.put(getString(R.string.diary_data_key_location), diaryLocation);
        jsonObject.put(getString(R.string.diary_data_key_content), diaryContent);
        jsonObject.put(getString(R.string.diary_data_key_recorded_time), System.currentTimeMillis());

        return jsonObject.toString();
    }

/*    private static String writeUsingXMLSerializer (String diaryDate, String diaryTime, String diaryLocation, String diaryContent) throws IOException {
        final String NAMESPACE = "";
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        xmlSerializer.setOutput(writer);

        xmlSerializer.startDocument("UTF-8", true);

        xmlSerializer.startTag(NAMESPACE, "DiaryEntry");
        xmlSerializer.attribute(NAMESPACE, "Time", "");
        xmlSerializer.endTag(NAMESPACE, "DiaryEntry");

        xmlSerializer.endDocument();

        return writer.toString();
    }*/

    //display a confirmation dialog before saving diary entry
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bDiaryAdd:
                if (!etDiaryText.getText().toString().matches("")) { //check if diary is empty
                    confirmEntry(getApplicationContext());
                } else {
                    showAlertMessage(getString(R.string.diary_empty_alert_title), getString(R.string.diary_empty_alert_message));
                }
                break;
            default:
                break;
        }
    }

    //confirm whether the user wants to save diary entry
    private void confirmEntry(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //dialog.setTitle("Confirmation");
        dialog.setMessage("Save Entry to DBS Diary?");
        dialog.setCancelable(true);
        dialog.setPositiveButton("Save Entry", new DialogInterface.OnClickListener() {
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
            if (root.canWrite()) {
                FileWriter filewriter = new FileWriter(file, false);
                BufferedWriter out = new BufferedWriter(filewriter);
                out.write(writeUsingJSON(tvDiaryDate.getText().toString(), tvDiaryTime.getText().toString(), tvDiaryLocation.getText().toString(), etDiaryText.getText().toString()));
                out.close();
                Toast.makeText(getApplicationContext(), "Diary Entry Saved", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        } catch (JSONException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        }


//        try {
//            Toast.makeText(getApplicationContext(),
//                    writeUsingJSON(tvDiaryDate.getText().toString(), tvDiaryTime.getText().toString(), tvDiaryLocation.getText().toString(), etDiaryText.getText().toString()),
//                    Toast.LENGTH_LONG).show();
//            finish();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    //Method for displaying a popup alert dialog
    private void showAlertMessage(String title, String Message) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
        //popupBuilder.setTitle(title);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTypeface(font);
        tvTitle.setTextColor(getResources().getColor(R.color.dbs_blue));
        tvTitle.setPadding(30,20,30,20);
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.add_diary_entry, menu);
//        return true;
//    }

}
