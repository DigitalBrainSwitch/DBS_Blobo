package uk.co.digitalbrainswitch.dbsblobodiary;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;

import uk.co.digitalbrainswitch.dbsblobodiary.list_models.CalendarListModel;

public class CalendarDatePickerActivity extends Activity implements CalendarView.OnDateChangeListener, AdapterView.OnItemClickListener {

    Typeface font;
    CalendarView cal;
    TextView tvCalendarDisplay;
    ListView listView;
    ArrayList<CalendarListModel> calendarListModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        font = ((MyApplication) getApplication()).getCustomTypeface();
        setContentView(R.layout.calendar_date_picker);

        tvCalendarDisplay = (TextView) findViewById(R.id.tvCalendarDisplay);
        tvCalendarDisplay.setTypeface(font);
        listView = (ListView) findViewById(R.id.lvDiaryEvents);
        listView.setOnItemClickListener(this);

        cal = (CalendarView) findViewById(R.id.cvDatePicker);
        cal.setSelectedWeekBackgroundColor(Color.TRANSPARENT);
        cal.setShowWeekNumber(false);
        cal.setFocusedMonthDateColor(Color.BLACK);
        cal.setUnfocusedMonthDateColor(getResources().getColor(R.color.light_gray));
        cal.setOnDateChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Change to a day before then change it back to current date. This forces the calendar to call onSelectedDayChange
        cal.setDate(System.currentTimeMillis() - 86400001L);
        cal.setDate(System.currentTimeMillis());
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        GregorianCalendar selectedDate = new GregorianCalendar(year, month, dayOfMonth);
        SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy_MM_dd");

        String displayDateString = displayFormat.format(selectedDate.getTime());
        String dateDirectory = fileFormat.format(selectedDate.getTime());

        calendarListModelArrayList = getListOfDiaryEntriesFileNames(getString(R.string.stored_diary_directory), dateDirectory);
        if (calendarListModelArrayList.size() > 0) {
            tvCalendarDisplay.setText(calendarListModelArrayList.size() + " DBS Diary Record(s) Found for " + displayDateString);
        } else {
            tvCalendarDisplay.setText("No DBS Diary Record for " + displayDateString);
        }
        //update the listview with the content of the selected day
        listView.setAdapter(createAdapter());
    }

    //check if a given path + directory exists
    private boolean checkIfDirectoryExist(String path, String directoryName) {
        File root = Environment.getExternalStorageDirectory();
        //remove the last '/' if it's in the path
        String dirPath = (path.charAt(path.length() - 1) == '/') ? path.substring(0, path.length() - 1) : path;
        File directory = new File(root, dirPath + "/" + directoryName);
        return (directory.exists() && directory.isDirectory());
    }

    private ArrayList<CalendarListModel> getListOfDiaryEntriesFileNames(String path, String directoryName) {
        ArrayList<CalendarListModel> returnList = new ArrayList<CalendarListModel>();
        if (checkIfDirectoryExist(path, directoryName)) {
            File root = Environment.getExternalStorageDirectory();
            String dirPath = (path.charAt(path.length() - 1) == '/') ? path.substring(0, path.length() - 1) : path;
            File directory = new File(root, dirPath + "/" + directoryName);

            File[] files = directory.listFiles();
            //sort by file names in ascending order
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            int count = 0;
            for (File f : files) {
                if (f.isFile() && hasExtension(f.getName(), "txt")) {
                    count++;
                    String fileNameWithoutExtention = removeExtension(f.getName());

                    String displayFileName = count + ". " + processFileNameForListDisplay(fileNameWithoutExtention); //Process File name for display here
                    CalendarListModel entry = new CalendarListModel(displayFileName, fileNameWithoutExtention);
                    returnList.add(entry);
                }
            }
        }
        return returnList;
    }

    //a quick way to convert yyyy_MM_dd-hh.mm.ss to yyyy/MM/dd hh:mm:ss
    private String processFileNameForListDisplay(String fileName){
        return fileName.replaceAll("\\.", ":").replaceAll("_", "/").replaceAll("-", " ");
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //########## TO DO ##########
        //Replace this toast with starting an activity to view diary record (with the filenamestring in the intent)
        Toast.makeText(getApplicationContext(), ((CalendarListModel) parent.getItemAtPosition(position)).getFileNameString(), Toast.LENGTH_SHORT).show();
        //########## TO DO ##########
    }

    //build arrayadapter for displaying diary entries
    //check tutorial http://www.vogella.com/articles/AndroidListView/article.html
    //and http://www.javacodegeeks.com/2013/06/android-listview-tutorial-and-basic-example.html
    //http://androidexample.com/How_To_Create_A_Custom_Listview_-_Android_Example/index.php?view=article_discription&aid=67&aaid=92

    private CustomListAdapter createAdapter() {
        return new CustomListAdapter(this, R.layout.calendar_diary_list_entry, calendarListModelArrayList);
    }

    //private class for creating a custom list adaptor
    private class CustomListAdapter extends ArrayAdapter {

        private Context mContext;
        private int id;
        private ArrayList<CalendarListModel> items;

        public CustomListAdapter(Context context, int textViewResourceId, ArrayList<CalendarListModel> list) {
            super(context, textViewResourceId, list);
            mContext = context;
            id = textViewResourceId;
            items = list;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            View mView = v;
            if (mView == null) {
                LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = vi.inflate(id, null);
            }

            TextView text = (TextView) mView.findViewById(R.id.tvCustomCalendarListItem);

            if (items.get(position) != null) {
                text.setTypeface(font); //change font style
                text.setText(items.get(position).getDisplayText());
            }
            return mView;
        }


    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.calendar_date_picker, menu);
//        return true;
//    }
}
