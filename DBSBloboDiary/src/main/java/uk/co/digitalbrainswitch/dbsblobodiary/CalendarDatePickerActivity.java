package uk.co.digitalbrainswitch.dbsblobodiary;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import uk.co.digitalbrainswitch.dbsblobodiary.util.CustomCalendarView;

public class CalendarDatePickerActivity extends Activity implements CalendarView.OnDateChangeListener {

    //CustomCalendarView cal;
    CalendarView cal;
    TextView tvCalendarDisplay;
    Button bCalendarSelect;
    Typeface font;
    LinearLayout layoutCalendar, layoutEntryList;
    String dateDirectory = "";


    Boolean test = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_date_picker);

        font = ((MyApplication) getApplication()).getCustomTypeface();
        tvCalendarDisplay = (TextView) findViewById(R.id.tvCalendarDisplay);
        tvCalendarDisplay.setTypeface(font);
        //bCalendarSelect = (Button) findViewById(R.id.bCalendarSelect);
        //bCalendarSelect.setTypeface(font);
        //bCalendarSelect.setOnClickListener(this);
        //layoutCalendar = (LinearLayout) findViewById(R.id.layoutCalendar);

        layoutEntryList = (LinearLayout) findViewById(R.id.layoutEntryList);

        cal = (CalendarView) findViewById(R.id.cvDatePicker);
        //cal = new CustomCalendarView(getApplicationContext());
        //cal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        cal.setSelectedWeekBackgroundColor(Color.TRANSPARENT);
        cal.setShowWeekNumber(false);
        cal.setFocusedMonthDateColor(Color.BLACK);
        cal.setUnfocusedMonthDateColor(getResources().getColor(R.color.light_gray));
        cal.setOnDateChangeListener(this);
        //layoutCalendar.addView(cal);

        //Change to a day before then change it back to current date. This forces the calendar to call onSelectedDayChange
        cal.setDate(System.currentTimeMillis() - 86400001L);
        cal.setDate(System.currentTimeMillis());

    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        // TODO Auto-generated method stub
        GregorianCalendar selectedDate = new GregorianCalendar(year, month, dayOfMonth);
        SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy_MM_dd");
        dateDirectory = fileFormat.format(selectedDate.getTime());

        if (checkIfDirectoryExist(getString(R.string.stored_diary_directory), dateDirectory)) {
            //tvCalendarDisplay.setText("## DBS Diary Record(s) Found for " + displayFormat.format(selectedDate.getTime()));
            int numDiaryEntries = checkNumberOfDiaryEntries(getString(R.string.stored_diary_directory), dateDirectory);
            if (numDiaryEntries > 0) {
                tvCalendarDisplay.setText(numDiaryEntries + " DBS Diary Record(s) Found for " + displayFormat.format(selectedDate.getTime()));
            } else {
                tvCalendarDisplay.setText("No DBS Diary Record for " + displayFormat.format(selectedDate.getTime()));
            }
        } else {
            tvCalendarDisplay.setText("No DBS Diary Record for " + displayFormat.format(selectedDate.getTime()));
        }
    }

    //check if a given path + directory exists
    private boolean checkIfDirectoryExist(String path, String directoryName) {
        File root = Environment.getExternalStorageDirectory();
        //remove the last / if it's in the path
        String dirPath = (path.charAt(path.length() - 1) == '/') ? path.substring(0, path.length() - 1) : path;
        File directory = new File(root, dirPath + "/" + directoryName);
        return (directory.exists() && directory.isDirectory());
    }

    private int checkNumberOfDiaryEntries(String path, String directoryName) {
        int numberOfDiaryEntries = 0;
        if (checkIfDirectoryExist(path, directoryName)) {
            File root = Environment.getExternalStorageDirectory();
            String dirPath = (path.charAt(path.length() - 1) == '/') ? path.substring(0, path.length() - 1) : path;
            File directory = new File(root, dirPath + "/" + directoryName);

            for (File f : directory.listFiles()) {
                if (f.isFile() && hasExtension(f.getName(), "txt")) {
                    numberOfDiaryEntries++;
                    //add file name to array list
                }
            }
        }
        return numberOfDiaryEntries;
    }

    //check if a given fileName has extension string
    private static boolean hasExtension(String fileName, String extention) {
        return fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length()).equalsIgnoreCase(extention);
    }

    //method for removing file extension from file name
    private static String removeExtension(String s) {
        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path up to the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }

    //build arrayadapter for displaying diary entries
    //check tutorial http://www.vogella.com/articles/AndroidListView/article.html





//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.calendar_date_picker, menu);
//        return true;
//    }
}
