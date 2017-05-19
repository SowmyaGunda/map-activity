package com.map.activity.tracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//sqlitehelper class to create and save data into sqlite database
class MapSqliteHelper extends SQLiteOpenHelper {
    static final String COLUMN_PATHVALUES = "pathvalues";
    static final String COLUMN_ROUTENAME = "routename";
    static final String COLUMN_STARTTIME = "starttime";
    static final String COLUMN_ENDTIME = "endtime";
    private static final String DATABASE_NAME = "maps.db";
    private static final String TABLE_NAME = "maps_table";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID = "id";
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "("
            + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_ROUTENAME + " text not null, "
            + COLUMN_STARTTIME + " text not null, " + COLUMN_ENDTIME + " text not null, "
            + COLUMN_PATHVALUES + " text not null);";


    MapSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    //insert values to database
    long insert(ContentValues values) {
        Log.i("Check Log", "sql helper insert");
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_NAME, null, values);
    }

    //to get all routenames, start time and end time to display on list.
    Cursor getAllRouteNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, new String[]{COLUMN_ROUTENAME, COLUMN_STARTTIME, COLUMN_ENDTIME}, null, null, null, null, null);
    }

    //to get route values for respected route.
    Cursor getRouteVales(String route) {
        SQLiteDatabase db = this.getReadableDatabase();
        String WHERE = COLUMN_ROUTENAME + " =? ";
        return db.query(TABLE_NAME, new String[]{COLUMN_PATHVALUES}, WHERE,
                new String[]{route}, null, null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
