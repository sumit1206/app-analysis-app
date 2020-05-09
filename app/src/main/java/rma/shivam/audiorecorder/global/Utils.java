package rma.shivam.audiorecorder.global;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import rma.shivam.audiorecorder.ARapp;
import rma.shivam.audiorecorder.R;
import rma.shivam.audiorecorder.helper.LogHelper;

import static android.content.Context.ACTIVITY_SERVICE;


public class Utils {

//    static class Log{
//        public static int ASSERT = 1;
//        public static void println(int type, String tag, String message){
//
//        }
//    }

    public static void showLongToast(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
    }

    public static void showShortToast(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showSnackBar(Context context, String msg){
        View view = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void logPrint(Class klass, String tag, String message){
        Log.println(Log.ASSERT,klass.getName(),tag+": "+message);
        LogHelper.writeToLog(klass.getName() + " : " + tag +" : " + message);
    }

    public static void saveBoolean(Context mContext, String name, boolean value) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }

    public static Boolean getBoolean(Context mContext, String name, boolean defaultvalue) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        return pref.getBoolean(name, defaultvalue);
    }

    public static void saveInt(Context mContext, String name, int value) {

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public static int getInt(Context mContext, String name, int defaultvalue) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        return pref.getInt(name, defaultvalue);
    }

    public static void saveString(Context mContext, String name, String value) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(name, value);
        editor.commit();
    }

    public static void removeString(Context mContext, String name) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(name);
        editor.commit();
    }

    public static String getString(Context mContext, String name,
                                   String defaultvalue) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        String val = pref.getString(name, defaultvalue);
        //Log.d(TAG, "val = " + val);
        return val;
    }

    public static void saveLong(Context mContext, String name, long value) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(name, value);
        editor.commit();
    }

    public static long getLong(Context mContext, String name,
                                   long defaultvalue) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        long val = pref.getLong(name, defaultvalue);
        //Log.d(TAG, "val = " + val);
        return val;
    }

    public static String getAppVersion(Context context){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return Values.NULL_VALUE;
        }
    }

    public static String getImei(Context context){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return telephonyManager.getImei(0);
                } else {
                    return  telephonyManager.getDeviceId();
                }
            }
        }catch (Exception e){
            logPrint(Utils.class,"error imei",Log.getStackTraceString(e));
        }
        return Values.NULL_VALUE;
    }

    public static String getTimestampInFormat(long millisecond, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);//,Locale.US
        String dateString = formatter.format(new Date(millisecond)).toString();
        return dateString;
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static String getReadableValue(long byt){
//        long byt = bits/8;
        if(byt < 1024)
            return String.valueOf(byt) + " byte";
        float kb = byt / 1024f;
        if(kb < 1024)
            return String.valueOf(kb) + " kb";
        return String.valueOf(kb / 1024f) + " mb";
    }

