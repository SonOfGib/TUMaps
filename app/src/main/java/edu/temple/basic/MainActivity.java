package edu.temple.basic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import edu.temple.basic.dao.mockup.MockupLocations;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {


    private LocationManager lm;
    private LocationListener ll;
    private GoogleMap mMap;
    private Marker lastMarker;
    private FloatingActionButton fab;

    private ArrayList<edu.temple.basic.dao.Location> mLocations;
    private MockupLocations mMockup;

    LinearLayout llBottomSheet;
    BottomSheetBehavior bottomSheetBehavior;

    public static final String WIKI_URL_EXTRA = "edu.temple.basic.WIKI_URL_EXTRA";

    public String title = "";
    String value;
    int resID;
    String test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocations = new ArrayList<>(fetchMapLocations());

        // get the bottom sheet view
        llBottomSheet = findViewById(R.id.bottom_sheet);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SupportMapFragment mapFragment = new SupportMapFragment();
        transaction.replace(R.id.mapView, mapFragment).commit();
        mapFragment.getMapAsync(MainActivity.this);

        lm = getSystemService(LocationManager.class);
        ll = makeLocationListener();

        fab = findViewById(R.id.fab1);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {addLocation();}

        });

    }

    /*
     * Adds a location to the map where the user pressed it.
     * TODO Prompt user for name of location.
     * TODO Add location to storage
     */
    private void addLocation() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Location Name");
        alert.setMessage("Please enter the new location name");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //final String value = input.getText().toString();
                value = input.getText().toString();
                //Toast.makeText(getParent(), "tap new location", Toast.LENGTH_SHORT).show();
                /*mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {

                        mMap.addMarker(new MarkerOptions().position(point).title(value)
                                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.building1)))
                                .snippet("Tut"));
                        mMap.setOnMapClickListener(null);
                    }
                });*/

                Point point = new Point();
                point.x = 1;
                point.y = 0;
                showStatusPopup(MainActivity.this, point);

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
      
        alert.show();
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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(this);


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
                .newLatLngZoom(new LatLng(39.9809459, -75.152955), 15);
        mMap.moveCamera(cameraUpdate);
    }

    @Override // Create a marker click listener to display content peek at bottom of screen.
    public boolean onMarkerClick(final Marker marker) {
        //get the map container height
        FrameLayout mapContainer = findViewById(R.id.mapView);

        Projection projection = mMap.getProjection();

        LatLng markerLatLng = new LatLng(marker.getPosition().latitude,
                marker.getPosition().longitude);
        Point markerScreenPosition = projection.toScreenLocation(markerLatLng);
        Point pointHalfScreenAbove = new Point(markerScreenPosition.x,
                markerScreenPosition.y + (mapContainer.getHeight() / 4));

        LatLng aboveMarkerLatLng = projection
                .fromScreenLocation(pointHalfScreenAbove);

        marker.showInfoWindow();
        CameraUpdate center = CameraUpdateFactory.newLatLng(aboveMarkerLatLng);
        mMap.animateCamera(center);

        expandBottomSheet(marker);

        return true;
    }

    /**
     * Bottom Sheet
     */
    private void expandBottomSheet(Marker marker) {
        title = (String) marker.getTag();
        String lastUp = "Last Update: 2019";
        String creator = "Creator: Will";

        ((TextView) findViewById(R.id.titleTextView)).setText(title);
        ((TextView) findViewById(R.id.lastUpTextView)).setText(lastUp);
        ((TextView) findViewById(R.id.creatorTextView)).setText(creator);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        (findViewById(R.id.wikiButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edu.temple.basic.dao.Location loc = mMockup.getLocation(title);
                if(loc != null){
                    Intent intent = new Intent(MainActivity.this, WikiViewerActivity.class);
                    intent.putExtra(WIKI_URL_EXTRA, loc.getPageURL());
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Housekeeping
     */
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

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {

        Paint color=new Paint();
        color.setTextSize(40);
        color.setColor(Color.WHITE);

        //View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.layout(0, 0, 48, 48);
        customMarkerView.buildDrawingCache();
        //Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
        Bitmap returnedBitmap = Bitmap.createBitmap(500, 192,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        canvas.drawText(value, 200, 80, color);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private void showStatusPopup(final Activity context, Point p) {

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.pickIconPopup);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.icon_selection_window, null);

        // Creating the PopupWindow
        final PopupWindow chooseIcon = new PopupWindow(context);
        chooseIcon.setContentView(layout);
        chooseIcon.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        chooseIcon.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        chooseIcon.setFocusable(true);

        // Some offset to align the popup a bit to the left, and a bit down, relative to button's position.
        int OFFSET_X = -20;
        int OFFSET_Y = 50;

        //Clear the default translucent background
        chooseIcon.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        chooseIcon.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);

        ImageView build1=layout.findViewById(R.id.building1);
        ImageView build2=layout.findViewById(R.id.building2);
        ImageView car1=layout.findViewById(R.id.car1);
        ImageView car2=layout.findViewById(R.id.car2);
        ImageView foodTruck=layout.findViewById(R.id.foodTruck);
        ImageView train=layout.findViewById(R.id.train);
        ImageView house=layout.findViewById(R.id.house);
        ImageView question=layout.findViewById(R.id.questionMark);

        build1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //resID=getResources().getIdentifier("building1.png", "drawable", "edu.temple.basic");
                resID=getResources().getIdentifier("building1", "drawable", getPackageName());
                chooseIcon.dismiss();

            }
        });

        build2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("building2", "drawable", getPackageName());
                chooseIcon.dismiss();
            }
        });

        car1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("car1", "drawable", getPackageName());
                chooseIcon.dismiss();
            }
        });

        car2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("car2", "drawable", getPackageName());
                chooseIcon.dismiss();
            }
        });

        foodTruck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("foodtruck1", "drawable", getPackageName());
                chooseIcon.dismiss();
            }
        });

        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("train", "drawable", getPackageName());
                chooseIcon.dismiss();
            }
        });

        house.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("house", "drawable", getPackageName());
                chooseIcon.dismiss();
            }
        });

        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("questionmark", "drawable", getPackageName());
                chooseIcon.dismiss();
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                mMap.addMarker(new MarkerOptions().position(point).title(value)
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(resID)))
                        .snippet(value));
                mMap.setOnMapClickListener(null);
            }
        });
    }


}
