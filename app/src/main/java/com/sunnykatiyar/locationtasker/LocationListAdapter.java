package com.sunnykatiyar.locationtasker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import static com.sunnykatiyar.locationtasker.MyLocationIntentService.myCurrentlocation;

/**
 * Created by Sunny Katiyar on 10-11-2017.
 */

public class LocationListAdapter extends BaseAdapter {

    List<SingleTaskObject> taskObjectsList;
    Activity context;
    public SQLiteHelper mySQLiteHelper;
    public SQLiteDatabase mySQLiteDatabase;
    public Cursor myCursor;

    public LocationListAdapter(Activity context, List<SingleTaskObject> taskObjectsList, SQLiteDatabase sd,Cursor c,SQLiteHelper sh) {
        this.context=context;
        this.taskObjectsList=taskObjectsList;
        this.mySQLiteDatabase=sd;
        this.myCursor=c;
        this.mySQLiteHelper=sh;
     //  Log.e("In LocationListAdapter:", "inside constructor :after "+ taskObjectsList.size() );
    }

    public class ViewHolder{
        TextView place_name;
        TextView latlong;
        TextView task_switch_text;
        TextView distance_txt;
        EditText label ;
        ImageButton delete_btn;
        ImageButton refresh_btn;
        Switch task_switch;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder vHolder;

     //   Log.e("In LocationListAdapter:", "inside getview : " );
        final SingleTaskObject targetObject = taskObjectsList.get(position);

        if (view == null) {
            view = context.getLayoutInflater().inflate(R.layout.location_listitem, null);
            vHolder = new ViewHolder();
            vHolder.place_name =  view.findViewById(R.id.location_name);
            vHolder.label = view.findViewById(R.id.label_edittext);
            vHolder.distance_txt = view.findViewById(R.id.distance);
            vHolder.refresh_btn = view.findViewById(R.id.refresh_btn);
            vHolder.task_switch = view.findViewById(R.id.task_switch);
        //  vHolder.latlong = (TextView) view.findViewById(R.id.text_latlong);
          //  Log.e("In LocationListAdapter:", "inside getview : in if : after viewholder etting " );
            view.setTag(vHolder);
            }
    else {
            vHolder = (ViewHolder) view.getTag();
         // Log.e("In LocationListAdapter:", "inside getview : in else : after viewholder etting " );
        }

        //vHolder.latlong.setText(targetObject.getLatitude()+" , "+targetObject.getLongitude());
            vHolder.place_name.setText(targetObject.getPlace_name());
            vHolder.label.setText(targetObject.getLabel_name());
            setSwitch(vHolder.task_switch,targetObject.getTask_status());

            final long task_id1 = targetObject.getId();
            vHolder.delete_btn=view.findViewById(R.id.delete_imgbtn);

            vHolder.delete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                delete_task(task_id1);
                }
            });

        vHolder.task_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int s_value;
                    Log.e("in onItemClick :", "taskSwitch clicked");
                    if (vHolder.task_switch.isChecked())
                        s_value = 1;
                    else s_value = 0;
                    updateInt(task_id1, DatabaseContract.Database_table.TABLE_NAME,
                            DatabaseContract.Database_table.COLUMN_TASKSTATUS, s_value);
                }
            });

        vHolder.label.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    String s= vHolder.label.getText().toString();
                    if(s.equals(""))
                    {
                        vHolder.label.setText(R.string.default_tasklabel);
                    }else vHolder.label.setText(vHolder.label.getText().toString());

                    updateString(targetObject.getId(), DatabaseContract.Database_table.TABLE_NAME,
                            DatabaseContract.Database_table.COLUMN_LABEL, vHolder.label.getText().toString());
                    }
              }
        });
 /*
        vHolder.label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vHolder.label.getText().toString().equals(R.string.default_tasklabel)){
                    vHolder.label.setText("");
                }

                    updateString(targetObject.getId(), DatabaseContract.Database_table.TABLE_NAME,
                            DatabaseContract.Database_table.COLUMN_LABEL, vHolder.label.getText().toString());
                }

        });
*/
        vHolder.place_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", targetObject.getLatitude(), targetObject.getLongitude());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                context.startActivity(intent);
            }
        });

        vHolder.refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location dst_location = new Location("");
                dst_location.setLongitude(targetObject.getLongitude());
                dst_location.setLatitude(targetObject.getLatitude());
                float f_distance  ;
                String s;
                if(myCurrentlocation!=null){
                    f_distance = myCurrentlocation.distanceTo(dst_location);
                    s=String.format("%.03f",f_distance/1000);
                    vHolder.distance_txt.setText("You are "+s+" kms away");
                }
                else{
                    vHolder.distance_txt.setText("Unable to determine current location");
                }
            }
        });
            return view;

    }


    public void updateString(long id, String table_name, String column_name, String newValue) {
        mySQLiteDatabase = mySQLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(column_name, newValue);
        mySQLiteDatabase.update(table_name, values, "_id = " + id, null);
      //  context.finish();
      //  Intent i = context.getIntent();
     //   context.startActivity(i);
        Log.e("data base Methods", "update_task");
    }

    public void updateInt(long id, String table_name, String column_name, int newValue) {
        mySQLiteDatabase = mySQLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(column_name, newValue);
        mySQLiteDatabase.update(table_name, values, "_id = " + id, null);
        context.finish();
        Intent i = context.getIntent();
        context.startActivity(i);
        Log.e("database Methods", "update_task");
    }

    public void delete_task(long task_id) {
        int i_before = myCursor.getCount();
        mySQLiteDatabase = mySQLiteHelper.getWritableDatabase();
        mySQLiteDatabase.delete(DatabaseContract.Database_table.TABLE_NAME, "_id = " + task_id, null);
        Log.e("delete_task_method :", "deletion attempted ");
        context.finish();
        context.startActivity(context.getIntent());
        int i_after = myCursor.getCount();
        if (i_before > i_after) {
            Toast.makeText(context, (i_before - i_after) + " task deleted", Toast.LENGTH_SHORT);
        } else
            Toast.makeText(context, "No task deleted", Toast.LENGTH_SHORT);
    }

    @Override
    public int getCount() {
     //   Log.e("In LocationListAdapter:", "inside getcount ");
        return taskObjectsList.size();
    }

    public void setSwitch(Switch s,int b){
   //     Log.e("In LocationListAdapter:", "inside setswitch : before if " );
        if (b==0) s.setChecked(false);
            else  s.setChecked(true);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}
