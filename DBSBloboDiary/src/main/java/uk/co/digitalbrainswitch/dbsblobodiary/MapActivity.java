package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TreeMap;

import uk.co.digitalbrainswitch.dbsblobodiary.location.TimeLocation;

public class MapActivity extends Activity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    //Mock up location: Lancaster 54.048606,-2.800511
    //Mock up location: Lancaster University 54.011653,-2.790509

    private GoogleMap googleMap;
    TreeMap<Long, TimeLocation> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        //Display the point on the map
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.fShowMap)).getMap(); //get MapFragment from layout
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        Bundle bundle = getIntent().getExtras();

        //check if single or multiple points
        String numberOfPoint = bundle.getString(getString(R.string.intent_extra_number_of_map_points));

        if (numberOfPoint.compareTo(getString(R.string.multiple_map_points)) == 0) {
            initialiseMultiplePointsMap(bundle);
        } else if (numberOfPoint.compareTo(getString(R.string.single_map_point)) == 0) {
            initialiseSinglePointMap(bundle);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initialiseMultiplePointsMap(Bundle bundle) {
        data = new TreeMap<Long, TimeLocation>();
        String selectedFileName = bundle.getString(getString(R.string.intent_extra_selected_file_name));
        readDataFromFile(selectedFileName);

        if (data.size() == 0) {
            showAlertMessage("Error", "Error reading data from file: " + selectedFileName);
            return;
        }

        //add location markers onto map
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (TreeMap.Entry<Long, TimeLocation> entry : data.entrySet()) {
            TimeLocation tl = entry.getValue();
            LatLng latLng = new LatLng(tl.getLatitude(), tl.getLongitude());
            Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng));

            String addressText = getAddress(latLng);
//            marker.setTitle("Time: " + getDateTime(tl.getTimeInMillisecond()));
            marker.setTitle(getDateTime(tl.getTimeInMillisecond()));
            marker.setSnippet(addressText);
            marker.showInfoWindow();
            builder.include(latLng);
//            googleMap.setOnMarkerClickListener(this);
            googleMap.setOnInfoWindowClickListener(this);
        }

        //move camera to show all markers
        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
        final float defaultMaxZoomLevel = 15f;
        final CameraUpdate zoom = CameraUpdateFactory.zoomTo(defaultMaxZoomLevel); //level 15 zoom
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                googleMap.moveCamera(cu);
                //set default max zoom to defaultMaxZoomLevel (level 15)
                if (googleMap.getCameraPosition().zoom > defaultMaxZoomLevel)
                    googleMap.moveCamera(zoom);
                googleMap.setOnCameraChangeListener(null);
            }
        });
    }

    private void readDataFromFile(String fileName) {
        File root = Environment.getExternalStorageDirectory();
        File storedDirectory = new File(root, getString(R.string.stored_data_directory));
        File file = new File(storedDirectory, fileName);
        try {
            FileInputStream inputStream = new FileInputStream(file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString;

                //Read every line from file. Discard pressure values that are lower than the threshold.
                while ((receiveString = bufferedReader.readLine()) != null) {
                    long timeInMillisecond = -1;
                    double latitude = -1;
                    double longitude = -1;
                    try {
                        StringTokenizer st = new StringTokenizer(receiveString, ";");
                        String timeString = st.nextToken();
                        String locationString = st.nextToken();
                        StringTokenizer stLocation = new StringTokenizer(locationString, ",");
                        String latitudeString = stLocation.nextToken();
                        String longitudeString = stLocation.nextToken();
                        timeInMillisecond = Long.parseLong(timeString);
                        latitude = Double.parseDouble(latitudeString);
                        longitude = Double.parseDouble(longitudeString);

                        TimeLocation timeLocation = new TimeLocation(timeInMillisecond, latitude, longitude);
                        data.put(timeInMillisecond, timeLocation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
    }

    private void initialiseSinglePointMap(Bundle bundle) {
        TimeLocation tl = bundle.getParcelable(getString(R.string.intent_extra_time_location));

        LatLng latLng = new LatLng(tl.getLatitude(), tl.getLongitude());
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng));

        String addressText = getAddress(latLng);

        //marker.setTitle("Time: " + getDateTime(tl.getTimeInMillisecond()));
        marker.setTitle(getDateTime(tl.getTimeInMillisecond()));
        marker.setSnippet(addressText);
        marker.showInfoWindow();
//        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13), 2000, null);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
    }

    //resolve address from geolocation (need internet)
    private String getAddress(LatLng latLng) {
        String addressText = "";

        if (isOnline()) {
            Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses.size() > 0) {
                    String display = "";
                    for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
                        display += addresses.get(0).getAddressLine(i) + "\n";
                    }
                    addressText += display;
                } else {
                    addressText += "Error: addresses size is " + addresses.size();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return addressText;
    }

    private String getDateTime(long timeInMilliSecond) {
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSecond);
        return formatter.format(calendar.getTime());
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        //Toast.makeText(getApplicationContext(), marker.getTitle() + " " + marker.getSnippet(), Toast.LENGTH_LONG).show();
        //Start the diary for reflection
        return false;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.map, menu);
//        return true;
//    }

    //Method for displaying a popup alert dialog
    private void showAlertMessage(String title, String Message) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
        popupBuilder.setTitle(title);
        popupBuilder.setMessage(Message);
        popupBuilder.setPositiveButton("OK", null);
        popupBuilder.show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String dateString = getDateString(marker.getTitle());
        String timeString = getTimeString(marker.getTitle());
        String directoryName = processDateForFile(dateString);
        String fileName = processInfoForFilename(marker.getTitle());

        String diaryDate = dateString;
        String diaryTime = timeString;
        String diaryLocation = marker.getSnippet();
        String diaryContent = "";
        String diaryLatitude = marker.getPosition().latitude + "";
        String diaryLongitude = marker.getPosition().longitude + "";
        String diaryCreatedTime = "";
        String diaryLastUpdated = "";
        boolean addNewEntry = true;

        File root = Environment.getExternalStorageDirectory();
        File directory = new File(root, getString(R.string.stored_diary_directory) + "/" + directoryName);
        File diaryEntryFile = new File(directory, fileName + ".txt");

//        Toast.makeText(getApplicationContext(), diaryEntryFile.getAbsolutePath(), Toast.LENGTH_LONG).show();


        //if file exist, it means that an entry already exists. update entry.
        if (diaryEntryFile.exists()) {
            //read from file and update
            JSONObject diaryJSONObject = parseJSONData(diaryEntryFile);
            if (diaryJSONObject != null) {
                try {
                    diaryDate = diaryJSONObject.getString(getString(R.string.diary_data_key_date));
                    diaryTime = diaryJSONObject.getString(getString(R.string.diary_data_key_time));
                    diaryLocation = diaryJSONObject.getString(getString(R.string.diary_data_key_location));
                    diaryContent = diaryJSONObject.getString(getString(R.string.diary_data_key_content));
                    diaryLastUpdated = diaryJSONObject.getString(getString(R.string.diary_data_key_last_updated_time));
                    diaryLatitude = diaryJSONObject.getString(getString(R.string.diary_data_key_location_latitude));
                    diaryLongitude = diaryJSONObject.getString(getString(R.string.diary_data_key_location_longitude));
                    diaryCreatedTime = diaryJSONObject.getString(getString(R.string.diary_data_key_created_time));
                    addNewEntry = false;

                    //Toast.makeText(getApplicationContext(), diaryDate + "\n" + diaryTime + "\n" + diaryLocation + "\n" + diaryContent + "\n" + diaryLastUpdated + "\n" + diaryLatitude + "\n" + diaryLongitude, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

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

        //Toast.makeText(getApplicationContext(), fileName + "\n" + dateString + "\n" + marker.getPosition().latitude + "," + marker.getPosition().longitude +"\n" + timeString, Toast.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), marker.getTitle() + " " + marker.getSnippet(), Toast.LENGTH_LONG).show();
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

    private String processDateForFile(String dateString) {
        return dateString.replaceAll("/", "_");
    }

    private String processTimeForFile(String timeString){
        return timeString.replaceAll(":", "\\.");
    }

    private String processInfoForFilename(String text) {
        return text.replaceAll("/", "_").replaceAll(" ", "-").replaceAll(":", "\\.");
    }

    private String getDateString(String text) {
        StringTokenizer st = new StringTokenizer(text, " ");
        return st.nextToken();
    }

    private String getTimeString(String text) {
        StringTokenizer st = new StringTokenizer(text, " ");
        st.nextToken();
        return st.nextToken();
    }

}
