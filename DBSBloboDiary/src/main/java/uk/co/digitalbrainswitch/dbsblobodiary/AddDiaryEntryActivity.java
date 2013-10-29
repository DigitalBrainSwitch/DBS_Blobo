package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.app.Activity;
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

import java.io.IOException;
import java.io.StringWriter;

public class AddDiaryEntryActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    Typeface font;
    TextView tvDiaryDate, tvDiaryTime, tvDiaryLocation;
    EditText etDiaryText;
    Button bDiaryAdd;

    boolean addFunction = true; //true add, false for update

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
        bDiaryAdd = (Button) findViewById(R.id.bDiaryAdd);
        bDiaryAdd.setTypeface(font);
        bDiaryAdd.setOnClickListener(this);
        bDiaryAdd.setOnLongClickListener(this);
        Drawable drawable = getResources().getDrawable((addFunction)? R.drawable.plus : R.drawable.update);
        float scale = 0.8f;
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * scale), (int) (drawable.getIntrinsicHeight() * scale));
        bDiaryAdd.setText(getString((addFunction) ? R.string.diary_button_add_string : R.string.diary_button_update_string));
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
                //if(confirmEntry(getApplicationContext())){
                    safeDiaryEntry();
                //}
                break;
            default:
                break;
        }
    }

    private static boolean answer;
    private boolean confirmEntry(Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle("Confirmation");
        dialog.setMessage("Choose Yes or No");
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                answer = true;
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                answer = false;
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
        return answer;
    }


    //use long click to bypass the confirmation dialog
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.bDiaryAdd:
                safeDiaryEntry();
                break;
            default:
                break;
        }
        return true;
    }

    private void safeDiaryEntry(){
        if (!etDiaryText.getText().toString().matches("")) { //check if diary is empty

            try {
                Toast.makeText(getApplicationContext(),
                        writeUsingJSON(tvDiaryDate.getText().toString(), tvDiaryTime.getText().toString(), tvDiaryLocation.getText().toString(), etDiaryText.getText().toString()),
                        Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            showAlertMessage("Diary is empty", "Please enter diary content.");
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
//        getMenuInflater().inflate(R.menu.add_diary_entry, menu);
//        return true;
//    }

}
