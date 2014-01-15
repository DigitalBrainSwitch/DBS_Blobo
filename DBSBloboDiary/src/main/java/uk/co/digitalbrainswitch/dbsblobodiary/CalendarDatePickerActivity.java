package uk.co.digitalbrainswitch.dbsblobodiary;

import android.content.Context;
import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

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

        this.initialise();

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

        //Change to a day before then change it back to current date. This forces the calendar to call onSelectedDayChange
        cal.setDate(System.currentTimeMillis() - 86400001L);
        cal.setDate(System.currentTimeMillis());
    }

    private void initialise() {
        //set custom title bar http://stackoverflow.com/a/8748802
        this.getActionBar().setDisplayShowCustomEnabled(true);
        this.getActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.titleview, null);
        //if you need to customize anything else about the text, do it here.
        //I'm using a custom TextView with a custom font in my layout xml so all I need to do is set title
        TextView tvTitle = (TextView) v.findViewById(R.id.title);
        tvTitle.setText(this.getTitle());
        tvTitle.setTextColor(getResources().getColor(android.R.color.white));
        tvTitle.setTypeface(font);
        //assign the view to the actionbar
        this.getActionBar().setCustomView(v);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            tvCalendarDisplay.setText(calendarListModelArrayList.size() + " DBS Diary Record" + (calendarListModelArrayList.size() > 1 ? "s" : "") + " Found for " + displayDateString);
            tvCalendarDisplay.setTextColor(getResources().getColor(R.color.dbs_blue));
        } else {
            tvCalendarDisplay.setText("No DBS Diary Record for " + displayDateString);
            tvCalendarDisplay.setTextColor(getResources().getColor(R.color.light_gray));
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
    private String processFileNameForListDisplay(String fileName) {
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
        String fileName = ((CalendarListModel) parent.getItemAtPosition(position)).getFileNameString();
        String directoryName = getDateString(fileName);

        File root = Environment.getExternalStorageDirectory();
        File directory = new File(root, getString(R.string.stored_diary_directory) + "/" + directoryName);
        File diaryEntryFile = new File(directory, fileName + ".txt");

        JSONObject diaryJSONObject = parseJSONData(diaryEntryFile);
        if (diaryJSONObject != null) {
            try {
                String diaryDate = diaryJSONObject.getString(getString(R.string.diary_data_key_date));
                String diaryTime = diaryJSONObject.getString(getString(R.string.diary_data_key_time));
                String diaryLocation = diaryJSONObject.getString(getString(R.string.diary_data_key_location));
                String diaryContent = diaryJSONObject.getString(getString(R.string.diary_data_key_content));
                String diaryLastUpdated = diaryJSONObject.getString(getString(R.string.diary_data_key_last_updated_time));
                String diaryLatitude = diaryJSONObject.getString(getString(R.string.diary_data_key_location_latitude));
                String diaryLongitude = diaryJSONObject.getString(getString(R.string.diary_data_key_location_longitude));
                String diaryCreatedTime = diaryJSONObject.getString(getString(R.string.diary_data_key_created_time));
                boolean addNewEntry = false;

                Intent intent = new Intent(this, AddDiaryEntryActivity.class);
                intent.putExtra(getString(R.string.intent_extra_diary_entry_date), processDateForFile(diaryDate));
                intent.putExtra(getString(R.string.intent_extra_diary_entry_time), processTimeForFile(diaryTime));
                intent.putExtra(getString(R.string.intent_extra_diary_entry_location), diaryLocation);
                intent.putExtra(getString(R.string.intent_extra_diary_entry_content), diaryContent);
                intent.putExtra(getString(R.string.intent_extra_diary_entry_add_or_update), addNewEntry);
                intent.putExtra(getString(R.string.intent_extra_diary_entry_created_time), diaryCreatedTime);

                intent.putExtra(getString(R.string.intent_extra_diary_entry_location_latitude), diaryLatitude);
                intent.putExtra(getString(R.string.intent_extra_diary_entry_location_longitude), diaryLongitude);

                startActivity(intent);


                //Toast.makeText(getApplicationContext(), diaryDate + "\n" + diaryTime + "\n" + diaryLocation + "\n" + diaryContent + "\n" + diaryLastUpdated + "\n" + diaryLatitude + "\n" + diaryLongitude, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Toast.makeText(getApplicationContext(), ((CalendarListModel) parent.getItemAtPosition(position)).getFileNameString(), Toast.LENGTH_SHORT).show();
//        Toast.makeText(getApplicationContext(), diaryEntryFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        //########## TO DO ##########
    }

    private String processDateForFile(String dateString) {
        return dateString.replaceAll("/", "_");
    }

    private String processTimeForFile(String timeString) {
        return timeString.replaceAll(":", "\\.");
    }

    private JSONObject parseJSONData(File diaryEntry) {
        String jsonString = null;
        JSONObject jsonObject = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(diaryEntry);
            int sizeOfJSONFile = fileInputStream.available();
            byte[] bytes = new byte[sizeOfJSONFile];
            fileInputStream.read(bytes);
            fileInputStream.close();
            jsonString = new String(bytes, "UTF-8");
            jsonObject = new JSONObject(jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private String getDateString(String text) {
        StringTokenizer st = new StringTokenizer(text, "-");
        return st.nextToken();
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
