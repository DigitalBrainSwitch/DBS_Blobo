package uk.co.digitalbrainswitch.dbsblobodiary;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.GregorianCalendar;

public class CalendarDatePickerActivity extends Activity implements CalendarView.OnDateChangeListener, View.OnClickListener {

    CalendarView cal;
    TextView tvCalendarDisplay;
    Button bCalendarSelect;
    Typeface font;

    Boolean test = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_date_picker);

        font = ((MyApplication) getApplication()).getCustomTypeface();
        tvCalendarDisplay = (TextView) findViewById(R.id.tvCalendarDisplay);
        tvCalendarDisplay.setTypeface(font);
        bCalendarSelect = (Button) findViewById(R.id.bCalendarSelect);
        bCalendarSelect.setTypeface(font);
        bCalendarSelect.setOnClickListener(this);

        cal = (CalendarView) findViewById(R.id.cvDatePicker);
        cal.setSelectedWeekBackgroundColor(Color.TRANSPARENT);
        cal.setShowWeekNumber(false);
        cal.setFocusedMonthDateColor(Color.BLACK);
        cal.setUnfocusedMonthDateColor(getResources().getColor(R.color.light_gray));
        cal.setOnDateChangeListener(this);
        //Change to a day before then change it back to current date. This forces the calendar to call onSelectedDayChange
        cal.setDate(System.currentTimeMillis() - 86400000L);
        cal.setDate(System.currentTimeMillis());
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        // TODO Auto-generated method stub
        tvCalendarDisplay.setText("Selected: " + year + " / " + month + " / " + dayOfMonth);
        test = !test;
        bCalendarSelect.setEnabled(test);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bCalendarSelect:
                //When the select date button is pushed

                break;
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.calendar_date_picker, menu);
//        return true;
//    }

}
