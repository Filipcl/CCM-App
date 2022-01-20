package com.example.android.androidskeletonapp.ui.main;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.android.androidskeletonapp.R;
import com.example.android.androidskeletonapp.data.Sdk;
import com.example.android.androidskeletonapp.ui.cold_chain.ColdChain;
import com.google.android.material.snackbar.Snackbar;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.annotations.NonNull;

public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final int PERMISSION_STORAGE_CODE = 1000;

    private TextView mConnectionState, mBatteryLvl;
    private String mDeviceName;
    private String mDeviceAddress;
    private TextView deviceName, currentTemp, minTemp, maxTemp, avgTemp ,currentHum, minHum, maxHum, currentDew, minDew, maxDew;
    private Button uploadBtn , settingsBtn, downloadBtn, deleteBtn, displayBtn;

    private BluetoothLeService mBluetoothLeService;
    private ArrayList<String> tempData = new ArrayList<>();
    private ArrayList<String> tempAlarm = new ArrayList<>();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    boolean tellSent = false;
    boolean infoSent = false;
    boolean uploadClicked = false;

    private String device_name;
    private String current_Temp;
    private String average_last_24h;
    private String max_Temp;
    private String min_Temp;

    private String CHANNEL_ID = "channel_1";
    private String CHANNEL_ID_2 = "channel_2";

    MyDatabaseHelper myDB;
    GraphView graphView;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Test";
            String description = "Test";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void createNotificationChannel2() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Test";
            String description = "Test";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_2, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                // When characteristics are fetched - automatically sendt command to get temps and stores it in a arraylist
                try {
                    if(!tellSent){
                        sendCommand(mBluetoothLeService, "*tell");
                        tellSent = true;
                        infoSent = false;
                        mBluetoothLeService.connect(mDeviceAddress);
                    }else if(!infoSent){
                        sendCommand(mBluetoothLeService, "*info");
                        tellSent = false;
                        infoSent = true;
                        mBluetoothLeService.connect(mDeviceAddress);
                    }
                    System.out.println("Command sent");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.DeviceControlToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Sdk.d2().trackedEntityModule().trackedEntityInstanceDownloader()
                .limit(10).limitByOrgunit(false).limitByProgram(false).download();
        Sdk.d2().eventModule().eventDownloader()
                .limit(10).limitByOrgunit(false).limitByProgram(false).download();

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        createNotificationChannel();
        createNotificationChannel2();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        // Sets up UI references.
        device_name = mDeviceAddress.substring(0,11);
        DeviceControlActivity.this.setTitle(device_name);
        myDB = new MyDatabaseHelper(DeviceControlActivity.this);
        ArrayList<Float> tempList = getCurrentTemp();
        //ArrayList<Date> dateList = getCaptureDateSorted();


        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mBatteryLvl = (TextView) findViewById(R.id.battery_lvl);

        deviceName = (TextView) findViewById(R.id.device_name);
        currentTemp = (TextView) findViewById(R.id.current_temp);
        minTemp = (TextView) findViewById(R.id.min_temp);
        maxTemp = (TextView) findViewById(R.id.max_temp);
        avgTemp = (TextView) findViewById(R.id.avg_temp);

        uploadBtn = (Button) findViewById(R.id.upload_btn);
        settingsBtn = (Button) findViewById(R.id.settings_btn);
        downloadBtn = (Button) findViewById(R.id.download_btn);
        deleteBtn = (Button) findViewById(R.id.delete_btn);
        displayBtn = (Button) findViewById(R.id.display_btn);

        graphView = findViewById(R.id.idGraphView);

        deviceName.setText(device_name);

        /*
        currentHum = (TextView) findViewById(R.id.current_hum);
        minHum = (TextView) findViewById(R.id.min_hum);
        maxHum = (TextView) findViewById(R.id.max_hum);
        currentDew = (TextView) findViewById(R.id.current_dew);
        minDew = (TextView) findViewById(R.id.min_dew);
        maxDew = (TextView) findViewById(R.id.max_dew);
         */

        /*
            for (int i = 0; i < tempAlarm.size() ; i++) {
                setTemperatureAlarm(tempAlarm.get(0), tempAlarm.get(1));
        }

         */


        DataPoint[] dataPoints = new DataPoint[tempList.size()]; // declare an array of DataPoint objects with the same size as your list
        for (int i = 0; i < tempList.size(); i++) {
            // add new DataPoint object to the array for each of your list entries
            dataPoints[i] = new DataPoint(i, tempList.get(i)); // not sure but I think the second argument should be of type double
        }

        graphView.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);

        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(6);

        graphView.addSeries(series);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadClicked = true;
                MyDatabaseHelper myDB = new MyDatabaseHelper(DeviceControlActivity.this);
                myDB.addTemperatures(device_name, current_Temp, max_Temp, min_Temp, average_last_24h);
                addEvent("g5oklCs7xIg","SDuMzcGLh8i","aecqgkE5quA", "iMDPax84iAN");
                Snackbar.make(v, "Adding temperature to database", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
       downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(DeviceControlActivity.this ,permissions, PERMISSION_STORAGE_CODE);
                    }
                    else{
                        startDownloading();
                        downloadNotifiction();
                        Snackbar.make(v, "Successfully downloaded", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
                else{
                    startDownloading();
                    downloadNotifiction();
                    Snackbar.make(v, "Successfully downloaded", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

       });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDB.deleteAllData();
                Snackbar.make(v, "Database cleared", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        displayBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(DeviceControlActivity.this);
                final EditText edittext = new EditText(DeviceControlActivity.this);
                alert.setTitle("Database");
                alert.setMessage(myDB.returnAllData().toString());
                alert.show();
                
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(DeviceControlActivity.this);
                LinearLayout layout = new LinearLayout(DeviceControlActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                alert.setTitle("Edit Alarm setting");
                alert.setMessage("New Alarm threshold");

                final EditText maxEditText = new EditText(DeviceControlActivity.this);
                maxEditText.setHint("20°C");
                layout.addView(maxEditText);

                final EditText minEditText = new EditText(DeviceControlActivity.this);
                minEditText.setHint("-40°C");
                layout.addView(minEditText);

                alert.setView(layout);

                alert.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String maxThreshold = maxEditText.getText().toString();
                        String minThreshold = minEditText.getText().toString();
                        setTemperatureAlarm(minThreshold, maxThreshold);
                        Toast.makeText(DeviceControlActivity.this, "Max temperature threshold: " + maxThreshold + " Min temperature threshold: " + minThreshold,
                                Toast.LENGTH_LONG).show();
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
            }

        });
    }

    public static final void sendCommand(BluetoothLeService service, String command) throws
            UnsupportedEncodingException {
        service.enableTXNotification();
        service.writeRXCharacteristic(command.getBytes("UTF-8"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //request granted from popup
                    startDownloading();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void startDownloading(){
        try {
           String file = SqliteExporter.export(myDB.getReadableDatabase());

        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result = " + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void downloadNotifiction(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        Uri uri = Uri.parse(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
        intent.setDataAndType(uri, "download/csv");

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(DeviceControlActivity.this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(DeviceControlActivity.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
                .setContentTitle("Successfully downloaded")
                .setContentText("Click to open file")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(DeviceControlActivity.this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(886, builder.build());
    }

    private void setTemperatureAlarm(String min , String max){
        String temp = "-2";
        int minTemp = Integer.parseInt(min);
        int maxTemp = Integer.parseInt(max);
        String status;

        if(Integer.parseInt(temp)  <= minTemp){
            status = "under";
        }else{
            status = "over";
        }
            if( Integer.parseInt(temp) <= minTemp || Integer.parseInt(temp) >= maxTemp ) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(DeviceControlActivity.this, CHANNEL_ID_2)
                        .setSmallIcon(R.drawable.ic_baseline_error_24)
                        .setContentTitle("Temperature Alarm")
                        .setContentText("Temperature are "+ status +" your threshold")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(DeviceControlActivity.this);
                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(887, builder.build());
            }
            else{
                System.out.println("Ops, could not send alarm");
            }

    }

    private ArrayList<Float> getCurrentTemp(){
        myDB = new MyDatabaseHelper(DeviceControlActivity.this);
        ArrayList<String> stringList = myDB.returnCurrentTemp();
        ArrayList<Float> floatList = new ArrayList<>();

        for (int i = 0; i < stringList.size(); i++) {
            floatList.add(Float.parseFloat(stringList.get(i).substring(0,4)));
        }
        return floatList ;
    }

    private ArrayList<Float> getCurrentTemps(){
        myDB = new MyDatabaseHelper(DeviceControlActivity.this);
        ArrayList<String> stringList = myDB.returnCurrentTemp();
        ArrayList<Float> floatList = new ArrayList<>();

        for (int i = 0; i < stringList.size(); i++) {
            //floatList.add(Float.parseFloat(stringList.get(i).substring(0,4)));
            System.out.println(floatList.get(i));
        }
        return floatList ;
    }
    /*
    private ArrayList<Date> getCaptureDateSorted() {
        myDB = new MyDatabaseHelper(DeviceControlActivity.this);
        ArrayList<String> dates =  myDB.returnCaptureDate();
        ArrayList<Date> formattedDates =  new ArrayList<>();

        for (int i = 0; i < dates.size() ; i++) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = format.format(dates.get(i));
            try {
                formattedDates.add(format.parse(dateString));
                System.out.println(formattedDates.get(i));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDates;
    }

     */
    private void updateTempColor(String temp , TextView tv){
        String s;
        if(temp.contains("-")){
            s = temp.substring(0,4);
            System.out.println(temp);
        }
        else if(temp.length() < 5){
            s = temp.substring(0,3);
            System.out.println(s);
        }else{
            s = temp.substring(0,4);
            System.out.println(s);
        }
        float f = Float.parseFloat(s);
        System.out.println(f);
        if(f <= 0){
            tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }else{
            tv.setTextColor(getResources().getColor(R.color.colorWarn));
        }
    }

    //adds data from sensor to array
    private void displayData(String data) {
        if (data != null) {
            tempData.add("---------");
            tempData.add(data);
            //readData(tempData);
            if(data.contains("Batt lvl")){
                mBatteryLvl.setText(data.substring(10,15));
            }
            if(data.contains("Cur Tem:")){
                current_Temp = data.substring(8,14).trim();
                updateTempColor(current_Temp , currentTemp);
                currentTemp.setText(data.substring(8,13) +"°");

            }
            if(data.contains("Lowst Tem:")){
               // min_Temp = data.substring(11,17).trim();
               // updateTempColor(min_Temp , minTemp);
               // minTemp.setText(data.substring(11,16)+"°");
            }
            if(data.contains("Hghst Tem:")){
                //max_Temp = data.substring(11,17);
                //maxTemp.setText(data.substring(11,17));
            }
            if(data.contains("24Hgh Tem:")){
                max_Temp = data.substring(11,17).trim();
                updateTempColor(max_Temp , maxTemp);
                maxTemp.setText(data.substring(11,15)+"°");
            }
            if(data.contains("24Low Tem:")){
                min_Temp = data.substring(11,17).trim();
                updateTempColor(min_Temp , minTemp);
                minTemp.setText(data.substring(11,15)+"°");
            }
            if(data.contains("24Avg Tem:")){
                average_last_24h = data.substring(11,17).trim();
                updateTempColor(average_last_24h , avgTemp);
                avgTemp.setText(data.substring(11,15)+"°");
            }
         /*   if(data.contains("Cur Hum:")){
                currentHum.setText(data.substring(8,14));
            }
            if(data.contains("Lowst Hum:")){
                minHum.setText(data.substring(11,17));
            }
            if(data.contains("Hghst Hum:")){
                maxHum.setText(data.substring(11,17));
            }
            if(data.contains("Lowst Dew:")){
                minDew.setText(data.substring(11,17));
            }
            if(data.contains("Hghst Dew:")){
                maxDew.setText(data.substring(11,17));
            }

          */
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void addEvent (String enrollmentID, String programUid , String programStageId, String ouUid) {
        String defaultOptionCombo = Sdk.d2().categoryModule().categoryOptionCombos()
                .byDisplayName().eq("default").one().blockingGet().uid();
        try {
            ArrayList<String> dbResult = getDbResult();

            //makes a new event and returns it eventUID
            String eventUid = Sdk.d2().eventModule().events().blockingAdd(
                    EventCreateProjection.builder()
                            .enrollment(enrollmentID)
                            .program(programUid)
                            .programStage(programStageId)
                            .organisationUnit(ouUid)
                            .attributeOptionCombo(defaultOptionCombo)
                            .build()
            );

            //sets the created-, enrollment- and event-date to this date.
            System.out.println("Ny Event UID :::::  " + eventUid);

            //Super important to have correct date format or it wont upload!!

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = format.format(new Date());
            Date date = format.parse (dateString);

            Sdk.d2().eventModule().events().uid(eventUid).setEventDate(date);
            Sdk.d2().enrollmentModule().enrollments().uid("g5oklCs7xIg").setEnrollmentDate(date);
            Sdk.d2().eventModule().events().uid(eventUid).setCompletedDate(date);


            //Get's the event's dataElement UID and set's a value to the event
            List<ProgramStageDataElement> programDataElem = Sdk.d2().programModule().programStageDataElements().blockingGet();
            for (ProgramStageDataElement elem: programDataElem
            ) {
                for (int i = 0; i < dbResult.size() ; i++) {
                    Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, elem.dataElement().uid()).blockingSet(dbResult.get(i).substring(0,4));
                    System.out.println(Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, elem.dataElement().uid()).blockingGet());
                }
            }

            //set eventStatus and upload
            Sdk.d2().eventModule().events().uid(eventUid).setStatus(EventStatus.COMPLETED);
            Sdk.d2().eventModule().events().upload();

        } catch (D2Error | ParseException d2Error) {
            d2Error.printStackTrace();
        }
    }

    private ArrayList<String> getDbResult(){
        myDB = new MyDatabaseHelper(DeviceControlActivity.this);
        //Prøv den nye spørringen her - kan hende at ikke alt blir lastet opp prøv da å endre format i blokking add.
        //return myDB.returnCurrentTemp();
        return myDB.returnAvgTemps();
    }

}