//    public static String getLocalIpv4Address() {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface
//                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf
//                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    System.out.println("ip1--:" + inetAddress);
//                    System.out.println("ip2--:" + inetAddress.getHostAddress());
//
//                    // for getting IPV4 format
//                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
//
//                        String ip = inetAddress.getHostAddress().toString();
////                        Log.println(Log.ASSERT, "IP", ip);
//                        // return inetAddress.getHostAddress().toString();
//                        return ip;
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            Log.println(Log.ASSERT, "ERROR", Log.getStackTraceString(ex));
//        }
//        return "Unavailable";
//    }

    public static String getIpv4(int ip){
        String ipString = String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        return ipString;
    }

    public static String getNetworkType(int networkType) {
        /** Network type is unknown */
        if(networkType == TelephonyManager.NETWORK_TYPE_UNKNOWN){ return "UNKNOWN";}
        /** Current network is GPRS */
        else if(networkType == TelephonyManager.NETWORK_TYPE_GPRS){ return "GPRS";}
        /** Current network is EDGE */
        else if(networkType == TelephonyManager.NETWORK_TYPE_EDGE){ return "EDGE";}
        /** Current network is UMTS */
        else if(networkType == TelephonyManager.NETWORK_TYPE_UMTS){ return "UMTS";}
        /** Current network is CDMA: Either IS95A or IS95B*/
        else if(networkType == TelephonyManager.NETWORK_TYPE_CDMA){ return "CDMA";}
        /** Current network is EVDO revision 0*/
        else if(networkType == TelephonyManager.NETWORK_TYPE_EVDO_0){ return "EVDO_0";}
        /** Current network is EVDO revision A*/
        else if(networkType == TelephonyManager.NETWORK_TYPE_EVDO_A){ return "EVDO_A";}
        /** Current network is 1xRTT*/
        else if(networkType == TelephonyManager.NETWORK_TYPE_1xRTT){ return "1xRTT";}
        /** Current network is HSDPA */
        else if(networkType == TelephonyManager.NETWORK_TYPE_HSDPA){ return "HSDPA";}
        /** Current network is HSUPA */
        else if(networkType == TelephonyManager.NETWORK_TYPE_HSUPA){ return "HSUPA";}
        /** Current network is HSPA */
        else if(networkType == TelephonyManager.NETWORK_TYPE_HSPA){ return "HSPA";}
        /** Current network is iDen */
        else if(networkType == TelephonyManager.NETWORK_TYPE_IDEN){ return "IDEN";}
        /** Current network is EVDO revision B*/
        else if(networkType == TelephonyManager.NETWORK_TYPE_EVDO_B){ return "EVDO_B";}
        /** Current network is LTE */
        else if(networkType == TelephonyManager.NETWORK_TYPE_LTE){ return "LTE";}
        /** Current network is eHRPD */
        else if(networkType == TelephonyManager.NETWORK_TYPE_EHRPD){ return "EHRPD";}
        /** Current network is HSPA+ */
        else if(networkType == TelephonyManager.NETWORK_TYPE_HSPAP){ return "HSPAP";}
        /** Current network is GSM */
        else if(networkType == TelephonyManager.NETWORK_TYPE_GSM){ return "GSM";}
        /** Current network is TD_SCDMA */
        else if(networkType == TelephonyManager.NETWORK_TYPE_TD_SCDMA){ return "SCDMA";}
        /** Current network is IWLAN */
        else if(networkType == TelephonyManager.NETWORK_TYPE_IWLAN){ return "IWLAN";}
        /** Current network is LTE_CA {@hide} */
//        @UnsupportedAppUsage
//        else if(networkType == TelephonyManager.NETWORK_TYPE_LTE_CA){ return "";}
        /** Current network is NR(New Radio) 5G. */
        else if(networkType == TelephonyManager.NETWORK_TYPE_NR){ return "NR";}
        else return "";
    }

    public static int getCqi(String SINR){
        double sinr = Double.parseDouble(SINR);
        if(sinr >= -6.7 && sinr < -4.7)return 1;
        if(sinr >= -4.7	&& sinr < -2.3)return 2;
        if(sinr >= -2.3	&& sinr < 0.2)return 3;
        if(sinr >= 0.2 && sinr < 2.4)return 4;
        if(sinr >= 2.4 && sinr < 4.3)return 5;
        if(sinr >= 4.3 && sinr < 5.9)return 6;
        if(sinr >= 5.9 && sinr < 8.1)return 7;
        if(sinr >= 8.1 && sinr < 10.3)return 8;
        if(sinr >= 10.3 && sinr < 11.7)return 9;
        if(sinr >= 11.7 && sinr < 14.1)return 10;
        if(sinr >= 14.1 && sinr < 16.3)return 11;
        if(sinr >= 16.3 && sinr < 18.7)return 12;
        if(sinr >= 18.7 && sinr < 21.0)return 13;
        if(sinr >= 21.0 && sinr < 22.7)return 14;
        if(sinr >= 22.7 && sinr < -4.7)return 15;
        return 0;
    }

    public static String foreGroundApp(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);

        String foregroundTaskPackageName = foregroundTaskInfo .topActivity.getPackageName();
        return foregroundTaskPackageName;
