package uk.co.digitalbrainswitch.dbsblobodiary;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import uk.co.digitalbrainswitch.dbsblobodiary.location.TimeLocation;

public class MapActivity extends Activity implements GoogleMap.OnMarkerClickListener {

    //Mock up location: Lancaster 54.048606,-2.800511
    //Mock up location: Lancaster University 54.011653,-2.790509

    private GoogleMap googleMap;


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

        if(numberOfPoint.compareTo(getString(R.string.multiple_map_points)) == 0){
            initialiseMultiplePointsMap(bundle);
        }else if(numberOfPoint.compareTo(getString(R.string.single_map_point)) == 0){
            initialiseSinglePointMap(bundle);
        }
    }

    private void initialiseMultiplePointsMap(Bundle bundle){
        String selectedFileName = bundle.getString(getString(R.string.intent_extra_selected_file_name));
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
