package com.sunnykatiyar.locationtasker;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.util.Log.e;
import static android.util.Log.i;
import static com.sunnykatiyar.locationtasker.MyLocationIntentService.ACCESS_FINE_LOCATION_CODE;
//import static com.sunnykatiyar.locationtasker.LocationService.ACCESS_FINE_LOCATION_CODE;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    ListView location_listview;
    ArrayList<String> LocationList;
    Intent intent = new Intent();
    public Activity activity = this;
    public static Context context;
    LocationListAdapter locationListAdapter;
    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
    int PLACE_PICKER_REQUEST = 1;
    public static ArrayList<SingleTaskObject> taskObjectsList;
    public static Cursor myCursor;
    //Database Objects ;
    protected static SQLiteHelper sqLiteHelper;
    SingleTaskObject temp;
    protected static SQLiteDatabase myDatabase;
    ImageButton delete_btn;
    EditText editText_label;
    Switch taskstatus_switch;
    public static TextView topTextView;
    protected static int ifAllSwitchesOff ;
   // protected static Location myCurrentlocation;

    /*
  //Location Api variables and constants
  protected static Location myLastlocation;

    protected static LocationCallback myLocationCallback;
    protected static GoogleApiClient myGoogleApiClient;
    protected static LocationRequest newLocationRequest;
    protected static FusedLocationProviderClient myFusedLocationProviderClient;
    protected static boolean requestLocationUpdates = true;
    protected static boolean notified = false;
    protected static int LOCATION_UPDATE_INTERVAL = 10000;
    protected static int FAST_LOCATION_UPDATE_INTERVAL = 5000;
    protected static int PROXIMITY_DISTANCE = 1;
    protected static final int ACCESS_FINE_LOCATION_CODE = 110;
*/
    protected static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private String TAG = "MainActivity :";

    String[] projection = {DatabaseContract.Database_table._ID, DatabaseContract.Database_table.COLUMN_PLACENAME,
            DatabaseContract.Database_table.COLUMN_LATITUDE, DatabaseContract.Database_table.COLUMN_LONGITUDE,
            DatabaseContract.Database_table.COLUMN_LABEL, DatabaseContract.Database_table.COLUMN_TASKSTATUS,
            DatabaseContract.Database_table.COLUMN_TONE, DatabaseContract.Database_table.COLUMN_REPEAT};

