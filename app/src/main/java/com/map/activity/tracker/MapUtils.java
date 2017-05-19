package com.map.activity.tracker;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

//Utils class for setter and getter methods.
class MapUtils {
    private static MapSqliteHelper mapSqliteHelper;
    private static MapUtils mapUtils;
    private boolean isTrackingOn = false;
    private boolean isActivityPaused = false;
    private Location location, startTrackLoc, endTrackLoc;
    private String routeName;
    private JSONObject jsonObject;
    private MapService mapService;
    private String startTime;
    private String endTime;

    static MapUtils getInstance() {
        if (mapUtils == null) {
            mapUtils = new MapUtils();
        }
        return mapUtils;
    }

    //initialize database to create sqlite database.
    void initializeDB(Context con) {
        mapSqliteHelper = new MapSqliteHelper(con);
    }

    boolean getTrackingOn() {
        return isTrackingOn;
    }

    void setTrackingOn(boolean value) {
        isTrackingOn = value;
    }

    //Convert cursor values to arralist of listdata
    ArrayList<ListData> getAllRouteList() {
        Cursor cursor = mapSqliteHelper.getAllRouteNames();
        ArrayList<ListData> list = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ListData data = new ListData(cursor.getString(cursor.getColumnIndex(MapSqliteHelper.COLUMN_ROUTENAME)),
                        cursor.getString(cursor.getColumnIndex(MapSqliteHelper.COLUMN_STARTTIME)),
                        cursor.getString(cursor.getColumnIndex(MapSqliteHelper.COLUMN_ENDTIME)));
                list.add(data);
                cursor.moveToNext();
            }
            return list;
        }

        return null;
    }

    boolean isStartEndLocationSame() {
        return (getStartTrackLoc() == null) && (getEndTrackLoc() == null) || (getStartTrackLoc().getLatitude() == getEndTrackLoc().getLatitude()) && getStartTrackLoc().getLongitude() == getEndTrackLoc().getLongitude();
    }

    void setCurrentLocation(Location l) {
        location = l;
    }

    Location getLocation() {
        return location;
    }

    void setStartTrackLocation(Location l) {
        startTrackLoc = l;
    }

    void setEndTrackLocation(Location endLoc) {
        endTrackLoc = endLoc;
    }

    private Location getEndTrackLoc() {
        return endTrackLoc;
    }

    private Location getStartTrackLoc() {
        return startTrackLoc;
    }

    //to set start time of tracking
    void setStartTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK);
        Calendar cal = Calendar.getInstance();
        startTime = dateFormat.format(cal.getTime());
    }

    private String getStartTime() {
        return startTime;
    }

    //to set end time of tracking
    void setEndTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK);
        Calendar cal = Calendar.getInstance();
        endTime = dateFormat.format(cal.getTime());
    }

    private String getEndTime() {
        return endTime;
    }

    boolean isActivityPaused() {
        return isActivityPaused;
    }

    void setActivityPaused(boolean value) {
        isActivityPaused = value;
    }

    void setRoutename(String s) {
        routeName = s;
    }

    private String getRouteName() {
        return routeName;
    }

    //To create contentvalues object to store values into database.
    void insertValuestoDB() {
        Log.i("Check Log", "map utils insert");
        ContentValues values = new ContentValues();
        values.put(MapSqliteHelper.COLUMN_ROUTENAME, getRouteName());
        values.put(MapSqliteHelper.COLUMN_PATHVALUES, getPathValuesString());
        values.put(MapSqliteHelper.COLUMN_STARTTIME, getStartTime());
        values.put(MapSqliteHelper.COLUMN_ENDTIME, getEndTime());
        long l = mapSqliteHelper.insert(values);
        Log.i("Check Log", "map utils insert" + l);
        setStartTrackLocation(null);
        setEndTrackLocation(null);
    }

    //convert route values cursor to arraylist of latlng
    ArrayList<LatLng> getRouteValues(String s) {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        String jsonString;
        Cursor cursor = mapSqliteHelper.getRouteVales(s);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            jsonString = cursor.getString(cursor.getColumnIndex(MapSqliteHelper.COLUMN_PATHVALUES));

            try {
                JSONObject json = new JSONObject(jsonString);
                JSONArray jsonArray = json.getJSONArray("pathvalues");
                for (int i = 0; i < jsonArray.length(); i++) {
                    String latLngStr = jsonArray.getString(i);
                    latLngs.add(stringToLatlng(latLngStr));

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return latLngs;
    }

    //convert array of latlng values to jsonobject.
    void savePathValuesToJson(ArrayList<LatLng> points) throws JSONException {
        jsonObject = new JSONObject();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            list.add(latlngToString(points.get(i)));
        }
        jsonObject.put("pathvalues", new JSONArray(list));
    }

    //convert jsonobject to string.
    private String getPathValuesString() {
        String s = jsonObject.toString();
        jsonObject = null;
        return s;
    }

    //convert latlng to string
    private String latlngToString(LatLng latLng) {
        return (latLng.latitude + "/" + latLng.longitude);
    }

    //convert string to latlng
    private LatLng stringToLatlng(String s) {
        String[] str = s.split("/");
        double latitude = Double.parseDouble(str[0]);
        double longitude = Double.parseDouble(str[1]);
        return new LatLng(latitude, longitude);
    }

    //requesting manifest permissons
    void askForPermission(MapsActivity context) {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(context, permissions, 1);
    }

    MapService getService() {
        return mapService;
    }

    void setService(MapService service) {
        mapService = service;
    }
}
