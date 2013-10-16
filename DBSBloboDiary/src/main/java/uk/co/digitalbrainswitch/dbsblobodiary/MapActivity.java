package uk.co.digitalbrainswitch.dbsblobodiary;

import android.content.Context;
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.StringTokenizer;
import java.util.TreeMap;

import uk.co.digitalbrainswitch.dbsblobodiary.location.TimeLocation;

public class MapActivity extends Activity implements GoogleMap.OnMarkerClickListener {

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
    }


    @Override
    protected void onResume() {
        super.onResume();

        Bundle bundle = getIntent().getExtras();

        //check if single or multiple points
        String numberOfPoint = bundle.getString(getString(R.string.intent_extra_number_of_map_points));

        if(numberOfPoint.compareTo(getString(R.string.multiple_map_points)) == 0){
            initialiseMultiplePointsMap(bundle);
        }else if(numberOfPoint.compareTo(getString(R.string.single_map_point)) == 0){
            initialiseSinglePointMap(bundle);
        }
    }

    private void initialiseMultiplePointsMap(Bundle bundle){
        data = new TreeMap<Long, TimeLocation>();
        String selectedFileName = bundle.getString(getString(R.string.intent_extra_selected_file_name));
        readDataFromFile(selectedFileName);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(TreeMap.Entry<Long, TimeLocation> entry : data.entrySet()){
            TimeLocation tl = entry.getValue();
            LatLng latLng = new LatLng(tl.getLatitude(), tl.getLongitude());
            Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng));

            String addressText = getAddress(latLng);
            marker.setTitle("Time: " + getDateTime(tl.getTimeInMillisecond()));
            marker.setSnippet(addressText);
            marker.showInfoWindow();
            builder.include(latLng);
            googleMap.setOnMarkerClickListener(this);
        }


        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);

        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                googleMap.moveCamera(cu);
                googleMap.setOnCameraChangeListener(null);
            }
        });
//        googleMap.moveCamera(cu);

    }

    private void readDataFromFile(String fileName) {
        File root = Environment.getExternalStorageDirectory();
        File storedDirectory = new File(root, getString(R.string.stored_data_directory));
        File file = new File(storedDirectory, fileName + ".txt");
        try {
            FileInputStream inputStream = new FileInputStream(file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString;

                //Read every line from file. Discard pressure values that are lower than the threshold.
                while ((receiveString = bufferedReader.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(receiveString, ";");
                    String timeString = st.nextToken();
                    String locationString = st.nextToken();
                    StringTokenizer stLocation = new StringTokenizer(locationString, ",");
                    String latitudeString = stLocation.nextToken();
                    String longitudeString = stLocation.nextToken();

                    long timeInMillisecond = Long.parseLong(timeString);
                    double latitude = Double.parseDouble(latitudeString);
                    double longitude = Double.parseDouble(longitudeString);

                    TimeLocation timeLocation = new TimeLocation(timeInMillisecond, latitude, longitude);
                    data.put(timeInMillisecond, timeLocation);
                }
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
    }

    private void initialiseSinglePointMap(Bundle bundle){
        TimeLocation tl = bundle.getParcelable(getString(R.string.intent_extra_time_location));

        LatLng latLng = new LatLng(tl.getLatitude(), tl.getLongitude());
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng));

        String addressText = getAddress(latLng);

        marker.setTitle("Time: " + getDateTime(tl.getTimeInMillisecond()));
        marker.setSnippet(addressText);
        marker.showInfoWindow();
        googleMap.setOnMarkerClickListener(this);
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13), 2000, null);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
    }

    //resolve address from geolocation (need internet)
    private String getAddress(LatLng latLng){
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

    private String getDateTime(long timeInMilliSecond){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

        //Start the diary for reflection
        return false;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.map, menu);
//        return true;
//    }
    
}
