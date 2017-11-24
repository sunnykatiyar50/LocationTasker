package com.sunnykatiyar.locationtasker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.os.Build.VERSION_CODES.N;

/**
 * Created by Sunny Katiyar on 17-11-2017.
 */

public class MyLocationIntentService extends IntentService implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {
    /** * Creates an IntentService.  Invoked by your subclass's constructor.
         * @param name Used to name the worker thread, important only for debugging.   */

    Activity activity;
    Context context;
    public static RingtoneManager ring_mgr;
    public static MediaPlayer mediaPlayer;
    String s;

 // Location Api variables and constants
    protected String TAG = "IntentService Class :";
    protected static Location myLastlocation;
    public static Location myCurrentlocation = new Location("");
    protected static LocationCallback myLocationCallback;
    protected static GoogleApiClient myGoogleApiClient;
    protected static LocationRequest newLocationRequest;
    protected static FusedLocationProviderClient myFusedLocationProviderClient;
    protected static boolean requestLocationUpdates = true;
    protected static boolean notified = false;
    protected static int LOCATION_UPDATE_INTERVAL = 10000;
    protected static int FAST_LOCATION_UPDATE_INTERVAL = 5000;
    protected static int PROXIMITY_DISTANCE = 5;
    protected static final int ACCESS_FINE_LOCATION_CODE = 110;
    public String channel_id;
    public String channel_desc;
    public String channel_name;
    protected int noti_importance;
    protected final int dest_arrived_id = 101;

    public MyLocationIntentService() {
        super("");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.e(TAG, "in HandleIntent :entry");

        ring_mgr =new RingtoneManager(context);

        buildGoogleApiClient();
        myGoogleApiClient.connect();
        createLocationRequest();

        if (myGoogleApiClient.isConnected() && requestLocationUpdates) {
            startLocationupdates();
            Log.e(TAG, "in HandleIntent : starting location updates");
        }

        myLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e("LocationMethods", "in LocatioCallback");
                for (Location newlocation : locationResult.getLocations()) {
                    Log.e(TAG, "in LocatioCallback" + (newlocation.getLatitude() + " , " + newlocation.getLongitude()));
                    myCurrentlocation = newlocation;
                    updateTextView("Current Location : ",newlocation);
                    alertOnCloseToDestination();
                    Toast.makeText(MainActivity.context, "new location : " + newlocation.getLatitude() + " , " + newlocation.getLongitude(), Toast.LENGTH_SHORT);
                }
            }
        };
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected");
        displayMyLastLocation();
        if (requestLocationUpdates)
        {
            startLocationupdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG,"in connectionSuspended");
        myGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("onConnectionFailed :", "Error Code = " + connectionResult.getErrorCode());
    }

    protected void createLocationRequest() {
        Log.e(TAG, "inside createLocationRequest");
        myGoogleApiClient.connect();
        newLocationRequest = new LocationRequest();
        newLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        newLocationRequest.setFastestInterval(FAST_LOCATION_UPDATE_INTERVAL);
        newLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        newLocationRequest.setSmallestDisplacement(PROXIMITY_DISTANCE);
    }

    public void alertOnCloseToDestination() {
        Log.e(TAG, "inside alertOnCloseToDestination");
        Location dest_location = new Location("");
        float dis_location;
        int onTasks = 0;
        long id_finishedTask = 3456789;
        SingleTaskObject task;
        for (int i = 0; i < MainActivity.taskObjectsList.size(); i++)
        {   task = MainActivity.taskObjectsList.get(i);
            if (task.getTask_status() == 1) {
                onTasks = onTasks + 1;
                dest_location.setLatitude(task.getLatitude());
                dest_location.setLongitude(task.getLongitude());
                dis_location = calculateDistance(dest_location, myCurrentlocation);
                Log.e(TAG, "task : " + task.getPlace_name() + " is on lees than " + dis_location);
                if (dis_location <= 100) {
                    Log.e(TAG, "task : " + task.getPlace_name() + "less than 100m");
                    if(id_finishedTask==task.getId()){
                        MainActivity.myDatabase = MainActivity.sqLiteHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(DatabaseContract.Database_table.COLUMN_TASKSTATUS, 0);
                        MainActivity.myDatabase.update(DatabaseContract.Database_table.TABLE_NAME, values, "_id = " + task.getId(), null);
                     //   mediaPlayer.stop();
                    }
                    if (!notified) ;
                    {
                        Log.e(TAG, "task : " + task.getPlace_name() + " is on lees than " + dis_location + "toast shown");
                        s = String.format("%.01f", dis_location);
                        Toast.makeText(MainActivity.context, task.getPlace_name() + " is only" + s + "m away", Toast.LENGTH_LONG).show();
                        notified = true;
                    }
                    if (dis_location <= 10) {
                        Log.e(TAG, "Disance less than 10m: Notify user");
                        playSound();
                        createNotification(dis_location, task.getPlace_name());
                        id_finishedTask=task.getId();

                    }
                }
            }
        }
         if(onTasks == 0) {
                stopLocationUpdates();
               // mediaPlayer.stop();
                notified = false;
            }

    }

    protected synchronized void buildGoogleApiClient() {
        Log.e(TAG, "before buildGoogleApiClient");
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        Log.e(TAG, "in if : creation of myGoogleClient :" + (myGoogleApiClient != null));
    }

    @SuppressLint("MissingPermission")
    public void displayMyLastLocation() {

        Log.e(TAG, "in displayMyLocation: Just Entered");
        //Log.e(TAG, "location provider assigned :");
        checkPermissions();
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.context);
        myFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.e(TAG, "addOnSuccessListener");
                if (location != null) {
                    myLastlocation = location;
                    updateTextView("Your Last Location is : ",myLastlocation );
                    Log.e("Last Known Location ", location.getLatitude() + " , " + location.getLongitude());
                    Toast.makeText(MainActivity.context, "Last Known Location is \n" + location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void makeRequest()
    {
        Log.e(TAG, "in makeRequest");
        ActivityCompat.requestPermissions(activity, new String[]
                {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
    }

    public void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                Log.e(TAG, "in displayMyLocation: before alert dialog");
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Location Permission is Necessary");
                alertBuilder.setMessage("App wont wok Correctly if location Permission is denied");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("In Dialog:showRequest", "in onClick");
                        makeRequest();
                    }
                });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            } else {
                makeRequest();
            }
            return;
        }
    }

    public float calculateDistance(Location currentLocation, Location destination) {
        float disInMetre = currentLocation.distanceTo(destination);
        return disInMetre;
    }


    public void updateTextView(String s,Location location) {
        MainActivity.topTextView.setText(s +location.getLatitude()+" , "+location.getLongitude());
    }

    public void ToggleLocationUpdates() {
        if (requestLocationUpdates == true) {
            requestLocationUpdates = false;
            Log.i("Toggle Location:", "location updates stooped");
            stopLocationUpdates();
        } else {
            Log.i(TAG, "Toggle Location : location updates enabled");
            requestLocationUpdates = true;
            startLocationupdates();
        }
    }

    @SuppressLint("MissingPermission")
    protected void startLocationupdates() {
        Log.e("startLocationUpdates:", "LocationUpdates Started");
        myFusedLocationProviderClient.requestLocationUpdates(newLocationRequest, myLocationCallback, null);
    }

    private void stopLocationUpdates() {
        Log.e("stopLocationUpdates:", "LocationUpdates Stopped");
        myFusedLocationProviderClient.removeLocationUpdates(myLocationCallback);
    }

