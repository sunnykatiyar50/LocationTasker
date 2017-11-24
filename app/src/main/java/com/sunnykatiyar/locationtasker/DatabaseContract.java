package com.sunnykatiyar.locationtasker;

import android.provider.BaseColumns;

/**
 * Created by Sunny Katiyar on 11-11-2017.
 */

public final class DatabaseContract {

    private DatabaseContract(){
    }

    public static class Database_table implements BaseColumns {
        public static final String TABLE_NAME = "Tasks_Entries ";

        public static final String COLUMN_PLACENAME = "Place_Name";
        public static final String COLUMN_LATITUDE = "Latitude";
        public static final String COLUMN_LONGITUDE = "Longitude";
        public static final String COLUMN_TASKSTATUS = "Task_Status";
        public static final String COLUMN_TONE = "AlarmTone" ;
        public static final String COLUMN_REPEAT =  "Task_Repeat" ;
        public static final String COLUMN_LABEL = "Task_Label" ;
    }
}
