package com.sunnykatiyar.locationtasker;

import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Sunny Katiyar on 12-11-2017.
 */

public final class SingleTaskObject extends Object {

    private long id ;
    private String place_name;
    private String label_name;
    private int Repeat;
    private int task_status;
    private String AlarmTone ;
    private double latitude;
    private double longitude;

    public SingleTaskObject(long id,String place_name,String label_name,
                            String AlarmTone,double latitude,double longitude,int repeat,int task_status)
    {
        this.id =id;
        this.place_name=place_name;
        this.label_name=label_name;
        this.AlarmTone=AlarmTone;
        this.latitude=latitude;
        this.longitude=longitude;
        this.task_status=task_status;
        this.Repeat=repeat;
    }


    public String getPlace_name(){
        return this.place_name;
    }

    public String getAlarmTone(){
        return this.AlarmTone;
    }

    public String getLabel_name(){
        return this.label_name;
    }

    public double getLatitude(){
        return this.latitude;
    }
    public double getLongitude(){
        return this.longitude;
    }


    public long getId(){
        return id;
    }

    public int getRepeat(){
        return this.Repeat;
    }

    public int getTask_status(){
        return  this.task_status;
    }



}