public void playSound(){
    Uri notification = ring_mgr.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    mediaPlayer = MediaPlayer.create(getApplicationContext(), notification);
    mediaPlayer.start();
//    Ringtone ringtone = ring_mgr.getRingtone(context,notification);
  //      ring_mgr.stopPreviousRingtone();
    //    ringtone.play();
}

public void createNotification(float f, String place_name){
    NotificationManager myNotiMgr = (NotificationManager) getSystemService(context.NOTIFICATION_SERVICE);
    channel_id = "my_channel_01";
    channel_name = "Location Reminder";
    channel_desc="When user Reaches its desired Destination";
    noti_importance=NotificationManager.IMPORTANCE_HIGH;
    NotificationChannel myNotiChannel= null;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        myNotiChannel = new NotificationChannel(channel_id,channel_name,noti_importance);
        myNotiChannel.setDescription(channel_desc);
        myNotiChannel.setVibrationPattern(new long[]{100,200,300,400,300,200,100});
        myNotiMgr.createNotificationChannel(myNotiChannel);
    }

    NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(MainActivity.context,channel_id)
            .setSmallIcon(R.drawable.ic_location_on_cyan_400_24dp)
            .setContentTitle("LOCATION REMINDER")
            .setContentText("You are near to "+place_name+".\nClick to turn off the reminder ");

    Intent notiIntent = new Intent(this,MainActivity.class);
    TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
    taskStackBuilder.addParentStack(MainActivity.class);
    taskStackBuilder.addNextIntent(notiIntent);
    PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
    notiBuilder.setContentIntent(pendingIntent);
    myNotiMgr.notify(dest_arrived_id,notiBuilder.build());
}

}
