package edu.temple.basic;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.temple.basic.dao.Page;
import edu.temple.basic.dao.mockup.MockupLocations;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Map + User Location
    private GoogleMap mMap;
    private LatLng currentLoc;
    boolean mLocationPermissionGranted;
    FusedLocationProviderClient mFusedLocationProviderClient;

    // Get Locations
    ArrayList<edu.temple.basic.dao.Location> mLocations = new ArrayList<>();
    ArrayList<edu.temple.basic.dao.Location> locations = new ArrayList<>();
    private MockupLocations mMockup;

    // Location Detail
    LinearLayout llBottomSheet;
    BottomSheetBehavior bottomSheetBehavior;
    public String title = "";
    public static final String WIKI_URL_EXTRA = "edu.temple.basic.WIKI_URL_EXTRA";

    // Add a location
    private FloatingActionButton fab;
    String value;
    int resID;
    boolean manual;
    boolean mBounded;
    LocationsFetchService mFetchService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mFetchService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            LocationsFetchService.LocalBinder mLocalBinder = (LocationsFetchService.LocalBinder)
                    service;

            mFetchService = mLocalBinder.getService();
            //fetch right away so we don't have old data
            mFetchService.fetchLocations();

        }
    };

    // recieve json and save it in prefs and then update the backend stuff!
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BROADCAST", "Received");
            JSONArray getArray = mFetchService.getLocationJson();
            //parse through json array
            try {
                for (int i = 0; i < getArray.length(); i++) {
                    JSONObject object = getArray.getJSONObject(i);
                    //id, name, lat, lng, creatorId, url
                    edu.temple.basic.dao.Location loc = new edu.temple.basic.dao.Location(object.getString("name"),
                            new Page(object.getString("url")),
                            new LatLng(object.getDouble("latitude"),
                                    object.getDouble("longitude")),
                            Integer.toString(object.getInt("creatorId")));

                    Log.d("gettrack", "name: " + loc.getName() + ", lat: " + loc.getLatLng().latitude + ", lng: " + loc.getLatLng().longitude + ", url: " + loc.getPageURL());
                    mLocations.add(loc);
                }
                addAllLocations();
            } catch (JSONException e) {
                Log.e("json error", "broadcast reciever()", e);
                return;
            }
            //all locations processed.
        }
    };

    // Housekeeping
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mLocations = new ArrayList<>(fetchMapLocations());

        Intent bindIntent = new Intent(this, LocationsFetchService.class);
        bindService(bindIntent, mConnection, BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
                new IntentFilter("fetched_markers"));

        // get the bottom sheet view
        llBottomSheet = findViewById(R.id.bottom_sheet);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SupportMapFragment mapFragment = new SupportMapFragment();
        transaction.replace(R.id.mapView, mapFragment).commit();
        mapFragment.getMapAsync(MainActivity.this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fab = findViewById(R.id.fab1);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {addLocation();}

        });

        activity = this;
    }

    /**
     * Map + User Location
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mLocationPermissionGranted = true;
        } else {
            // Show rationale and request permission
            mLocationPermissionGranted = false;
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(perms, 111);
        }

        try{
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
                            }
                        });
            }
        } catch (Exception e) {
            Log.e("location", "issue getting location");
        }

        try { // Customize base map styling
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));
            if (!success) {
                Log.e(" map", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(" map", "Can't find style. Error: ", e);
        }

        addAllLocations();

        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newLatLngZoom(new LatLng(39.9809459, -75.152955), 15);
        mMap.moveCamera(cameraUpdate);
    }

    private void addAllLocations() {
        if(mMap != null && mLocations != null){
            for(edu.temple.basic.dao.Location l : mLocations){
                Log.d("gettrack", "found: " + l.getName() + " lat: " + l.getLatLng().latitude + " lng: " + l.getLatLng().longitude + " at: " + l.getPageURL());

                MarkerOptions markerOptions = (new MarkerOptions())
                        .position(l.getLatLng())
                        .title(l.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                mMap.addMarker(markerOptions).setTag(l.getName());
            }
        }
        else
            Log.d("gettrack", "locations are null");
    }

    /**
     * Get Locations
     */
    private List<edu.temple.basic.dao.Location> fetchMapLocations() {
        mMockup = MockupLocations.init();
        return mMockup.getAllLocations();
    }

    /**
     * Location detail
     */
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
     * Add a location
     */
    // TODO add location to storage
    private void addLocation() {
        // alert dialog for choosing to use current location or manually placing
        AlertDialog.Builder alertChoose= new AlertDialog.Builder(this);
        alertChoose.setTitle("Place where?");
        alertChoose.setMessage("Use current location or manually place?");

        // alert dialog
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Location Name");
        alert.setMessage("Please enter the new location name");
        final EditText input = new EditText(this);
        alert.setView(input);

        alertChoose.setPositiveButton("Current Location?", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton) {
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideKeyboard(activity);
                        value=input.getText().toString();
                        Point point = new Point();
                        point.x = 1;
                        point.y = 0;
                        manual=false;
                        showIconPopup(MainActivity.this, point);
                        //addMarker(resID, false);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled
                    }
                });

                alert.show();
            }
        });

        alertChoose.setNegativeButton("Manually Place?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        hideKeyboard(activity);
                        value = input.getText().toString();

                        Point point = new Point();
                        point.x = 1;
                        point.y = 0;
                        manual=true;
                        showIconPopup(MainActivity.this, point);
                        //addMarker(resID, true);

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled
                    }
                });

                alert.show();
            }

        });
        alertChoose.show();
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {

        //Toast.makeText(getApplication(),"in get marker", Toast.LENGTH_LONG).show();

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

    private void showIconPopup(final Activity context, Point p) {
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
                resID=getResources().getIdentifier("building1", "drawable", getPackageName());
                addMarker(resID, manual);
                chooseIcon.dismiss();
            }
        });

        build2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("building2", "drawable", getPackageName());
                addMarker(resID, manual);
                chooseIcon.dismiss();
            }
        });

        car1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("car1", "drawable", getPackageName());
                addMarker(resID, manual);
                chooseIcon.dismiss();
            }
        });

        car2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("car2", "drawable", getPackageName());
                addMarker(resID, manual);
                chooseIcon.dismiss();
            }
        });

        foodTruck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("foodtruck1", "drawable", getPackageName());
                addMarker(resID, manual);
                chooseIcon.dismiss();
            }
        });

        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("train", "drawable", getPackageName());
                addMarker(resID, manual);
                chooseIcon.dismiss();
            }
        });

        house.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("house", "drawable", getPackageName());
                addMarker(resID, manual);
                chooseIcon.dismiss();
            }
        });

        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resID=getResources().getIdentifier("questionmark", "drawable", getPackageName());
                addMarker(resID, manual);
                chooseIcon.dismiss();
            }
        });
    }

    public void addMarker(int rID, boolean manual){

        if(!manual){
            mMap.addMarker(new MarkerOptions().position(currentLoc).title(value)
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(resID)))
                    .snippet(value));
        }
        else{
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent=new Intent(this, Login.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Housekeeping
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }
        else {
            mLocationPermissionGranted = false;
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111); // make a constant reference
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 111) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                mMap.setMyLocationEnabled(true);
            } else {
                mLocationPermissionGranted = false;
                Toast.makeText(this, "No map permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
