package com.sunnykatiyar.locationtasker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import static com.sunnykatiyar.locationtasker.DatabaseContract.Database_table;
import static com.sunnykatiyar.locationtasker.DatabaseContract.Database_table.TABLE_NAME;
import static com.sunnykatiyar.locationtasker.MainActivity.context;

/**
 * Created by Sunny Katiyar on 11-11-2017.
 */

public class SQLiteHelper extends SQLiteOpenHelper {

    public final String CREATE_TABLE_COMMAND = "CREATE TABLE "+ TABLE_NAME+"("+ Database_table._ID+" INTEGER PRIMARY KEY,"+Database_table.COLUMN_PLACENAME+
            " TEXT,"+Database_table.COLUMN_LATITUDE+" DOUBLE,"+Database_table.COLUMN_LONGITUDE+" DOUBLE,"+Database_table.COLUMN_LABEL+
            " TEXT,"+Database_table.COLUMN_TASKSTATUS+" INTEGER,"+Database_table.COLUMN_TONE+" TEXT,"+Database_table.COLUMN_REPEAT+" INTEGER)";

    public final String DELETE_TABLE_COMMAND = "DROP TABLE IF EXISTS "+TABLE_NAME;

    public static final String DATABASE_NAME = "TasksDetails.db";
    public static final int DATABASE_VERSION = 2;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_COMMAND);
        Toast.makeText(context,"after create database",Toast.LENGTH_SHORT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_TABLE_COMMAND);
        onCreate(db);
    }
}