/*
    protected void createLocationRequest() {
        Log.e("Location method", "createLocationRequest");
        newLocationRequest = new LocationRequest();
        newLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        newLocationRequest.setFastestInterval(FAST_LOCATION_UPDATE_INTERVAL);
        newLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
     // newLocationRequest.setSmallestDisplacement(PROXIMITY_DISTANCE);
    }
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

       // MyLocationIntentService.mediaPlayer.stop();
        context = getApplicationContext();
        sqLiteHelper = new SQLiteHelper(context);
        taskObjectsList = new ArrayList<>();
        location_listview = (ListView) findViewById(R.id.location_listview);
        taskstatus_switch = findViewById(R.id.task_switch);
        topTextView = findViewById(R.id.topTextView);
        delete_btn = findViewById(R.id.delete_imgbtn);

        myDatabase = sqLiteHelper.getReadableDatabase();
        myCursor = myDatabase.query(DatabaseContract.Database_table.TABLE_NAME, projection,
                null, null, null, null, "_id " + "DESC");

        //  Log.e("MainActivity.java", "onCreate: after querying database : "+myCursor.getCount());

        if ((myCursor != null) && (myCursor.getCount() > 0)) {
            for (int i = 0; i < myCursor.getCount(); i++) {
                myCursor.moveToNext();
                temp = new SingleTaskObject(
                        myCursor.getLong(myCursor.getColumnIndex(DatabaseContract.Database_table._ID)),
                        myCursor.getString(myCursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_PLACENAME)),
                        myCursor.getString(myCursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_LABEL)),
                        myCursor.getString(myCursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_TONE)),
                        myCursor.getDouble(myCursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_LATITUDE)),
                        myCursor.getDouble(myCursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_LONGITUDE)),
                        myCursor.getInt(myCursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_REPEAT)),
                        myCursor.getInt(myCursor.getColumnIndex(DatabaseContract.Database_table.COLUMN_TASKSTATUS)));
                taskObjectsList.add(temp);
                //  Log.e("MainActivity.java", "onCreate: inforloop : cursoring data queried)" + temp.getPlace_name());
            }
        }

        Log.e("MainActivity.java", "onCreate: Before setAdapter()");

 //     locationListAdapter = new LocationListAdapter(activity, taskObjectsList, myDatabase, myCursor, sqLiteHelper);
//      location_listview.setAdapter(locationListAdapter);
//      locationListAdapter.notifyDataSetChanged();

        MyCustomCursorAdapter myCursorAdapter = new MyCustomCursorAdapter(activity, myCursor, 0);
        location_listview.setAdapter(myCursorAdapter);
        myCursorAdapter.changeCursor(myCursor);

        Log.e("MainActivity.java", "onCreate: after setAdapter()");

        Log.e(" in oncreate :", "before onitem click ");
        location_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SingleTaskObject s = taskObjectsList.get(position);
            long task_id = s.getId();
            Log.e("in onitemclick :", " entered ");
            Toast.makeText(context, "inside onitemclick" + s.getId() + " : " + s.getPlace_name(), Toast.LENGTH_SHORT);
        }
        });

        if (checkGooglePlayServices()) {
            Log.e(TAG, "in oncreate: after if(checkgooglePlayServices)");
            Intent intent = new Intent(this,MyLocationIntentService.class);
            checkForPermissions();
            startService(intent);
            //buildGoogleApiClient();
            //createLocationRequest();
        }
/*
        myLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e("LocationMethods", "in LocatioCallback");
                for (Location newlocation : locationResult.getLocations()) {
                    Log.e("LocationMethods", "in LocatioCallback" + (newlocation.getLatitude() + " , " + newlocation.getLongitude()));
                    myCurrentlocation = newlocation;
                    updateTextView(newlocation);
                    alertOnCloseToDestination();
                    Toast.makeText(context, "new location : " + newlocation.getLatitude() + " , " + newlocation.getLongitude(), Toast.LENGTH_SHORT);
                }
            }
        };
*/
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.add_location: {
                try {
                    startActivityForResult(intentBuilder.build(activity), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }

        }
        return true;
    }

