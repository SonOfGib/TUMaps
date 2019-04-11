package edu.temple.basic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import edu.temple.basic.dao.mockup.MockupLocations;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {



    private LocationManager lm;
    private LocationListener ll;
    private GoogleMap mMap;
    private Marker lastMarker;
    private FloatingActionButton fab;

    private ArrayList<edu.temple.basic.dao.Location> mLocations;
    private MockupLocations mMockup;

    public static final String WIKI_URL_EXTRA = "edu.temple.basic.WIKI_URL_EXTRA";

    // please work

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocations = new ArrayList<>(fetchMapLocations());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SupportMapFragment mapFragment = new SupportMapFragment();
        transaction.replace(R.id.mapView, mapFragment).commit();
        mapFragment.getMapAsync(MainActivity.this);

        lm = getSystemService(LocationManager.class);
        ll = makeLocationListener();

        fab = findViewById(R.id.fab1);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLocation();
            }
        });

        //WebView myWebView = findViewById(R.id.webView);
        //myWebView.loadUrl("http://ec2-34-203-104-209.compute-1.amazonaws.com/");
    }

    /*
     * Adds a location to the map where the user pressed it.
     * TODO Prompt user for name of location.
     * TODO Add location to storage
     */
    private void addLocation() {
        Toast.makeText(this, "tap new location", Toast.LENGTH_SHORT).show();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                //allPoints.add(point);
                //mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point));
                mMap.setOnMapClickListener(null);
            }
        });
    }

    // track your current location
    private LocationListener makeLocationListener(){
        Log.e( "marktrack", "kicked off makeLocationlistener");
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e( "marktrack", "location changed");

                //TODO This is a bug waiting to happen, markers aren't meant to persist
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (lastMarker != null) {
                    lastMarker.setPosition(latLng);
                }
                else {
                    MarkerOptions markerOptions = (new MarkerOptions())
                            .position(latLng)
                            .title("You");

                    lastMarker = mMap.addMarker(markerOptions);
                    Log.e( "marktrack", "added a marker");

                }

                //moved camera update to onReady, it was very annoying here...

            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) { }
            @Override
            public void onProviderDisabled(String provider) { }
        };
    }

    /**
     * Map stuff
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        //Create a marker click listener to display content peek at bottom of screen.
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String name = (String) marker.getTag();
                edu.temple.basic.dao.Location loc = mMockup.getLocation(name);

                //Moved to the InfoWindow onClickListener method
                /*if(loc != null){
                    Intent intent = new Intent(MainActivity.this, WikiViewerActivity.class);
                    intent.putExtra(WIKI_URL_EXTRA, loc.getPageURL());
                    startActivity(intent);
                    return true;
                }*/
                marker.showInfoWindow();
                return false;
            }
        });

        //load webview upon clicking the info window
        map.setOnInfoWindowClickListener(this);

        //TODO better style, current style removes too much info.
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(" map", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(" map", "Can't find style. Error: ", e);
        }

        //Add all the map locations
        //TODO make custom icons that display the name of the location next to it.
        if(mLocations != null){
            for(edu.temple.basic.dao.Location l : mLocations){

                MarkerOptions markerOptions = (new MarkerOptions())
                        .position(l.getLatLng())
                        .title(l.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                mMap.addMarker(markerOptions).setTag(l.getName());

                //The line below will display the name of one of the markers without clicking on it. I assume it is the last marker created. But clicking on the info window or another marker will crash the app.
                //mMap.addMarker(markerOptions).showInfoWindow();
            }
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newLatLngZoom(new LatLng(39.9809459, -75.152955), 14);
        mMap.moveCamera(cameraUpdate);
    }

    private List<edu.temple.basic.dao.Location> fetchMapLocations() {
        mMockup = MockupLocations.init();
        return mMockup.getAllLocations();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(ll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            registerForLocationUpdates();
        else
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 111); // make a constant reference
    }

    @SuppressLint("MissingPermission") //eww
    private void registerForLocationUpdates() {
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
        lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, ll);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            registerForLocationUpdates();
        else
            Toast.makeText(this, "No map permission", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        String name = (String) marker.getTag();
        edu.temple.basic.dao.Location loc = mMockup.getLocation(name);
        if(loc != null){
            Intent intent = new Intent(MainActivity.this, WikiViewerActivity.class);
            intent.putExtra(WIKI_URL_EXTRA, loc.getPageURL());
            startActivity(intent);
            //return true;
        }

    }
}
