package rma.shivam.audiorecorder.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.renderscript.Sampler;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.global.Values;
import rma.shivam.audiorecorder.helper.LogHelper;
import rma.shivam.audiorecorder.local.CookiesAdapter;
import rma.shivam.audiorecorder.model.AutoStartHandler;
import rma.shivam.audiorecorder.model.History;

import static rma.shivam.audiorecorder.helper.LogHelper.appendToCsv;
import static rma.shivam.audiorecorder.helper.LogHelper.closeCsv;
import static rma.shivam.audiorecorder.helper.LogHelper.createCsv;

public class BackgroundRunner extends Service {

    public static String TIMESTAMP = Values.NULL_VALUE;

    public static String LAT = Values.NULL_VALUE;
    public static String LON = Values.NULL_VALUE;
    public static String ACCURACY = Values.NULL_VALUE;
    public static String SPEED = Values.NULL_VALUE;
    public static String PROVIDER = Values.NULL_VALUE;
    public static String GEO_STATE = Values.NULL_VALUE;

    public static String LAT2 = Values.NULL_VALUE;
    public static String LON2 = Values.NULL_VALUE;

    public static String TECH = Values.NULL_VALUE; //2G,3G,4G
    public static String SUB_TECH = Values.NULL_VALUE; //GSM-2G, WCDMA-3G, LTE-4G
    public static String ASU = Values.NULL_VALUE;
    public static String RSRQ = Values.NULL_VALUE; //ONLY 4G
    public static String ECIO = Values.NULL_VALUE; //3G
    public static String RX_QUAL = Values.NULL_VALUE; // 2G
    public static String RSRP = Values.NULL_VALUE; //ONLY 4G
    public static String RSCP = Values.NULL_VALUE; //3G
    public static String RX_LEVEL = Values.NULL_VALUE; // 2G
    public static String EARFCN = Values.NULL_VALUE; //ONLY 4G
    public static String UARFCN = Values.NULL_VALUE; //3G
    public static String ARFCN = Values.NULL_VALUE; // 2G
    public static String SINR = Values.NULL_VALUE; //ONLY 4G
    public static String MCC = Values.NULL_VALUE;
    public static String MNC = Values.NULL_VALUE;
    public static String LAC_TAC = Values.NULL_VALUE; //4G/3G/2G
    public static String CELL_ID = Values.NULL_VALUE; //ONLY 4G
    public static String PSC_PCI = Values.NULL_VALUE; //PSC FOR 3G/2G, PCI FOR 4G
    public static String SPN = Values.NULL_VALUE; //SERVICE PROVIDER NAME
    public static String DATA_STATE = Values.NULL_VALUE; //SAME FOR BOTH SIM
    public static String SERVICE_STATE = Values.NULL_VALUE; //SAME FOR BOTH SIM
    public static String BLUETOOTH_STATE = Values.NULL_VALUE; //SAME FOR BOTH SIM

    public static String RNC = Values.NULL_VALUE;
    public static String CQI = Values.NULL_VALUE;
    public static String FreqBand = Values.NULL_VALUE;
    public static String FREQ = Values.NULL_VALUE;
    public static String BAND = Values.NULL_VALUE;
    public static String TA = Values.NULL_VALUE;
    public static String CALL_STATE = Values.NULL_VALUE;
    public static String CALL_DURATION = Values.NULL_VALUE;
    public static String TEST_STATE = Values.NULL_VALUE;
    public static String RSSI = Values.NULL_VALUE;
    public static String SS = Values.NULL_VALUE;

    public static String WIFI_SSID = Values.NULL_VALUE;
    public static String WIFI_IP = Values.NULL_VALUE;
    public static String WIFI_RSSI = Values.NULL_VALUE;
    public static String WIFI_FREQ = Values.NULL_VALUE;
    public static String WIFI_LINK_SPEED = Values.NULL_VALUE;
    public static String WIFI_STATE = Values.NULL_VALUE;
    public static String DATA_NETWORK_TYPE = Values.NULL_VALUE;

    TelephonyManager tm;
    LocationManager locationManager;
    WifiManager wifiManager;
    ConnectivityManager connectivityManager;
    MyPhoneStateListener myPhoneStateListener;
    BluetoothAdapter bluetoothAdapter;
    Handler mHandler;
    Runnable mHandlerTask;
    Handler telephonyDataCollectorHandler;
    Runnable telephonyDataCollector;