//        PackageManager pm = context.getPackageManager();
//        PackageInfo foregroundAppPackageInfo = null;
//        String foregroundTaskAppName = null;
//        try {
//            foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
//            foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            return  foregroundTaskPackageName;
//        }
//        return foregroundTaskAppName==null?"":foregroundTaskAppName;
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static Drawable getPackageIcon(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getApplicationIcon(packageName);
        }
        catch (PackageManager.NameNotFoundException e) {
            return context.getDrawable(R.drawable.ic_not_installed);
        }
    }

    public static String getPackageName(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getNameForUid(getPackageUid(context,packageName));
        }
        catch (Exception e) {
            return "";
        }
    }

    public static int getPackageUid(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getApplicationInfo(packageName, 0).uid;
        }
        catch (Exception e) {
            return 0;
        }
    }

    public static String getPackageInfo(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getApplicationInfo(packageName, 0).taskAffinity;
        }
        catch (Exception e) {
            return "-";
        }
    }

    public static String getPackageFromUid(Context context, int uid) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getNameForUid(uid);
        }
        catch (Exception e) {
            return "-";
        }
    }

    public static void showNotification(Context context, String title, String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);//to show content in lock screen
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

    public static Long getPackageBytesReceived(Context context, String packageName){
//        packageName = packageName.equals(Constant.PACKAGE_YOUTUBE2)?"android.uid.system:1000":packageName;
        int localUid;
        PackageManager packageManager = context.getPackageManager();
        try {
            localUid = packageManager.getApplicationInfo(packageName, 0).uid;
        }
        catch (Exception e) {
            localUid = 0;
        }
        Long usg = TrafficStats.getTotalRxBytes();
//        logPrint(Utils.class,"usage", usg+"");
        return usg;
        /**File dir = new File("/proc/net/");///proc/uid_stat/
        if(packageName.equals(Constant.DEFAULT_APP)){
            return readFrom_proc_net_dev();
        }
        String[] children = dir.list();
        if(!Arrays.asList(children).contains(String.valueOf(localUid))){
            logPrint(Utils.class,"uid "+localUid, "not exist");
            return r eadFrom_proc_net_dev();
        }
        File uidFileDir = new File("/proc/uid_stat/"+String.valueOf(localUid));
        File uidActualFileReceived = new File(uidFileDir,"tcp_rcv");
        File uidActualFileSent = new File(uidFileDir,"tcp_snd");

        String textReceived = "0";
        String textSent = "0";

        try {
            BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
            BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
            String receivedLine;
            String sentLine;

            if ((receivedLine = brReceived.readLine()) != null) {
                textReceived = receivedLine;
            }
            if ((sentLine = brSent.readLine()) != null) {
                textSent = sentLine;
            }

        }
        catch (IOException e) {
            logPrint(Utils.class,"uid error "+localUid, Log.getStackTraceString(e));
        }
        logPrint(Utils.class,"uid usage "+localUid, Long.valueOf(textReceived).longValue()+"");
        return Long.valueOf(textReceived).longValue();**/
    }

    private static Long readFrom_proc_net_dev() {
        BufferedReader reader;
        String line;
        String[] values;
        long totalBytes = 0;//rx,tx
        try {
            reader = new BufferedReader(new FileReader("/proc/net/dev"));

            while ((line = reader.readLine()) != null) {
                if (line.contains("eth") || line.contains("wlan")){
                    values = line.trim().split("\\s+");
                    totalBytes = Long.parseLong(values[1]);//rx
//                    totalBytes[1] +=Long.parseLong(values[9]);//tx
                }
            }
            reader.close();
            logPrint(Utils.class,"/proc/net/dev", totalBytes+"");
        }
        catch (Exception e) {
            logPrint(Utils.class,"/proc/net/dev", "not found");
        }
        //transfer to kb
//        totalBytes[0] =  totalBytes[0] / 1024;
//        totalBytes[1] =  totalBytes[1] / 1024;

        return totalBytes;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static Long getFromNetworkStats(Context context, int packageUid) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int type = connectivityManager.getActiveNetworkInfo().getType();
        if(type == ConnectivityManager.TYPE_WIFI){
            Utils.logPrint(Utils.class,"network type", "TYPE_WIFI");
        }else {
            Utils.logPrint(Utils.class,"network type", "TYPE_MOBILE");
        }
        String subscriberId = getSubscriberId(context, type);
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    type,
                    subscriberId,
                    0,
                    System.currentTimeMillis(),
                    packageUid);
        } catch (Exception e) {
            Utils.logPrint(Utils.class,"network error", Log.getStackTraceString(e));
            return 0L;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        long usage = bucket.getRxBytes();
        Utils.logPrint(Utils.class,"network values", usage + ":" + subscriberId + ":" + type);
        return usage;
    }

    @SuppressLint("MissingPermission")
    private static String getSubscriberId(Context context, int networkType) {
        if (networkType == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        }
        return "";
    }

    public static void killPackage(Context context, String packageName){
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
//        activityManager.killBackgroundProcesses(packageName);

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Method method = null;
        try {
            method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(am, packageName);
        } catch (Exception e) {
            Log.println(Log.ASSERT, "killPackage",Log.getStackTraceString(e));
        }
    }

    public static boolean isDisplayPotrait(Context context){
//        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
//        int orientation = windowManager.getDefaultDisplay().getOrientation();
//        return orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
//                orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT ||
//                orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT ||
//                orientation == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int min = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        if(min == displayMetrics.widthPixels){
            return true;
        }
        return false;
    }

    public static void setDisplayLandscape(Activity activity){
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
    }

    public static String getCentricSpn(String networkOperatorName) {
        String spn = networkOperatorName;
        if(networkOperatorName.toUpperCase().contains("AIRTEL")){
            spn = "Airtel";
        } else if(networkOperatorName.toUpperCase().contains("JIO")){
            spn = "Jio";
        }
        return spn;
    }

    private void changeScreenOrientation(Context context) {
//        if (Settings.System.getInt(context.getContentResolver(),
//                Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//                }
//            }, 4000);
//        }
    }

    public static String onScreenActivity(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);

        String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
        return foregroundTaskPackageName;
