package edu.temple.basic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private LocationManager lm;
    private LocationListener ll;
    private GoogleMap mMap;
    private Marker lastMarker;
    private FloatingActionButton fab;

    // please work

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                CameraUpdate cameraUpdate = CameraUpdateFactory
                        .newLatLngZoom(lastMarker.getPosition(), 14);

                mMap.moveCamera(cameraUpdate);
                Log.e( "marktrack", "tried to do camera stuff");

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
}
