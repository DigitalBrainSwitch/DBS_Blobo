package uk.co.digitalbrainswitch.dbsblobodiary;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReflectionActivity extends Activity implements View.OnClickListener {

    Typeface font;
    ImageButton ibRead, ibWrite;
    TextView tvViewEntriesText, tvAddEntryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reflection);
        font = ((MyApplication) getApplication()).getCustomTypeface();
        this.initialise();
    }

    private void initialise() {
        ibRead = (ImageButton) findViewById(R.id.ibRead);
        ibRead.setOnClickListener(this);
        ibWrite = (ImageButton) findViewById(R.id.ibWrite);
        ibWrite.setOnClickListener(this);
        tvViewEntriesText = (TextView) findViewById(R.id.tvViewEntriesText);
        tvViewEntriesText.setTypeface(font);
        tvAddEntryText = (TextView) findViewById(R.id.tvAddEntryText);
        tvAddEntryText.setTypeface(font);

        TextView titleBar = (TextView) getWindow().findViewById(android.R.id.title);
        titleBar.setTypeface(font);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()){
            case R.id.ibRead:
                //Display previous diary entries
                intent = new Intent(this, CalendarDatePickerActivity.class);
                startActivity(intent);
                //finish();
                break;
            case R.id.ibWrite:
                //Add a new diary entry
                Calendar c = Calendar.getInstance();
                intent = new Intent(this, AddDiaryEntryActivity.class);
                intent.putExtra(getString(R.string.intent_extra_diary_entry_date), getDateString(c));
                intent.putExtra(getString(R.string.intent_extra_diary_entry_time), getTimeString(c));
                intent.putExtra(getString(R.string.intent_extra_diary_entry_location), getString(R.string.diary_entry_empty_location_general_entry));
                intent.putExtra(getString(R.string.intent_extra_diary_entry_content), "");
                boolean addNewEntry = true;
                intent.putExtra(getString(R.string.intent_extra_diary_entry_add_or_update), addNewEntry);
                intent.putExtra(getString(R.string.intent_extra_diary_entry_created_time), "");

                intent.putExtra(getString(R.string.intent_extra_diary_entry_location_latitude), getString(R.string.diary_entry_empty_latitude));
                intent.putExtra(getString(R.string.intent_extra_diary_entry_location_longitude), getString(R.string.diary_entry_empty_longitude));

                startActivity(intent);
                //finish();
                break;
        }
    }

    private String getDateString(Calendar c){
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd");
        return df.format(c.getTime());
    }

    private String getTimeString(Calendar c){
        SimpleDateFormat df = new SimpleDateFormat("HH.mm.ss");
        return df.format(c.getTime());
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.reflection, menu);
//        return true;
//    }
    
}
