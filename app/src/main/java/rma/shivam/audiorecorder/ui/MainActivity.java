package rma.shivam.audiorecorder.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

import rma.shivam.audiorecorder.ARapp;
import rma.shivam.audiorecorder.R;
import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.global.Values;
import rma.shivam.audiorecorder.model.AutoStartHandler;
import rma.shivam.audiorecorder.services.AnimationViewRotate;
import rma.shivam.audiorecorder.services.AudioRecordingService;
import rma.shivam.audiorecorder.services.BackgroundRunner;

public class MainActivity extends AppCompatActivity {

    public static Activity activity;
    Context context;
    Intent recordingService;
    IntentFilter intentRecording;
    IntentFilter intentLoging;
    TextView tvState, tvDuration, tvAppName, tvAlert, tvMyAppVersion;
    ImageView ivRefreshBtn, ivAppIcon;
    AudioManager audioManager;
    Toolbar mainToolbar;
    AnimationViewRotate refreshButtonRotation;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if (action != null) {
                switch (action){
                    case Constant.ACTION_RECORDING:
                        if(intent.hasExtra(Constant.KEY_RECORDING_CALLBACK)){
                            String message = intent.getStringExtra(Constant.KEY_RECORDING_CALLBACK);
                            if(message.equals(Constant.CODE_EXIT)){
                                findViewById(R.id.stop_btn).performClick();
                                break;
                            }
                            if(message.equals(Constant.CODE_ITERATION_STOPPED)){
                                findViewById(R.id.start_btn).setVisibility(View.VISIBLE);
                                break;
                            }
                            Utils.showLongToast(context, message);
                        }
                        if(intent.hasExtra(Constant.KEY_RECORDING_DURATION)){
                            long time = Long.parseLong(intent.getStringExtra(Constant.KEY_RECORDING_DURATION));
                            int sec = (int) (time % 60);
                            int min = (int) (time / 60);
                            tvDuration.setText(String.format("%02d", min) +":"+String.format("%02d", sec));
                        }
                        if(intent.hasExtra(Constant.KEY_RECORDING_START_NEXT)){
                            String appToStart = intent.getStringExtra(Constant.KEY_RECORDING_START_NEXT);
                            startNext(appToStart);
                        }
                        break;
                    case " ":
                    default:
                        Utils.logPrint(getClass(), "undefined", "broadcast");
                }
            }
        }

    };

    private void startNext(String appToStart) {
        String data[] = appToStart.split(Values.NULL_VALUE);
        tvAppName.setText(data[1]);
        Drawable icon = Utils.getPackageIcon(context, data[0]);
        if(icon != null)
            ivAppIcon.setImageDrawable(icon);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.start_btn).performClick();
            }
        }, 2000);
    }

    BroadcastReceiver broadcastReceiverLogging = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if (action != null) {
                switch (action){
                    case Values.ACTION_LOGGING:
                        if(intent.hasExtra(Values.CODE_SERVICE)){
                            String message = intent.getStringExtra(Values.CODE_SERVICE);
                            if(message.equals(Values.SERVICE_STARTED)){
                                findViewById(R.id.stop_btn).setVisibility(View.VISIBLE);
                            }else if (message.equals(Values.SERVICE_STOPPED)){
                                findViewById(R.id.stop_btn).setVisibility(View.GONE);
                            }
                        }
                        break;
                    case " ":
                    default:
                        Utils.logPrint(getClass(), "undefined", "broadcast");
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        showState();
        registerReceiver(broadcastReceiver, intentRecording);
        registerReceiver(broadcastReceiverLogging, intentLoging);
        setUpToolbar();
        setAppVersion();
    }

    private void init() {
        context = this;
        activity = this;
        tvState = findViewById(R.id.state);
        ivRefreshBtn = findViewById(R.id.refresh_button);
        tvDuration = findViewById(R.id.duration);
        tvAppName = findViewById(R.id.app_name);
        ivAppIcon = findViewById(R.id.app_icon);
        tvAlert = findViewById(R.id.alert);
        tvMyAppVersion = findViewById(R.id.my_app_version);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        recordingService = new Intent(context, AudioRecordingService.class);
        intentRecording = new IntentFilter(Constant.ACTION_RECORDING);
        intentLoging = new IntentFilter(Values.ACTION_LOGGING);
        refreshButtonRotation = new AnimationViewRotate(ivRefreshBtn);
    }

    private void showState() {
        String state = Utils.getString(context, Constant.KEY_MY_LOCATION, getString(R.string.my_location));
        tvState.setText(state);
        if(refreshButtonRotation.isRotating()){
            String msg = "City reset to " + state;
            Utils.showShortToast(context, msg);
            refreshButtonRotation.stopRotation();
        }
        Utils.logPrint(getClass(), "saved state", state);
        if(state.equals(getString(R.string.my_location))){
            fetchState();
        }
    }

    private void fetchState() {
        refreshButtonRotation.startRotation();
        getDeviceLocation();
    }

    void getDeviceLocation() {
        Utils.logPrint(this.getClass(),"getDeviceLocation","triggered");
        String provider = getBestProvider();
        if(!provider.equals(Values.NULL_VALUE)) {
            try {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                @SuppressLint("MissingPermission")
                Location mLastKnownLocation = locationManager.getLastKnownLocation(provider);
                if (mLastKnownLocation != null) {
                    double lat = mLastKnownLocation.getLatitude();
                    double lon = mLastKnownLocation.getLongitude();
                    Utils.logPrint(getClass(), "lat : lon", lat + " : " + lon);
                    new GetAddress(lat, lon).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    return;
                }else {
                    Utils.logPrint(getClass(), "getDeviceLocation", "null");
                }
            } catch (Exception ex) {
                Utils.logPrint(getClass(), "getDeviceLocation", Log.getStackTraceString(ex));
            }
        }
        String msg = "Enable location and retry again\nIf already enabled try setting ACCURACY to HIGH.";
        showErrorDialog(msg);
    }

    private String getBestProvider(){
        Criteria myCriteria = new Criteria();
        myCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        myCriteria.setPowerRequirement(Criteria.POWER_LOW);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(myCriteria, true);
        Utils.logPrint(getClass(), "provider", provider+"");
        if(provider == null){
            return Values.NULL_VALUE;
        }
        return provider;
    }

    private class GetAddress extends AsyncTask<Void, Void, String>{

        private double lat, lon;

        public GetAddress(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String state = null;
            try {
                List<Address> address = geocoder.getFromLocation(lat, lon, 1);
                state = address.get(0).getSubAdminArea();
            } catch (IOException e) {
                Utils.logPrint(getClass(), "GetAddress", Log.getStackTraceString(e));
            }
            return state;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Utils.logPrint(getClass(), "state", s);
            if(s == null){
                String msg = "Unable to fetch City from " + lat + " : " + lon + "\nCheck your internet connection";
                showErrorDialog(msg);
                return;
            }
            Utils.saveString(context, Constant.KEY_MY_LOCATION, s);
            showState();
        }
    }

    private void setAppVersion() {
        try {
            PackageInfo pinfo;
            pinfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
            String appVersion = pinfo.versionName;
            tvMyAppVersion.setText("v "+appVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private  void setUpToolbar(){
        mainToolbar = findViewById(R.id.main_toolbar);
        mainToolbar.setTitle(R.string.app_name);
        mainToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mainToolbar.setOverflowIcon(getResources().getDrawable(R.drawable.more_vert_black));
        setSupportActionBar(mainToolbar);
    }

    public void startClicked(View view) {
        if(locationAvailable()) {
            String appUri = Utils.getString(context, Constant.KEY_INTENT_URI, Constant.DEFAULT_INTENT_URI);
            Utils.logPrint(getClass(),"appUri from S.Pref", appUri);
            String appPackage = Utils.getString(context, Constant.KEY_APP_TO_RECORD_PACKAGE, Constant.DEFAULT_APP);
            Intent intent;
            if (appPackage.equals(Constant.PACKAGE_RAKUTEN)) {
                intent = getPackageManager().getLaunchIntentForPackage(appPackage);
                startActivity(intent);
            } else if (appPackage.equals(Constant.PACKAGE_YOUTUBE2)) {
                intent = getPackageManager().getLaunchIntentForPackage(appPackage);
                intent.putExtra("data", appUri);
                startActivity(intent);
            } else {
                Uri data = Uri.parse(appUri);
                Utils.logPrint(getClass(),"appUri after parsing", appUri);
                try {
                    intent = new Intent(Intent.ACTION_VIEW, data);
                    intent.setPackage(appPackage);
                    startActivity(intent);
                } catch (Exception e) {
                    try {
                        Utils.logPrint(getClass(), "intent error", Log.getStackTraceString(e));
                        intent = new Intent(Intent.ACTION_VIEW, data);
                        startActivity(intent);
                    } catch (Exception ex) {
                        String msg = "Failed opening \"" + appUri + "\" in \"" + appPackage + "\"\n\nERROR: " + ex.getMessage();
                        showErrorDialog(msg);
                        return;
                    }
                }
            }
//            startActivity(intent);
            startService(recordingService);
            if (AutoStartHandler.isActivated) {
                view.setVisibility(View.GONE);
            }
        }else {
            String msg = "Enable location and retry again\nIf already enabled try setting ACCURACY to HIGH.";
            showErrorDialog(msg);
        }
    }

    private boolean locationAvailable(){
        Criteria myCriteria = new Criteria();
        myCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        myCriteria.setPowerRequirement(Criteria.POWER_LOW);
        // let Android select the right location provider for you
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(myCriteria, true);
        if(provider == null){
            Utils.logPrint(getClass(), "provider", "null");
            return false;
        }
        BackgroundRunner.PROVIDER = provider;
        Utils.logPrint(getClass(),"provider", provider);
        return true;
    }

    private void showErrorDialog(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setPositiveButton("close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        alertDialog.setMessage(msg);
        alertDialog.show();
    }

    public void stopClicked(View view) {
        stopService(recordingService);
        view.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case  R.id.settings:
                if (AudioRecordingService.isRecording) {
                    Utils.showShortToast(context, getString(R.string.cannot_open_activity));
                    return false;
                }
                Intent settingsIntent = new Intent(context, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.history:
                if (AudioRecordingService.isRecording) {
                    Utils.showShortToast(context, getString(R.string.cannot_open_activity));
                    return false;
                }
                Intent historyIntent = new Intent(context, HistoryActivity.class);
                startActivity(historyIntent);
                break;
            case R.id.portal:
                if (AudioRecordingService.isRecording) {
                    Utils.showShortToast(context, getString(R.string.cannot_open_activity));
                    return false;
                }
                Intent portalIntent = new Intent(context, PortalActivity.class);
                startActivity(portalIntent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String appName = Utils.getString(context, Constant.KEY_APP_TO_RECORD_NAME, Constant.DEFAULT_APP_NAME);
        tvAppName.setText(appName);
        String packageName = Utils.getString(context, Constant.KEY_APP_TO_RECORD_PACKAGE, Constant.DEFAULT_APP);
        Drawable icon = Utils.getPackageIcon(context, packageName);
        if(icon != null)
            ivAppIcon.setImageDrawable(icon);
    }

    public void stateRefreshClicked(View view) {
        fetchState();
    }
}