    private static FusedLocationProviderClient mFusedLocationProviderClient;
    private static LocationRequest locationRequest;

    DecimalFormat decimalFormat;

    public static Context context;
    public static boolean processRunning = false;
    private static boolean killProcess = Constant.DEFAULT_KILL_PROCESS;
    int previousCallState = -2;
    long callStartTime = 0;
    boolean logToBeSaved, appPrivilagedHigh;
    int LOOP_INTERVAL;
    int SPLIT_ON;
    public static String DEFAULT_LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

    static long count = 0;

    BroadcastReceiver wifiSignalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fetchWifiData();
        }
    };

    BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fetchWifiData();
        }
    };

    private void fetchWifiData() {
        if(Utils.isWifiConnected(context)) {
            WIFI_STATE = "1";
            WifiInfo info = wifiManager.getConnectionInfo();
            WIFI_SSID = info.getSSID();
            WIFI_RSSI = String.valueOf(info.getRssi());
            int ip = info.getIpAddress();
            WIFI_IP = Utils.getIpv4(ip);
            WIFI_FREQ = info.getFrequency()+"";
            WIFI_LINK_SPEED = info.getLinkSpeed()+"";
        }else{
            WIFI_STATE = "0";
            WIFI_SSID = Values.NULL_VALUE;
            WIFI_RSSI = Values.NULL_VALUE;
            WIFI_IP = Values.NULL_VALUE;
            WIFI_FREQ = Values.NULL_VALUE;
            WIFI_LINK_SPEED = Values.NULL_VALUE;
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Utils.logPrint(this.getClass(),"Location","changed");
            if(location != null) {
                ACCURACY = String.valueOf(location.getAccuracy());
                SPEED = String.valueOf(location.getSpeed());
                LAT = String.valueOf(location.getLatitude());
                LON = String.valueOf(location.getLongitude());
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Utils.logPrint(this.getClass(),"onStatusChanged","triggered");
            Utils.logPrint(this.getClass(),"provider , status , extras",provider+" , "+String.valueOf(status)+" , "+extras.toString());
            setupLocationListener(provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Utils.logPrint(this.getClass(),"onProviderEnabled",provider + " triggered");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Utils.logPrint(this.getClass(),"onProviderDisabled",provider + " triggered");
            try {
                sendBroadcast(Values.CODE_LOCATION, "disabled");
            }catch (Exception e){
                Log.println(Log.ASSERT,"Error stopping process",Log.getStackTraceString(e));
            }
        }
    };

    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            boolean avail = locationAvailability.isLocationAvailable();
            Utils.logPrint(getClass(),"locationAvailability",avail+"");
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if(locationResult == null){
                Utils.logPrint(getClass(),"locationAvailability","null");
            }else {
                Utils.logPrint(getClass(),"locationAvailability",locationResult.getLastLocation().toString());
                LAT2 = locationResult.getLastLocation().getLatitude()+"";
                LON2 = locationResult.getLastLocation().getLongitude()+"";
            }
        }
    };

    public BackgroundRunner() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Utils.logPrint(this.getClass(),"onBind","triggered");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.logPrint(this.getClass(),"onStartCommand","triggered");
        count = 0;
        processRunning = true;
        init();
        resetAllValues();
        if(logToBeSaved) {
            createCsv();
        }
        setupWifiListener();
        getDeviceLocation(DEFAULT_LOCATION_PROVIDER);
        setupLocationListener(DEFAULT_LOCATION_PROVIDER);
        setupPhoneStateListener();
        telephonyDataCollector.run();
        mHandlerTask.run();
        sendBroadcast(Values.CODE_SERVICE, Values.SERVICE_STARTED);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Utils.logPrint(this.getClass(), "onDestroy", "triggered");
            processRunning = false;
            removePhoneStateListener();
            removeLocationListenor();
            removeWifiListener();
            telephonyDataCollectorHandler.removeCallbacks(telephonyDataCollector);
            mHandler.removeCallbacks(mHandlerTask);
            if (logToBeSaved) {
                closeCsv();
            }
