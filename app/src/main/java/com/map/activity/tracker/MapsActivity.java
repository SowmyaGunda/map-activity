package com.map.activity.tracker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;

import java.util.ArrayList;

import static com.map.activity.tracker.R.id.map;

/*MapsActivity to display map view*/
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String MOVE_CAMERA = "com.map.activity.tracker.MapsActivity.movecamera";
    private static final int UPDATE_MOVE_CAMERA = 1;
    private static ArrayList<LatLng> routePoints = new ArrayList<>();
    private GoogleMap mMap;
    private MapService mapService;
    private Polyline line;
    private Switch mSwitch;
    private MapUtils mapUtils;
    private ArrayList<LatLng> bundlelatLng;
    //handler to handle location changed requests sequentially.
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_MOVE_CAMERA) {
                moveMapCursor(new LatLng(mapUtils.getLocation().getLatitude(), mapUtils.getLocation().getLongitude()));
            }
        }
    };
    //Receiver to receive move camers intents when location changed and it will pass messages to handlee.
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MOVE_CAMERA)) {
                if (!mapUtils.isActivityPaused()) {
                    mHandler.sendEmptyMessage(UPDATE_MOVE_CAMERA);
                } else {
                    routePoints.add(new LatLng(mapUtils.getLocation().getLatitude(), mapUtils.getLocation().getLongitude()));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapUtils = MapUtils.getInstance();
        mapService = mapUtils.getService();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        //switch to on or off tracking
        mSwitch = (Switch) findViewById(R.id.mySwitch);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mapUtils.setTrackingOn(true);
                    mapUtils.setStartTrackLocation(mapUtils.getLocation());
                    mapUtils.setStartTime();

                } else {
                    mapUtils.setTrackingOn(false);
                    mapUtils.setEndTrackLocation(mapUtils.getLocation());
                    mapUtils.setEndTime();
                    if (!mapUtils.isStartEndLocationSame()) {
                        displayDialog(false);
                    } else {
                        displayDialog(true);
                    }
                }
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(MOVE_CAMERA);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapUtils.setActivityPaused(false);
        if (mapUtils.getTrackingOn() && mMap != null) {
            moveMapCursor(new LatLng(mapUtils.getLocation().getLatitude(), mapUtils.getLocation().getLongitude()));
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //to display route when respected saved route is selected from saved list.
        if (intent.hasExtra("extras") && mMap != null) {
            bundlelatLng = new ArrayList<>();
            Bundle bundle = intent.getBundleExtra("extras");
            bundlelatLng = bundle.getParcelableArrayList("routevalues");
            if (bundlelatLng != null) {
                try {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bundlelatLng.get(0), 17.0f));
                    for (int i = 0; i < bundlelatLng.size(); i++) {
                        routePoints.add(bundlelatLng.get(i));
                    }
                    PolylineOptions pOptions = new PolylineOptions()
                            .width(5)
                            .color(Color.BLUE)
                            .geodesic(true);
                    for (int z = 0; z < routePoints.size(); z++) {
                        LatLng point = routePoints.get(z);
                        pOptions.add(point);
                    }
                    line = mMap.addPolyline(pOptions);
                } catch (Exception e) {
                    MapsInitializer.initialize(MapsActivity.this);
                    mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bundlelatLng.get(0), 17.0f));
                        }
                    });

                }
            }

        }
    }

    /*to display dialog when tracking stopped
    * if start and end location are same it will display a dialog with message
    * otherwise it will display another dialog with ok, cancel buttons to save data
    * into database and it will save data into database*/
    private void displayDialog(boolean isTrue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        if (isTrue) {
            builder.setTitle("Both start and end locations are same");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mapUtils.setStartTrackLocation(null);
                    mapUtils.setEndTrackLocation(null);
                    reloadMap();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            builder.setTitle("Give some name to save this route");
            final EditText input = new EditText(MapsActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mapUtils.setRoutename(input.getText().toString());
                    try {
                        mapUtils.savePathValuesToJson(routePoints);
                        routePoints.clear();
                        reloadMap();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mapUtils.insertValuestoDB();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mapUtils.setStartTrackLocation(null);
                    mapUtils.setEndTrackLocation(null);
                    routePoints.clear();
                    reloadMap();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (TextUtils.isEmpty(s)) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }

                }
            });
        }
    }

    @Override
    protected void onPause() {
        mapUtils.setActivityPaused(true);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    /**
 * Manipulates the map once available.
 * This callback is triggered when the map is ready to be used.
 * This is where we can add markers or lines, add listeners or move the camera. In this case,
 * we just add a marker near Sydney, Australia.
 * If Google Play services is not installed on the device, the user will be prompted to install
 * it inside the SupportMapFragment. This method will only be triggered once the user has
 * installed Google Play services and returned to the app.
 */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mapUtils.askForPermission(this);
        }

        mMap.setMyLocationEnabled(true);
        try {
            if (bundlelatLng == null) {
                Location location = null;
                if (mapService.canGetLocation) {
                    location = mapService.getLocation();
                }
                final LatLng latLng;
                if (location != null) {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
                }
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bundlelatLng.get(0), 17.0f));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bundlelatLng.get(0), 17.0f));
                for (int i = 0; i < bundlelatLng.size(); i++) {
                    routePoints.add(bundlelatLng.get(i));
                }
                PolylineOptions pOptions = new PolylineOptions()
                        .width(5)
                        .color(Color.BLUE)
                        .geodesic(true);
                for (int z = 0; z < routePoints.size(); z++) {
                    LatLng point = routePoints.get(z);
                    pOptions.add(point);
                }
                line = mMap.addPolyline(pOptions);
            }
        } catch (Exception e) {
            MapsInitializer.initialize(MapsActivity.this);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bundlelatLng.get(0), 17.0f));
                }
            });

        }

    }

    //to refresh map and it will display current location
    private void reloadMap() {
        Location location = null;
        if(mapService.canGetLocation) {
            location = mapService.getLocation();
            Log.i("Check Log","can get location");

        }
        if (mapUtils.getTrackingOn()) {
            mSwitch.setChecked(false);
            mapUtils.setTrackingOn(false);
        }
        if (routePoints.size() > 0) {
            routePoints.clear();
        }
        if (line != null) {
            line.remove();
        }
        final LatLng latLng;
        if(location !=null) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
        }
    }

    //to move cursor when on location changed and if tracking is on it will draw the line.
    private void moveMapCursor(LatLng latLng) {
        if (mMap == null) {
            return;
        }
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17.0f));
        if (mapUtils.getTrackingOn()) {
            routePoints.add(latLng);
            PolylineOptions pOptions = new PolylineOptions()
                    .width(5)
                    .color(Color.BLUE)
                    .geodesic(true);
            for (int z = 0; z < routePoints.size(); z++) {
                LatLng point = routePoints.get(z);
                pOptions.add(point);
            }
            line = mMap.addPolyline(pOptions);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Menu with options saved routes and reload options.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                Intent intent = new Intent(MapsActivity.this, MapListActivity.class);
                startActivity(intent);
                break;
            case R.id.action_reload:
                reloadMap();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
