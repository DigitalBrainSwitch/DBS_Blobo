package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.digitalbrainswitch.dbsblobodiary.list_models.EventDateListModel;

public class MapListActivity extends ListActivity {

    Typeface font;
    AdapterView.OnItemClickListener itemListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.map_data_list);

        font = ((MyApplication) getApplication()).getCustomTypeface();

        TextView titleBar = (TextView) getWindow().findViewById(android.R.id.title);
        titleBar.setTypeface(font);

        itemListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                //Use intent to start visualisation activity
                Intent intent = new Intent(MapListActivity.this, MapActivity.class);
                String selectFileName = ((EventDateListModel) parent.getItemAtPosition(position)).getFileNameString();
                intent.putExtra(getString(R.string.intent_extra_selected_file_name), selectFileName);
                intent.putExtra(getString(R.string.intent_extra_number_of_map_points), getString(R.string.multiple_map_points));

                startActivity(intent);
                //MapListActivity.this.finish();
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Read the names of available files and refresh the list
        ListAdapter adapter = createAdapter();
        setListAdapter(adapter);
        getListView().setOnItemClickListener(itemListener);
    }

    //Creates and returns a custom list adapter for the current list activity
    private CustomListAdapter createAdapter() {
        File root = Environment.getExternalStorageDirectory();
        File dataDir = new File(root, getString(R.string.stored_data_directory));

        ArrayList<EventDateListModel> fileNamesList = new ArrayList<EventDateListModel>();
        for (File f : dataDir.listFiles()) {
            if (f.isFile()) {
                String fileName = f.getName();
                String displayName = processFileNameForListDisplay(f.getName());
                EventDateListModel item = new EventDateListModel(displayName, fileName);
                fileNamesList.add(item);
            }
        }
        CustomListAdapter adapter = new CustomListAdapter(this, R.layout.map_data_list, fileNamesList);

        return adapter;
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.map_list, menu);
//        return true;
//    }

    private static String processFileNameForListDisplay(String fileName){
        return removeExtension(fileName).replaceAll("_", " ").replaceAll("-", "/");
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

    //private class for creating a custom list adaptor
    private class CustomListAdapter extends ArrayAdapter {

        private Context mContext;
        private int id;
        private ArrayList<EventDateListModel> items;

        public CustomListAdapter(Context context, int textViewResourceId, ArrayList<EventDateListModel> list) {
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

            TextView text = (TextView) mView.findViewById(R.id.tvCustomMapListItem);

            if (items.get(position) != null) {
                text.setTypeface(font); //change font style
                text.setText(items.get(position).getDateString());
            }

            return mView;
        }
    }
}
