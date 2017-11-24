package com.sunnykatiyar.locationtasker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Sunny Katiyar on 12-11-2017.
 */

public class MyCustomCursorAdapter extends CursorAdapter {

   Activity context;
   LayoutInflater inflater;

    public MyCustomCursorAdapter(Activity context, Cursor c, int flags) {
        super(context, c, flags);

        this.context=context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final SingleTaskObject targetObject = new SingleTaskObject(
                cursor.getLong(cursor.getColumnIndex(DatabaseContract.Database_table._ID)),
                cursor.getString(cursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_PLACENAME)),
                cursor.getString(cursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_LABEL)),
                cursor.getString(cursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_TONE)),
                cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_LONGITUDE)),
                cursor.getInt(cursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_REPEAT)),
                cursor.getInt(cursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_TASKSTATUS)));

        final TextView place_name = view.findViewById(R.id.location_name);
        final EditText label = view.findViewById(R.id.label_edittext);
        final ImageButton refresh_imgbtn = view.findViewById(R.id.refresh_btn);
        final ImageButton delete_imgbtn = view.findViewById(R.id.delete_imgbtn);
        final TextView distance_txt = view.findViewById(R.id.distance);
        final Switch task_switch = view.findViewById(R.id.task_switch);

        Location dest_location = new Location("");
        dest_location.setLatitude(targetObject.getLatitude());
        dest_location.setLongitude(targetObject.getLongitude());
        float f_distance;
        final String s_distance;
        if (MyLocationIntentService.myCurrentlocation != null) {
            f_distance = MyLocationIntentService.myCurrentlocation.distanceTo(dest_location);
            s_distance = String.format("%.03f",f_distance/1000);
        } else s_distance="Unable to determine current location";

            place_name.setText(targetObject.getPlace_name());
            label.setText(targetObject.getLabel_name());
        //  viewHolder.distance_txt.setText("You are "+s_distance+" kms away");

            if (targetObject.getTask_status() == 0) task_switch.setChecked(false);
            else task_switch.setChecked(true);

            refresh_imgbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        distance_txt.setText("You are "+s_distance+" kms away");
                }
            });

            label.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((label.getText().toString()).equals(R.string.default_tasklabel)) {
                        label.setText("");
                    }
                }
            });

            place_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f",targetObject.getLatitude(),targetObject.getLongitude());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    MainActivity.context.startActivity(intent);
                }
            });


            label.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String s_label = label.getText().toString();
                    if (!hasFocus)
                        updateString(targetObject.getId(), DatabaseContract.Database_table.TABLE_NAME, DatabaseContract.Database_table.COLUMN_LABEL,s_label);
                }
            });

            task_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task_switch.isChecked()) {
                        updateInt(targetObject.getId(), DatabaseContract.Database_table.TABLE_NAME, DatabaseContract.Database_table.COLUMN_TASKSTATUS, 1);
                    } else {
                        updateInt(targetObject.getId(), DatabaseContract.Database_table.TABLE_NAME, DatabaseContract.Database_table.COLUMN_TASKSTATUS, 0);
                    }
                }
            });

            delete_imgbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete_task(targetObject.getId());
                }
            });
        }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
         return inflater.inflate(R.layout.location_listitem,parent,false);
    }

    public void updateString(long id, String table_name, String column_name, String newValue) {
        MainActivity.myDatabase = MainActivity. sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(column_name, newValue);
        MainActivity.myDatabase.update(table_name, values, "_id = " + id, null);
        //context.finish();
        //Intent i = context.getIntent();
        //context.startActivity(i);
        Log.e("data base Methods", "update_task");
    }

    public void updateInt(long id, String table_name, String column_name, int newValue) {
        MainActivity.myDatabase = MainActivity.sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(column_name, newValue);
        MainActivity.myDatabase.update(table_name, values, "_id = " + id, null);
        context.finish();
        Intent i = context.getIntent();
        context.startActivity(i);
        Log.e("database Methods", "update_task");
    }

    public void delete_task(long task_id) {
        int i_before = MainActivity.myCursor.getCount();
        MainActivity.myDatabase = MainActivity.sqLiteHelper.getWritableDatabase();
        MainActivity.myDatabase.delete(DatabaseContract.Database_table.TABLE_NAME, "_id = " + task_id, null);
        Log.e("delete_task_method :", "deletion attempted ");
        context.finish();
        context.startActivity(context.getIntent());

        int i_after = MainActivity.myCursor.getCount();
        if (i_before > i_after) {
            Toast.makeText(context, (i_before - i_after) + " task deleted", Toast.LENGTH_SHORT);
        } else
            Toast.makeText(context, "No task deleted", Toast.LENGTH_SHORT);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    protected void onContentChanged() {
        super.onContentChanged();
    }

}
