package rma.shivam.audiorecorder.global;

import android.Manifest;
import android.content.Intent;

import rma.shivam.audiorecorder.ARapp;

public class Values {
    public static final int PERMISSION_REQUEST_CODE = 101;
    public static final int BATTERY_OPTIMIZATION_REQUEST_CODE = 102;
    public static final long CSV_DATA_COUNT = 1000;
    public static final String ANDROID_ERROR = "2147483647";
    public static final String ACTION_LOGGING = "bg runner";
    public static final String CODE_LOCATION = "bg location";
    public static final String CODE_SERVICE = "bg service";
    public static final String SERVICE_STARTED = "bg service started";
    public static final String SERVICE_STOPPED = "bg service stopped";
    public static String[] permissionArray = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND,
            Manifest.permission.INTERNET,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.GET_TASKS
    };

    public static final boolean DEFAULT_APP_ON_FRONT = false;
    public static final boolean DEFAULT_SAVE_LOG = true;
    public static final int DEFAULT_LOOP_INTERVAL = 1000;//500;//1000;//
    public static final String DEFAULT_BLACKBOX_ID = "0000";
    public static final int DEFAULT_BLACKBOX_ID_LENGTH = 4;
    public static final long DEFAULT_CALL_DURATION = 1000 * 60 * 3;//3 MINUTES
    public static final long DEFAULT_CALL_CONNECT_DURATION = 1000 * 10;//10 SECONDS
    public static final int DEFAULT_SPLIT_ON = 1000;

    public static final float LOCATION_MIN_DISTANCE = 0.1f;
    public static final int LOCATION_MIN_TIME = 100;

    public static final String KEY_SAVE_LOG = "key_save_log";
    public static final String KEY_APP_ON_FRONT = "key_app_on_front";
    public static final String KEY_LOOP_INTERVAL = "key_loop_interval";
    public static final String KEY_BLACKBOX_ID = "key_blackbox_id";
    public static final String KEY_SPLIT_ON = "key split on";

    public static final String TECH_4G = "4G";
    public static final String TECH_3G = "3G";
    public static final String TECH_2G = "2G";
    public static final String SUB_TECH_4G = "LTE";
    public static final String SUB_TECH_3G = "WCDMA";
    public static final String SUB_TECH_2G = "GSM";

    public static final int LOOP_INTERVAL = Utils.getInt(ARapp.context, KEY_LOOP_INTERVAL, DEFAULT_LOOP_INTERVAL);

    public static final String SAVE_PATH = Constant.RECORD_PATH+"/rma_passive_data";
    public static final String BACKUP_PATH = Constant.RECORD_BACKUP_PATH+"/rma_passive_data";
    public static final String LOG_PATH = "/rma_ar_log";
    public static final String FILE_NAME = "_rfReport.csv";
    public static final String DELIMETER = ",";
    public static final String LINE_SEPARATOR = "\n";
    public static final String NULL_VALUE = "-";
    public static final String ZERO = "0";
    public static final String CALL_STATE_IDLE = "0";

    public static final double CQI_m = 0.286406;
    public static final double CQI_c = 6.71307;
}