public void checkForPermissions(){
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION))
        {
            Log.e("Location method", "in displayMyLocation: before alert dialog");
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
/*
    public void alertOnCloseToDestination() {
        Location dest_location= new Location("");
        float dis_location;
        SingleTaskObject task;
        for (int i = 0; i < taskObjectsList.size(); i++)
        {
            task = taskObjectsList.get(i);
            if(task.getTask_status()==1)
            {   dest_location.setLatitude(task.getLatitude());
                dest_location.setLongitude(task.getLongitude());
                dis_location = calculateDistance(dest_location, myCurrentlocation);
                if(dis_location<=200)
                {
                    Toast.makeText(context, "Destination is " + dest_location+"m away", Toast.LENGTH_SHORT).show();
                  //  notifyUser(task,myCurrentlocation,dest_location);
                }
            }
        }
    }
      public void notifyUser(SingleTaskObject taskAboutTocomplete,Location currentLocation,Location destLocation){
        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(context)
    }
*/

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        myDatabase = sqLiteHelper.getWritableDatabase();

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(activity, data);
                String toastMsg1 = String.format("Place Selected: %s", place.getName());
                Toast.makeText(activity, toastMsg1, Toast.LENGTH_LONG).show();
                ContentValues contentValues = new ContentValues();
                contentValues.put(DatabaseContract.Database_table.COLUMN_PLACENAME, place.getName().toString());
                contentValues.put(DatabaseContract.Database_table.COLUMN_LATITUDE, place.getLatLng().latitude);
                contentValues.put(DatabaseContract.Database_table.COLUMN_LONGITUDE, place.getLatLng().longitude);
                contentValues.put(DatabaseContract.Database_table.COLUMN_LABEL,"");
                contentValues.put(DatabaseContract.Database_table.COLUMN_REPEAT, 0);
                contentValues.put(DatabaseContract.Database_table.COLUMN_TASKSTATUS, 1);
                contentValues.put(DatabaseContract.Database_table.COLUMN_TONE, "DEFAULT");

                long contentRow = myDatabase.insert(DatabaseContract.Database_table.TABLE_NAME, null, contentValues);

                finish();
                startActivity(getIntent());
                //Toast.makeText(activity,"Text Entered in Database"+place.getName().toString(), Toast.LENGTH_LONG).show();

            }
        }
    }

    public boolean checkGooglePlayServices() {
        GoogleApiAvailability gapi = GoogleApiAvailability.getInstance();
        int gms_Status = gapi.isGooglePlayServicesAvailable(context);
        Log.e(TAG, "CheckPlayServices");
        if (gms_Status != ConnectionResult.SUCCESS) {
            if (gapi.isUserResolvableError(gms_Status)) {
                gapi.getErrorDialog(this, gms_Status, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(context, "Cannot run without Google Play Services ", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        } else {
            return true;
        }
    }
/*
    protected synchronized void buildGoogleApiClient() {
        Log.e("Location method", "before buildGoogleApiClient");
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        Log.e("LocationMethods", "in if : creation of myGoogleClient" + (myGoogleApiClient != null));
    }
*/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("Location method", "onConnected");
        //displayMyLastLocation();
        //if (requestLocationUpdates) {
        //    startLocationupdates();
        //}
    }
/*
    public void displayMyLastLocation() {
        Log.e("Location method", "in displayMyLocation: Just Entered");

        Log.e("Location method", "in displayMyLocation: exittted");

        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        myFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.e("Location method", "addOnSuccessListener");
                if (location != null) {
                    myLastlocation = location;
                    updateTextView(myLastlocation);
                    Log.e("Last Known Location", location.getLatitude() + " , " + location.getLongitude());
                    Toast.makeText(context, "Last Known Location is \n" + location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
*/
    protected void makeRequest() {
        Log.e(TAG, "in makeRequest");
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Location method", "in onRequestPermissions");

        switch (requestCode) {
            case ACCESS_FINE_LOCATION_CODE: {
                if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("Permission :", "Granted Succcessfully");
                } else {
                    Log.i("Permission :", "Denied");
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //myGoogleApiClient.connect();
        Log.e(TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("onConnectionFailed :", "Error Code = " + connectionResult.getErrorCode());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "in onResume playservices = " + checkGooglePlayServices());
        checkGooglePlayServices();
       // if (myGoogleApiClient.isConnected() && requestLocationUpdates == true) {
       //     startLocationupdates();
    }


    @Override
    protected void onStart() {
        super.onStart();
      //  Log.e("Location method", "in onStart before connecting myGoogleApiClient = " + (myGoogleApiClient != null));
     //   if (myGoogleApiClient != null) {
       //     myGoogleApiClient.connect();
        //    Log.e("Location method", "in onStart: after connecting myGoogleApiClient = " + (myGoogleApiClient != null));
    }

/*
    @SuppressLint("MissingPermission")
    protected void startLocationupdates() {
        Log.e("startLocationUpdates:", "LocationUpdates Started");
        // myFusedLocationProviderClient.requestLocationUpdates(myGoogleApiClient,newLocationRequest,null);
        myFusedLocationProviderClient.requestLocationUpdates(newLocationRequest, myLocationCallback, null);
    }

    private void stopLocationUpdates() {
        Log.e("stopLocationUpdates:", "LocationUpdates Stopped");
        myFusedLocationProviderClient.removeLocationUpdates(myLocationCallback);
    }
*/



    @Override
    protected void onDestroy(){
        super.onDestroy();
        //stopLocationUpdates();
    }


}
// Log.e("MainActivity.java", "onCreate: before calling list adpater : "+taskObjectsList.size());

// myCursor.close();
//  setContentView(R.layout.activity_main);
