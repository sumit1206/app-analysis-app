package rma.shivam.audiorecorder.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import rma.shivam.audiorecorder.R;
import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.helper.Ping;
import rma.shivam.audiorecorder.services.JitterService;

import static rma.shivam.audiorecorder.helper.Ping.ping;

public class SplashActivity extends AppCompatActivity {

    Context context;
    TextView alertText,appTitle,clickHere;
    Animation title_animation;
    PowerManager pm;
    String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        init();
        if(permissionsGranted()){
            startControllerActivity();
        }

//        title_animation = AnimationUtils.loadAnimation(this,R.anim.up_to_down);
        appTitle = findViewById(R.id.app_title);
//        appTitle.startAnimation(title_animation);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {
        context = this;
        alertText = findViewById(R.id.alert_text);
        clickHere = findViewById(R.id.click_here);
        packageName = getPackageName();
        pm = (PowerManager) getSystemService(POWER_SERVICE);
    }

    private boolean permissionsGranted() {
        for(String permission: Constant.permissionArray){
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, Constant.permissionArray, Constant.PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE && grantResults.length == Constant.permissionArray.length) {
            for (int i = 0;i < grantResults.length; i++){
                int result = grantResults[i];
                String permission = permissions[i];
                if(result == PackageManager.PERMISSION_DENIED &&
                        (!permission.equals(Manifest.permission.FOREGROUND_SERVICE) &&
                                !permission.equals(Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND) &&
                                !permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION))){
                    Utils.logPrint(this.getClass(),permission, String.valueOf(result));
                    alertText.setText(getString(R.string.permission_not_granted));
                    clickHere.setVisibility(View.VISIBLE);
                    clickHere.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionsGranted();
                        }
                    });
                    return;
                }
            }
            startControllerActivity();
        }
    }

    void startControllerActivity(){
        clickHere.setVisibility(View.GONE);
        alertText.setText("");
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pm.isIgnoringBatteryOptimizations(packageName)){
//            Utils.logPrint(this.getClass(),"isIgnoringBatteryOptimizations","false");
//            requestBatteryOptimization();
//        }else {
//            Utils.logPrint(this.getClass(),"isIgnoringBatteryOptimizations","true");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Utils.logPrint(this.getClass(),"hasDrawOverlayPermission","false");
            requestDrawOverlayPermission();
        }else {
            Utils.logPrint(this.getClass(),"hasDrawOverlayPermission","true");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(context, MainActivity.class));
                    finish();
                }
            }, 2000);
        }
//        }
    }

    void requestBatteryOptimization(){
        Utils.logPrint(this.getClass(),"requestBatteryOptimization","triggered");
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);//ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.setData(Uri.parse("package:" + packageName));
        startActivityForResult(intent, Constant.BATTERY_OPTIMIZATION_REQUEST_CODE);
    }

    public void requestDrawOverlayPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(context)) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
        startActivityForResult(intent, Constant.SYSTEM_OVERLAY_PERMISSION);
//            } else {
//                startControllerActivity();
//            }
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.logPrint(this.getClass(),"onActivityResult","triggered");
        if(requestCode == Constant.SYSTEM_OVERLAY_PERMISSION){
            startControllerActivity();
        }
    }

}
