package rma.shivam.audiorecorder.global;

import android.Manifest;
import android.content.Intent;

public class Constant {
    public static final int SERVER_TIMEOUT = 5000;
    public static final int PERMISSION_REQUEST_CODE = 1000;
    public static final int BATTERY_OPTIMIZATION_REQUEST_CODE = 1001;
    public static final int SYSTEM_OVERLAY_PERMISSION = 1002;
    public static final int BOUND = 60;

    public static String[] permissionArray = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.CAPTURE_AUDIO_OUTPUT,
            Manifest.permission.PACKAGE_USAGE_STATS,
            Manifest.permission.KILL_BACKGROUND_PROCESSES,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND,
            Manifest.permission.INTERNET,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.GET_TASKS,
            Manifest.permission.SYSTEM_ALERT_WINDOW
    };

    public static final String FILE_NAME_APP_LIST = "app_list";
    public static final String SAVE_PATH_CACHE_DUMP = "cache_dump";
    public static final String DELIMETER = ",";
    public static final String LINE_SEPARATOR = "\n";
    public static final String LOG_DATE_FORMAT = "dd MMM yyyy hh:mm:ss a";

    public static final String DEFAULT_INTENT_URI = "https://www.youtube.com/watch?v=FlKGRJkdz8k";
    public static final String DEFAULT_APP = "com.google.android.youtube";
    public static final String DEFAULT_APP_NAME = "YouTube";
    public static final boolean DEFAULT_KILL_PROCESS = true;

    public static final String RECORD_PATH = "/rma_recorded";
    public static final String RECORD_PREFIX = "_record_";
    public static final String RECORD_RORMAT = ".amr";
    public static final String RECORD_BACKUP_PATH = "/rma_recorded_backup";

    public static final String ACTION_RECORDING = "recording service";
    public static final String ACTION_UPLOAD_TRIGGER = "action upload trigger";
    public static final String ACTION_FLOATING_BUTTON_CLICK = "action floating button clicked";

    public static final String KEY_MY_LOCATION = "my location";
    public static final String KEY_APP_DATA_VERSION = "app data version";
    public static final String KEY_RECORDING_CALLBACK = "recording callback";
    public static final String KEY_RECORDING_DURATION = "recording duration";
    public static final String KEY_RECORDING_START_NEXT = "recording_stopped_start_next";
    public static final String KEY_INTENT_URI = "intent uri";
    public static final String KEY_APP_TO_RECORD_PACKAGE = "app to record package";
    public static final String KEY_APP_TO_RECORD_NAME = "app to record name";
    public static final String KEY_SELECTED_APP_SERIAL = "selected app serial";
    public static final String KEY_PLAYBACK_TIME = "playback time";
    public static final String KEY_KILL_PROCESS = "kill process";
    public static final String CODE_ITERATION_STOPPED = "iteration stopped";
    public static final String CODE_EXIT = "exit";

    /**
     *todo MySql urls
    public static final String CSV_URL = "http://172.104.177.75/live_analyzer/analyser_dev/analysing_files/apis/upload_rf_data_1.php";
    public static final String AUDIO_URL = "http://172.104.177.75/live_analyzer/analyser_dev/analysing_files/analysing_file.php";
    public static final String APP_ANALYSER_PORTAL = "http://172.104.177.75/live_analyzer/analyser_dev/app_analyser/index.php";*/

    /**
     * todo Pg urls
     * */
    private static final String ROOT = "http://172.104.177.75/live_analyzer/analyser_dev_pg/analysing_files/apis/";
    public static final String APP_DATA_URL = ROOT + "cheak_version_send_data_app_urls.php";
    public static final String CSV_URL = ROOT + "upload_rf_data_2.php";
    public static final String AUDIO_URL = ROOT + "upload_audio_for_process.php";//"analyse_shi.php";//"analysing_file.php";
    public static final String APP_ANALYSER_PORTAL = "http://172.104.177.75/call_analyse/admin/login.php";

    public static final String NOT_UPLOADED = "not uploaded";
    public static final String UPLOADING = "uploading";
    public static final String UPLOADED = "uploaded";
    public static final String UPLOADING_FAILED = "failed";
    public static final String INVALID = "invalid";
    public static final String NOT_CREATED = "not created";

    public static final String PACKAGE_RAKUTEN = "tv.wuaki";
    public static final String PACKAGE_YOUTUBE2 = "rma.shivam.youtubeapplication";

}
