package uk.co.digitalbrainswitch.dbsblobodiary;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ShowEventsActivity extends Activity implements View.OnClickListener {

    Typeface font;
    ImageButton ibShowByTimeline, ibShowByMap;
    TextView tvShowByTimeline, tvShowByMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_events_options);
        font = ((MyApplication) getApplication()).getCustomTypeface();
        this.initialise();
    }

    private void initialise() {
        ibShowByTimeline = (ImageButton) findViewById(R.id.ibShowByTimeline);
        ibShowByTimeline.setOnClickListener(this);
        ibShowByMap = (ImageButton) findViewById(R.id.ibShowByMap);
        ibShowByMap.setOnClickListener(this);
        tvShowByTimeline = (TextView) findViewById(R.id.tvShowByTimeline);
        tvShowByTimeline.setTypeface(font);
        tvShowByMap = (TextView) findViewById(R.id.tvShowByMap);
        tvShowByMap.setTypeface(font);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibShowByTimeline:
                showTimeData();
                //finish();
                break;
            case R.id.ibShowByMap:
                showMap();
                //finish();
                break;
        }
    }

    private void showTimeData() {
        Intent intent = new Intent(this, TimeDataListActivity.class);
        startActivity(intent);
    }

    private void showMap() {
        Intent intent = new Intent(this, MapListActivity.class);
        startActivity(intent);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.show_events, menu);
//        return true;
//    }

}