//        PackageManager pm = context.getPackageManager();
//        PackageInfo foregroundAppPackageInfo = null;
//        String foregroundTaskAppName = null;
//        try {
//            foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
//            foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            return  foregroundTaskPackageName;
//        }
//        return foregroundTaskAppName==null?"":foregroundTaskAppName;
    }

    public static void clearCache(String packageName) {
        String inputLine = "";
        try {
            String command = "pm clear "+packageName;
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command);
            Utils.logPrint(Utils.class,"command",command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            inputLine = bufferedReader.readLine();
            while ((inputLine != null)) {
                Utils.logPrint(Utils.class,"inputline",inputLine);
                inputLine = bufferedReader.readLine();
            }
            logPrint(Utils.class,"cleared",packageName);
        } catch (Exception e) {
            logPrint(Utils.class,"error clearing cache",Log.getStackTraceString(e));
        }
    }

//    public static void clearCache(Context context, String packageName){
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
//        activityManager.clearApplicationUserData();
//    }

    public static String a(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int min = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        if (min >= 2160) {
            return "UHD";
        }
        if (min >= 1080) {
            return "FHD";
        }
        return min >= 720 ? "HD" : "SD";
    }

    public static Rect b(Context context){
        WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Rect rect = new Rect();
        w.getDefaultDisplay().getRectSize(rect);
//        logPrint(Utils.class,"bottom",rect.bottom+"");
//        logPrint(Utils.class,"left",rect.left+"");
//        logPrint(Utils.class,"right",rect.right+"");
//        logPrint(Utils.class,"top",rect.top+"");
        Rect rect1 = new Rect(0,0,rect.bottom,rect.right);
        return rect1;
//         logPrint(Utils.class,"display",f+"="+f2);
    }

    public static boolean isWifiConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    public static SpannableString boldGreyText(Object text) {
        String text1 = text + "";
        SpannableString dataValue = new SpannableString(text1);
//        dataValue.setSpan(new RelativeSizeSpan(3.5f), 0, dataValue.length(), 0);
        dataValue.setSpan(new StyleSpan(Typeface.BOLD), 0, dataValue.length(), 0);
        dataValue.setSpan(new ForegroundColorSpan(ARapp.context.getResources().getColor(R.color.text_color)), 0, dataValue.length(), 0);

//        return dataValue.toString();
        return dataValue;
    }

    public static SpannableString boldText(Object text) {
        String text1 = text + "";
        SpannableString dataValue = new SpannableString(text1);
//        dataValue.setSpan(new RelativeSizeSpan(3.5f), 0, dataValue.length(), 0);
        dataValue.setSpan(new StyleSpan(Typeface.BOLD), 0, dataValue.length(), 0);
//        dataValue.setSpan(new ForegroundColorSpan(ARapp.context.getResources().getColor(R.color.text_color)), 0, dataValue.length(), 0);

//        return dataValue.toString();
        return dataValue;
    }

    public static double getLatency(){
        String ipAddress = "8.8.8.8";
        String pingCommand = "/system/bin/ping -c 1 "+ ipAddress;
        String inputLine;
        double avgRtt = 0.0;

        try {
            // execute the command on the environment interface
            Process process = Runtime.getRuntime().exec(pingCommand);
            // gets the input stream to get the output of the executed command
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            inputLine = bufferedReader.readLine();
            while ((inputLine != null)) {
                Utils.logPrint(Utils.class,"inputline",inputLine);
                if (inputLine.length() > 0 && inputLine.contains("time=")) {  // when we get to the last line of executed ping command
                    String ping = inputLine.substring(inputLine.lastIndexOf('=')+1,inputLine.lastIndexOf(' '));
                    Utils.logPrint(Utils.class,"ping",ping);
                    avgRtt = Double.parseDouble(ping);
                    break;
                }
                inputLine = bufferedReader.readLine();
            }
        } catch (IOException e){
            Utils.logPrint(Utils.class,"error ping", Log.getStackTraceString(e));
        }
        return avgRtt;
    }

    public static boolean isInternetConnectionStrong() {
        double THRESHOLD_PING = 500;
        Utils.logPrint(Utils.class, "checking", "latency");
        double latency = Utils.getLatency();
        Utils.logPrint(Utils.class, "latency", latency+" ms");
        return latency < THRESHOLD_PING;
    }
}

