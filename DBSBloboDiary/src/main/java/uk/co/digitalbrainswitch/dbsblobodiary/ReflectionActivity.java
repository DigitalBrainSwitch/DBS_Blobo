package uk.co.digitalbrainswitch.dbsblobodiary;

import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibRead:
                //Display previous diary entries
                break;
            case R.id.ibWrite:
                //Add a new diary entry
                break;
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.reflection, menu);
//        return true;
//    }
    
}
