package com.felhr.serialportexamplesync;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        IdentityFragment.OnIdentityMessageListener,
        ConsoleFragment.OnConsoleMessageListener {

    private static final String TAG = "MainActivity";

    // UI
    private DrawerLayout drawer;

    private DevicesFragment devicesFragment;
    private MapFragment mapFragment;
    private IdentityFragment identityFragment;
    private ConsoleFragment consoleFragment;
    private Fragment currentFragment;

    // Triangulation
    public List<Device> mDeviceList;
    public DeviceListAdapter devAdapter;

    // Location
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    // USB Service
    private UsbService usbService;
    private MyHandler mHandler;

    // Verbose Levels
    // 0: None; 1: OK, ERR; 2: NRF SEND; 3: NRF RECV
    private int verbose;

    // My Identity
    private String myName = "TRI1";
    private double myLat;
    private double myLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toogle);
        toogle.syncState();

        // Fragments
        initFragments();

        if(savedInstanceState == null) {
            replaceFragment(consoleFragment);
            navigationView.setCheckedItem(R.id.nav_console);
        }

        // Triangulation
        mDeviceList = new ArrayList<>();
        devAdapter = new DeviceListAdapter(this, mDeviceList);

        // Location Service
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initLocationRequest(10000, 3000);
        setLocationCallBack();

        myLat = 0.0;
        myLon = 0.0;

        // Verbose Level
        verbose = 1;
    }

    private void initFragments() {
        devicesFragment = new DevicesFragment();
        mapFragment = new MapFragment();
        identityFragment = new IdentityFragment();
        consoleFragment = new ConsoleFragment();

        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.add(R.id.fragment_container, devicesFragment);
        ft.add(R.id.fragment_container, mapFragment);
        ft.add(R.id.fragment_container, identityFragment);
        ft.add(R.id.fragment_container, consoleFragment);

        ft.hide(devicesFragment);
        ft.hide(mapFragment);
        ft.hide(identityFragment);
        ft.hide(consoleFragment);

        ft.commit();

        currentFragment = consoleFragment;
    }

    private void replaceFragment(Fragment fragment) {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.hide(currentFragment);
        ft.show(fragment);
        ft.commit();

        currentFragment = fragment;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.nav_devices:
                replaceFragment(devicesFragment);
                break;
            case R.id.nav_map:
                replaceFragment(mapFragment);
                break;
            case R.id.nav_identity:
                replaceFragment(identityFragment);
                break;
            case R.id.nav_console:
                replaceFragment(consoleFragment);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();
        startService(UsbService.class, usbConnection, null);

        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

        stopLocationUpdates();
    }

    // Device Identity Methods

    public String getMyName() {
        return myName;
    }
    public double getMyLat() {
        return myLat;
    }

    public double getMyLon() {
        return myLon;
    }

    public int getVerbose() {
        return verbose;
    }

    /*
     * FRAGMENTS CALLBACK
     */

    // Identity Fragment

    @Override
    public void onDeviceNameChanged(String message) {
        myName = message;

        String data = "SD " + myName + ";";

        usbService.write(data.getBytes());
        consoleFragment.appendToConsole("> " + data + "\n");
    }

    @Override
    public void onVerboseLevelChanged(int level) {
        verbose = level;
    }

    // Console Fragment

    @Override
    public void onNewCommandInvoked(String message) {
        usbService.write(message.getBytes());
    }

    /*
     * TRIANGULATION SERVICE
     */

    private void removeIfContains(List<Device> list, Device item) {
        String itemName = item.getName();
        int listSize = list.size();

        for(int i = 0; i < listSize; i++)
            if(list.get(i).getName().equals(itemName)) {
                list.remove(i);
                return;
            }
    }

    // DEBUGGING: Change to private later
    public void updateDeviceLocation(String data) {
        String[] chunks = data.split(" ");
        Device device = new Device(chunks[1], Float.parseFloat(chunks[2]), Float.parseFloat(chunks[3]));

        removeIfContains(mDeviceList, device);
        mDeviceList.add(device);

        consoleFragment.appendToConsole("Device " + device.getName() + " updated\n");
    }

    // DEBUGGING: Change to private later
    public void iter() {
        int listSize = mDeviceList.size();

        for(int i = 0; i < listSize; i++) {
            Device item = mDeviceList.get(i);
            Log.d(TAG, "iter: Name: " + item.getName() + ", Lat: " + String.valueOf(item.getLatitude())
                    + ", Lon: " + String.valueOf(item.getLongitude()));
        }
    }

    /*
     * LOCATION SERVICE
     */
    private void initLocationRequest(long interval, long fastestInterval) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setLocationCallBack() {
        mLocationCallback = (new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        if (usbService != null) {
                            myLat = location.getLatitude();
                            myLon = location.getLongitude();

                            String data = "SP " +
                                    String.valueOf(round(location.getLatitude(), 6)) + " " +
                                    String.valueOf(round(location.getLongitude(), 6)) + ";";

                            consoleFragment.appendToConsole("> " + data + "\n");
                            usbService.write(data.getBytes());
                        }
                    }
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private double round(double val, int decimals) {
        double div = Math.pow(10, decimals);
        return Math.round(val * div) / div;
    }

    /*
     * USB SERIAL DRIVER
     */
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                consoleFragment.appendToConsole("USB Ready\n");
                break;
            case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                consoleFragment.appendToConsole("USB Permission not granted\n");
                break;
            case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                consoleFragment.appendToConsole("No USB connected\n");
                break;
            case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                consoleFragment.appendToConsole("USB disconnected\n");
                break;
            case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                consoleFragment.appendToConsole("USB device not supported\n");
                break;
        }
        }
    };

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        private StringBuilder serialData;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
            serialData = new StringBuilder();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    // String data = (String) msg.obj;
                    // mActivity.get().display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    String buffer = (String) msg.obj;

                    if(!buffer.equals("")) {
                        int index = buffer.indexOf("\n");

                        if (index != -1) {
                            serialData.append(buffer, 0, index);
                            mActivity.get().handleVerbose(serialData.toString());

                            serialData = new StringBuilder();
                            serialData.append(buffer, index + 1, buffer.length());

                        } else {
                            serialData.append(buffer);
                        }
                    }

                    break;
            }
        }
    }

    private void handleVerbose(String data) {
        if (verbose > 0 && (data.startsWith("OK") || data.startsWith("ERR"))) {
            consoleFragment.appendToConsole(data + "\n");
        }
        else if (verbose > 1 && data.startsWith("nRF24L01>")) {
            consoleFragment.appendToConsole(data + "\n");
        }
        else if (verbose > 2 && data.startsWith("nRF24L01<")) {
            consoleFragment.appendToConsole(data + "\n");
        }

        if (data.startsWith("nRF24L01<")) {
            updateDeviceLocation(data);
        }
    }
}
