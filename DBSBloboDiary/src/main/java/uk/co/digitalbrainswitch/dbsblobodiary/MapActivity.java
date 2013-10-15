package uk.co.digitalbrainswitch.dbsblobodiary;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import java.util.ArrayList;

import uk.co.digitalbrainswitch.dbsblobodiary.location.TimeLocation;

public class MapActivity extends Activity {

    //Mock up location: Lancaster 54.048606,-2.800511
    //Mock up location: Lancaster University 54.011653,-2.790509

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        Bundle extras = getIntent().getExtras();

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.map, menu);
//        return true;
//    }
    
}
