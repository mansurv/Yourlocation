package com.netmontools.lookatnet.utils.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;

import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.Loader;
import androidx.loader.content.CursorLoader;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.netmontools.lookatnet.BuildConfig;
import com.netmontools.lookatnet.R;
import com.netmontools.lookatnet.utils.LogSystem;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
        //LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MapsActivity";
    public static final String SATELLITE = "com.netmontools.accesspoints.SATELLITE";
    public static final String LATITUDE = "com.netmontools.accesspoints.LATITUDE";
    public static final String LONGITUDE = "com.netmontools.accesspoints.LONGITUDE";
    public static final String BSSID = "com.netmontools.accesspoints.BSSID";
    public static final String SSID = "com.netmontools.accesspoints.SSID";
    public static final String ID = "com.netmontools.accesspoints.ID";

    private GoogleMap mGoogleMap;
    public static Cursor mapCursor;
    private boolean isSatelliteMode = false;
    SupportMapFragment mapFragment = null;

    public long argId = -1;
    public double argLatitude;
    public double argLongitude;
    public String argBssid;
    public String argSsid;
    private Loader<Cursor> loader;
    private Cursor cursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        isSatelliteMode = getIntent().getBooleanExtra(SATELLITE,false);

        argId = getIntent().getLongExtra(ID, -1);
        argLatitude = getIntent().getDoubleExtra(LATITUDE, 0);
        argLongitude = getIntent().getDoubleExtra(LONGITUDE, 0);
        argBssid = getIntent().getStringExtra(BSSID);
        argSsid = getIntent().getStringExtra(SSID);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (isSatelliteMode) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        if (argLatitude == 0) {
            //LoaderManager lm = getSupportLoaderManager();
            //lm.initLoader(0, null, this);
        } else {
            //mGoogleMap.setPadding(0,0,0,140);
            LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
            LatLng latLng = new LatLng(argLatitude, argLongitude);
            mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(argSsid).snippet(argBssid).draggable(true));
            latLngBuilder.include(latLng);
            Display display = this.getWindowManager().getDefaultDisplay();
            // construct a movement instruction for the map camera
            CameraUpdate movement = CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(),
                    display.getWidth(), display.getHeight(),12);
            mGoogleMap.moveCamera(movement);

            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker arg0) {
                }

                @SuppressWarnings("unchecked")
                @Override
                public void onMarkerDragEnd(Marker arg0) {
                    if (BuildConfig.USE_LOG) {
                        LogSystem.logInFile(TAG, " Location:\n  " + arg0.getPosition().longitude + ", " + arg0.getPosition().latitude);
                    }
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
                    //updateLocation(arg0.getPosition().longitude, arg0.getPosition().latitude);
                }

                @Override
                public void onMarkerDrag(Marker arg0) {
                }
            });
        }
    }

    private void updateUI() {
        if (mGoogleMap == null)
            return;

        // also create a LatLngBounds so we can zoom to fit
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        // iterate over the locations
        int latIndex = mapCursor.getColumnIndex("latitude");
        int longIndex = mapCursor.getColumnIndex("longitude");
        int ssidIndex = mapCursor.getColumnIndex("ssid");
        int bssidIndex = mapCursor.getColumnIndex("bssid");

        mapCursor.moveToFirst();
        while (!mapCursor.isAfterLast()) {
            argLatitude = mapCursor.getDouble(latIndex);
            argLongitude = mapCursor.getDouble(longIndex);
            if((argLatitude == 0L) || (argLongitude == 0L)) {
                if (mapCursor.getCount() == 1)
                    return;
                mapCursor.moveToNext();
            }
            argSsid = mapCursor.getString(ssidIndex);
            argBssid = mapCursor.getString(bssidIndex);
            LatLng latLng = new LatLng(argLatitude, argLongitude);

            MarkerOptions netMarkerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(argSsid)
                    .snippet(argBssid);
            mGoogleMap.addMarker(netMarkerOptions);

            latLngBuilder.include(latLng);
            mapCursor.moveToNext();
        }
        mapCursor.close();
        // make the map zoom to show the track, with some padding
        // use the size of the current display in pixels as a bounding box
        Display display = this.getWindowManager().getDefaultDisplay();
        // construct a movement instruction for the map camera
        CameraUpdate movement = CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(),
                display.getWidth(), display.getHeight(), 12);
        mGoogleMap.moveCamera(movement);
    }
}