//            createHistory();
            sendBroadcast(Values.CODE_SERVICE, Values.SERVICE_STOPPED);
        }catch (Exception ignored){}
    }

    private void init() {
        context = this;
        killProcess = Utils.getBoolean(context, Constant.KEY_KILL_PROCESS, Constant.DEFAULT_KILL_PROCESS);
        LOOP_INTERVAL = Utils.getInt(context, Values.KEY_LOOP_INTERVAL, Values.DEFAULT_LOOP_INTERVAL);
        SPLIT_ON = Utils.getInt(context, Values.KEY_SPLIT_ON, Values.DEFAULT_SPLIT_ON);
        appPrivilagedHigh = Utils.getBoolean(context, Values.KEY_APP_ON_FRONT, Values.DEFAULT_APP_ON_FRONT);
        logToBeSaved = Utils.getBoolean(context, Values.KEY_SAVE_LOG, Values.DEFAULT_SAVE_LOG);
        locationManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        DEFAULT_LOCATION_PROVIDER = getBestLocationProvider();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        decimalFormat = new DecimalFormat("0.0");
        mHandler = new Handler();
        mHandlerTask = new Runnable() {
            @Override
            public void run() {
                TIMESTAMP = String.valueOf(System.currentTimeMillis());
                logPrint();
                mHandler.postDelayed(mHandlerTask, LOOP_INTERVAL);
            }
        };
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyDataCollectorHandler = new Handler();
        telephonyDataCollector = new Runnable() {
            @Override
            public void run() {
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                collectTelephonyData();
                telephonyDataCollectorHandler.postDelayed(telephonyDataCollector, LOOP_INTERVAL);
            }
        };
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private String getBestLocationProvider(){
        Criteria myCriteria = new Criteria();
        myCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        myCriteria.setPowerRequirement(Criteria.POWER_LOW);
        // let Android select the right location provider for you
        return locationManager.getBestProvider(myCriteria, true);
    }

    private void resetAllValues(){
        GEO_STATE = Utils.getString(context, Constant.KEY_MY_LOCATION, Values.NULL_VALUE);
        LAT = Values.NULL_VALUE;
        LON = Values.NULL_VALUE;
        ACCURACY = Values.NULL_VALUE;
        SPEED = Values.NULL_VALUE;

        LAT2 = Values.NULL_VALUE;
        LON2 = Values.NULL_VALUE;

        TECH = Values.NULL_VALUE; //2G,3G,4G
        SUB_TECH = Values.NULL_VALUE; //GSM-2G, WCDMA-3G, LTE-4G
        ASU = Values.NULL_VALUE;
        RSRQ = Values.NULL_VALUE; //ONLY 4G
        ECIO = Values.NULL_VALUE; //3G
        RX_QUAL = Values.NULL_VALUE; // 2G
        RSRP = Values.NULL_VALUE; //ONLY 4G
        RSCP = Values.NULL_VALUE; //3G
        RX_LEVEL = Values.NULL_VALUE; // 2G
        EARFCN = Values.NULL_VALUE; //ONLY 4G
        UARFCN = Values.NULL_VALUE; //3G
        ARFCN = Values.NULL_VALUE; // 2G
        SINR = Values.NULL_VALUE; //ONLY 4G
        MCC = Values.NULL_VALUE;
        MNC = Values.NULL_VALUE;
        LAC_TAC = Values.NULL_VALUE; //4G/3G/2G
        CELL_ID = Values.NULL_VALUE; //ONLY 4G
        PSC_PCI = Values.NULL_VALUE; //PSC FOR 3G/2G, PCI FOR 4G
        SPN = Values.NULL_VALUE; //SERVICE PROVIDER NAME
        DATA_STATE = Values.NULL_VALUE; //SAME FOR BOTH SIM
        SERVICE_STATE = Values.NULL_VALUE; //SAME FOR BOTH SIM
        BLUETOOTH_STATE = Values.NULL_VALUE; //SAME FOR BOTH SIM

        RNC = Values.NULL_VALUE;
        CQI = Values.NULL_VALUE;
        FreqBand = Values.NULL_VALUE;
        FREQ = Values.NULL_VALUE;
        BAND = Values.NULL_VALUE;
        TA = Values.NULL_VALUE;
        CALL_STATE = Values.NULL_VALUE;
        CALL_DURATION = Values.NULL_VALUE;
        TEST_STATE = Values.NULL_VALUE;
        RSSI = Values.NULL_VALUE;
        SS = Values.NULL_VALUE;

        JitterService.JITTER = Values.NULL_VALUE;
    }

    private void setupWifiListener() {
        registerReceiver(wifiSignalReceiver,  new IntentFilter(WifiManager.RSSI_CHANGED_ACTION ));
        registerReceiver(wifiStateReceiver,  new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION ));
    }

    private void removeWifiListener() {
        unregisterReceiver(wifiSignalReceiver);
        unregisterReceiver(wifiStateReceiver);
    }

    @SuppressLint("MissingPermission")
    private void setupLocationListener(String locationProvider){
        PROVIDER = locationProvider;
        Utils.logPrint(this.getClass(),"LocationListener","invoked");
        locationManager.requestLocationUpdates( locationProvider,
                Values.LOCATION_MIN_TIME,
                Values.LOCATION_MIN_DISTANCE, locationListener);
    }

    private void removeLocationListenor(){
        Utils.logPrint(this.getClass(),"LocationListener","revoked");
        locationManager.removeUpdates(locationListener);
    }

    void getDeviceLocation(String provider) {
        /**
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        Utils.logPrint(this.getClass(),"getDeviceLocation","triggered");
        try {
//            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
//            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
//                @Override
//                public void onComplete(@NonNull Task<Location> task) {
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        Location mLastKnownLocation = task.getResult();
//                        if(mLastKnownLocation != null) {
//                            ACCURACY = String.valueOf(mLastKnownLocation.getAccuracy());
//                            SPEED = String.valueOf(mLastKnownLocation.getSpeed());
//                            LAT = String.valueOf(mLastKnownLocation.getLatitude());
//                            LON = String.valueOf(mLastKnownLocation.getLongitude());
//                            Utils.logPrint(this.getClass(),"Location", String.valueOf(mLastKnownLocation.getLatitude())+String.valueOf(mLastKnownLocation.getLongitude()));
//                        }
//                    }
//                }
//            });
            Location mLastKnownLocation = locationManager.getLastKnownLocation(provider);
            if(mLastKnownLocation != null) {
                ACCURACY = String.valueOf(mLastKnownLocation.getAccuracy());
                SPEED = String.valueOf(mLastKnownLocation.getSpeed());
                LAT = String.valueOf(mLastKnownLocation.getLatitude());
                LON = String.valueOf(mLastKnownLocation.getLongitude());
                Utils.logPrint(this.getClass(),"Location", String.valueOf(mLastKnownLocation.getLatitude())+String.valueOf(mLastKnownLocation.getLongitude()));
            }
        } catch (SecurityException e)  {
            Log.println(Log.ASSERT, "Error fetching location",Log.getStackTraceString(e));
            if(provider.equals(DEFAULT_LOCATION_PROVIDER)){
                getDeviceLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
    }

    private void collectTelephonyData() {
//        Utils.logPrint(this.getClass(),"collectTelephonyData","started");
        if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS
                || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA
                || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA
                || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA
                || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP) {
            TECH = Values.TECH_3G;
            SUB_TECH = Values.SUB_TECH_3G;
        } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
            TECH = Values.TECH_4G;
            SUB_TECH = Values.SUB_TECH_4G;
        } else {
            TECH = Values.TECH_2G;
            SUB_TECH = Values.SUB_TECH_2G;
        }
        SPN = Utils.getCentricSpn(tm.getNetworkOperatorName());
        List<CellInfo> allCellInfo = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            allCellInfo = tm.getAllCellInfo();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Utils.logPrint(getClass(),"getCarrierConfig", String.valueOf(tm.getCarrierConfig()));
            }
        }
        if(allCellInfo != null && allCellInfo.size() > 0) {
            for (CellInfo cellInfo : allCellInfo) {
                if (cellInfo.isRegistered()) {
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
//                        Utils.logPrint(this.getClass(), "CellInfoLte", "detected");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            EARFCN = String.valueOf(cellInfoLte.getCellIdentity().getEarfcn());
                            UARFCN = Values.NULL_VALUE;
                            ARFCN = Values.NULL_VALUE;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            RSRQ = String.valueOf(cellInfoLte.getCellSignalStrength().getRsrq());
                            RSRQ = RSRQ.equals(Values.ANDROID_ERROR)?Values.NULL_VALUE:RSRQ;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            RSSI = String.valueOf(cellInfoLte.getCellSignalStrength().getRssi());
                            RSSI = RSSI.equals(Values.ANDROID_ERROR)?Values.NULL_VALUE:RSSI;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            BAND = String.valueOf(cellInfoLte.getCellIdentity().getBandwidth());
                        }
                        TA = String.valueOf(cellInfoLte.getCellSignalStrength().getTimingAdvance());
                        ASU = String.valueOf(cellInfoLte.getCellSignalStrength().getAsuLevel());
                        RSRP = String.valueOf(cellInfoLte.getCellSignalStrength().getDbm());
                        RSCP = Values.NULL_VALUE;
                        RX_LEVEL = Values.NULL_VALUE;
                        MCC = String.valueOf(cellInfoLte.getCellIdentity().getMcc());
                        MNC = String.valueOf(cellInfoLte.getCellIdentity().getMnc());
                        LAC_TAC = String.valueOf(cellInfoLte.getCellIdentity().getTac());
                        CELL_ID = String.valueOf(cellInfoLte.getCellIdentity().getCi());
                        PSC_PCI = String.valueOf(cellInfoLte.getCellIdentity().getPci());
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            UARFCN = String.valueOf(cellInfoWcdma.getCellIdentity().getUarfcn());
                            EARFCN = Values.NULL_VALUE;
                            ARFCN = Values.NULL_VALUE;
                        }
                        BAND = Values.NULL_VALUE;
                        TA = Values.NULL_VALUE;
                        ASU = String.valueOf(cellInfoWcdma.getCellSignalStrength().getAsuLevel());
                        RSCP = String.valueOf(cellInfoWcdma.getCellSignalStrength().getDbm());
                        RSRP = Values.NULL_VALUE;
                        RX_LEVEL = Values.NULL_VALUE;
                        MCC = String.valueOf(cellInfoWcdma.getCellIdentity().getMcc());
                        MNC = String.valueOf(cellInfoWcdma.getCellIdentity().getMnc());
                        LAC_TAC = String.valueOf(cellInfoWcdma.getCellIdentity().getLac());
                        CELL_ID = String.valueOf(cellInfoWcdma.getCellIdentity().getCid());
                        PSC_PCI = String.valueOf(cellInfoWcdma.getCellIdentity().getPsc());
                    } else if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            ARFCN = String.valueOf(cellInfoGsm.getCellIdentity().getArfcn());
                            UARFCN = Values.NULL_VALUE;
                            EARFCN = Values.NULL_VALUE;
                        }
                        BAND = Values.NULL_VALUE;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            TA = String.valueOf(cellInfoGsm.getCellSignalStrength().getTimingAdvance());
                        }
                        ASU = String.valueOf(cellInfoGsm.getCellSignalStrength().getAsuLevel());
                        RX_LEVEL = String.valueOf(cellInfoGsm.getCellSignalStrength().getDbm());
                        RSCP = Values.NULL_VALUE;
                        RSRP = Values.NULL_VALUE;
                        MCC = String.valueOf(cellInfoGsm.getCellIdentity().getMcc());
                        MNC = String.valueOf(cellInfoGsm.getCellIdentity().getMnc());
                        LAC_TAC = String.valueOf(cellInfoGsm.getCellIdentity().getLac());
                        CELL_ID = String.valueOf(cellInfoGsm.getCellIdentity().getCid());
                        PSC_PCI = String.valueOf(cellInfoGsm.getCellIdentity().getPsc());
                    }
                }
            }
        }
        DATA_STATE = String.valueOf(tm.getDataState());
    }

    public void setupPhoneStateListener() {
        Utils.logPrint(this.getClass(),"PhoneStateListener","invoked");
        if (myPhoneStateListener == null)
            myPhoneStateListener = new MyPhoneStateListener();
        tm.listen(myPhoneStateListener,
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_CELL_LOCATION
                        | PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_CALL_STATE
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    public void removePhoneStateListener() {
        Utils.logPrint(this.getClass(),"PhoneStateListenor","revoked");
        if (myPhoneStateListener != null) {
            tm.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            Utils.logPrint(this.getClass(),"SignalStrength",signalStrength.toString());
            if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS
                    || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA
                    || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA
                    || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA
                    || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP) {
                String ssignal = signalStrength.toString();
                String[] parts = ssignal.split(" ");
                if (parts.length > 5) {
                    try {
                        ECIO = String.valueOf(Integer.parseInt(parts[4]) / 10);
                    } catch (NumberFormatException e) {
                        ECIO = "0";
                    }
                }
                int mRssi = signalStrength.getGsmSignalStrength();
                RSSI = String.valueOf(((2 * mRssi) - 113));
                SS = "0";
                RSRQ = Values.NULL_VALUE;
                RX_QUAL = Values.NULL_VALUE;
            } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                String ssignal = signalStrength.toString();
                String[] parts = ssignal.split(" ");
                if (parts.length > 8) {
                    try {
                        SS = String.valueOf(Integer.parseInt(parts[8]) * 2 - 113);
                    } catch (NumberFormatException e) {
                        SS = "0";
                    }
                }
                if (parts.length > 9 && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    try {
                        RSSI = parts[9].equals(Values.ANDROID_ERROR)?Values.NULL_VALUE:parts[9];
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if (parts.length > 10 && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    try {
                        RSRQ = parts[10];
                    } catch (NumberFormatException e) {
                        RSRQ = "0";
                    }
                }
                if (parts.length > 11) {
                    try {
                      SINR = parts[11].equals(Values.ANDROID_ERROR)?Values.NULL_VALUE:
                              String.valueOf(decimalFormat.format(Double.valueOf(parts[11]) / 10));
                    } catch (Exception e) {
                        SINR = "0";
                    }
                    try {
                        double CQI_x = Double.valueOf(SINR);
                        CQI = String.valueOf((Values.CQI_m * CQI_x) + Values.CQI_c);
                    }catch (Exception e){
                        CQI = Values.NULL_VALUE;
                    }
                }
                ECIO = Values.NULL_VALUE;
                RX_QUAL = Values.NULL_VALUE;
            } else {
                RX_QUAL = String.valueOf((signalStrength.getGsmBitErrorRate() >= 0 && signalStrength.getGsmBitErrorRate() <= 7 ? signalStrength.getGsmBitErrorRate() : -1));
                int mRssi = signalStrength.getGsmSignalStrength();
                RSSI = String.valueOf(((2 * mRssi) - 113));
                SS = "0";
                RSRQ = Values.NULL_VALUE;
                RX_LEVEL = Values.NULL_VALUE;
            }
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            RNC = String.valueOf(((((GsmCellLocation) location).getCid() >> 16) & 0xffff));
        }


        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);
            CALL_STATE = String.valueOf(state);
            if(state == TelephonyManager.CALL_STATE_OFFHOOK && previousCallState != TelephonyManager.CALL_STATE_OFFHOOK){
                callStartTime = System.currentTimeMillis();
            } else if(state != TelephonyManager.CALL_STATE_OFFHOOK && previousCallState == TelephonyManager.CALL_STATE_OFFHOOK ){
                long callEndTime = System.currentTimeMillis();
                long callDuration = callEndTime - callStartTime;
                CALL_DURATION = String.valueOf(callDuration);
            }
            previousCallState = state;
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            SERVICE_STATE = String.valueOf(serviceState.getState());
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            super.onDataConnectionStateChanged(state, networkType);
        }
    }

    private void logPrint() {
        BLUETOOTH_STATE = String.valueOf(bluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET));
        if(logToBeSaved) {
//            if(count >= SPLIT_ON && CALL_STATE.equalsIgnoreCase(Values.CALL_STATE_IDLE)){
//                closeCsv();
//                createCsv();
//                count = 0;
//            }else {
                appendToCsv();
//            }
        }
        resetValues();
    }


    void resetValues(){
        CALL_DURATION = Values.NULL_VALUE;
    }

//    private void createHistory() {
//        long now = System.currentTimeMillis();
//        History history = new History(String.valueOf(now),AudioRecordingService.SESSION, AutoStartHandler.session,
//                AudioRecordingService.appToRecordName, Constant.NOT_CREATED,Constant.NOT_UPLOADED);
//        CookiesAdapter cookiesAdapter = new CookiesAdapter(context);
//        cookiesAdapter.openWritable();
//        cookiesAdapter.addHisory(history);
//        cookiesAdapter.close();
//    }

    void sendBroadcast(String key, String message){
        Intent intent = new Intent(Values.ACTION_LOGGING);
        intent.setPackage(getPackageName());
        intent.putExtra(key, message);
        getApplicationContext().sendBroadcast(intent);
        Utils.logPrint(getClass(),key, message);
    }
}
