package uk.co.digitalbrainswitch.dbsblobodiary;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ShowTimeDataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_time_data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_time_data, menu);
        return true;
    }
    
}
