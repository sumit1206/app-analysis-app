package rma.shivam.audiorecorder;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.global.Values;
import rma.shivam.audiorecorder.local.CookiesAdapter;
import rma.shivam.audiorecorder.local.CookiesAttribute;
import rma.shivam.audiorecorder.model.AutoStartHandler;
import rma.shivam.audiorecorder.services.AudioUploadingService;
import rma.shivam.audiorecorder.services.ConnectivityStatusReceiver;
import rma.shivam.audiorecorder.services.CsvUploadingService;
import rma.shivam.audiorecorder.services.UpdateAppDataCsv;

public class ARapp extends Application {

    public static Context context;
    static FileOutputStream fileOutputStream;
    static OutputStreamWriter outputStreamWriter;

    public static File STORAGE_DIR;

    public static String FILE_DIR;
    public static String MAKE = Values.NULL_VALUE;
    public static String MODEL = Values.NULL_VALUE;
    public static String OS = Values.NULL_VALUE;
    public static String appVersion = Values.NULL_VALUE;
    public static String IMEI = Values.NULL_VALUE;


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        MAKE = Build.MANUFACTURER;
        MODEL = Build.MODEL;
        OS = Build.VERSION.RELEASE;
        appVersion = Utils.getAppVersion(context);
        IMEI = Utils.getImei(context);
        FILE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
        ContextWrapper cw = new ContextWrapper(this);
        STORAGE_DIR = cw.getDir(context.getPackageName(), Context.MODE_PRIVATE);
        if (!STORAGE_DIR.exists()) {
            try {
                STORAGE_DIR.mkdir();
                Log.println(Log.ASSERT, "ARapp","directory created");
            }catch (Exception e){
                Log.println(Log.ASSERT, "ARapp","error creating directory: "+Log.getStackTraceString(e));
            }
        }else{
            Log.println(Log.ASSERT, "ARapp","directory exists");
        }

        createCookiesDb();
        createAppDataCsv();
        createBackupDrive();
//        autoUpload();
        registerReceiver(new ConnectivityStatusReceiver(), new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        AutoStartHandler.retriveClass();
    }

    private void createCookiesDb() {
        CookiesAdapter cookiesAdapter = new CookiesAdapter(context);
        cookiesAdapter.createDatabase();
    }

    private void createAppDataCsv() {
        new UpdateAppDataCsv().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void autoUpload() {
        context.startService(new Intent(context, CsvUploadingService.class));
        context.startService(new Intent(context, AudioUploadingService.class));
    }

//    void createAppDataCsv(){
//        String path = STORAGE_DIR + "";
//        String fileName = Constant.FILE_NAME_APP_LIST;
//        File dir = new File(path, Constant.SAVE_PATH_CACHE_DUMP);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//        File file = new File(path +"/"+ Constant.SAVE_PATH_CACHE_DUMP+"/"+ fileName);
//        try {
//            if (!file.exists()) {
//                file.createNewFile();
//                String data = ""+
//                        //NAME_______________________________PACKAGE____________________________________URI
//                        "Rakuten TV" + Constant.DELIMETER + "tv.wuaki" + Constant.DELIMETER + "https://rakuten.tv/uk/streams/movie/in-the-name-of-the-king-a-dungeon-siege-tale/trailer" + Constant.LINE_SEPARATOR +
//                        "GYAO!" + Constant.DELIMETER + "jp.co.yahoo.gyao.android.app" + Constant.DELIMETER + "https://gyao.yahoo.co.jp/player/00486/v12534/v1000000000000014970/" + Constant.LINE_SEPARATOR +
//                        "Prime Video" + Constant.DELIMETER + "com.amazon.avod.thirdpartyclient" + Constant.DELIMETER + "https://app.primevideo.com/detail?gti=amzn1.dv.gti.96b78c9f-5f62-e1bd-647d-2293b8ff1cd7&ref_=atv_dp_share_mv&r=web" + Constant.LINE_SEPARATOR +
//                        "YouTube" + Constant.DELIMETER + "com.google.android.youtube" + Constant.DELIMETER + "https://www.youtube.com/watch?v=VxPaQmvVMUU" + Constant.LINE_SEPARATOR +
//                        "YouTube2" + Constant.DELIMETER + "rma.shivam.youtubeapplication" + Constant.DELIMETER + "https://www.youtube.com/watch?v=3wkPgFrM36o" + Constant.LINE_SEPARATOR +
//                        "ZEE5" + Constant.DELIMETER + "com.graymatrix.did" + Constant.DELIMETER + "https://www.zee5.com/tvshows/details/kundali-bhagya-february-26-2020/0-6-366/kundali-bhagya-february-26-2020/0-1-manual_42oev3itabg0" + Constant.LINE_SEPARATOR +
//                        "Airtel Xstream" + Constant.DELIMETER + "tv.accedo.airtel.wynk" + Constant.DELIMETER + "https://content.airtel.tv/s/q7u3eCvNQHs2EwhF" + Constant.LINE_SEPARATOR +
//                        "Jio Cinema" + Constant.DELIMETER + "com.jio.media.ondemand" + Constant.DELIMETER + "http://tinyurl.com/ujmoun2" + Constant.LINE_SEPARATOR +
//                        "SonyLIV" + Constant.DELIMETER + "com.sonyliv" + Constant.DELIMETER + "https://sonyliv.app.link/SKXXMHGix3" + Constant.LINE_SEPARATOR +
//                        "Tubi TV" + Constant.DELIMETER + "com.tubitv" + Constant.DELIMETER + "https://tubitv.com/movies/464400/a_turtles_tale_2_sammys_escape_from_paradise" + Constant.LINE_SEPARATOR +
//                        "Hotstar" + Constant.DELIMETER + "in.startv.hotstar" + Constant.DELIMETER + "https://www.hotstar.com/1000235906" + Constant.LINE_SEPARATOR;
//
//                fileOutputStream = new FileOutputStream(file, true);
//                outputStreamWriter = new OutputStreamWriter(fileOutputStream);
//                outputStreamWriter.write(data);
//                fileOutputStream.flush();
//                outputStreamWriter.flush();
//                outputStreamWriter.close();
//                fileOutputStream.close();
//            }
//        } catch (Exception e) {
//            Log.println(Log.ASSERT,"Error creating csv",Log.getStackTraceString(e));
//        }
//    }

    private void createBackupDrive() {
        String basePath = ARapp.FILE_DIR;
        File file = new File(basePath, "rma_recorded_backup");
        if(!file.exists()){
            Utils.logPrint(getClass(),"Backup_folder","not exist");
            if(file.mkdir()){
                Utils.logPrint(getClass(),"Backup_folder","created");
                String basePath2 = ARapp.FILE_DIR + Constant.RECORD_BACKUP_PATH;
                File file2 = new File(basePath2, "rma_passive_data");
                if(!file2.exists()){
                    Utils.logPrint(getClass(),"inner_Backup_folder","not exist");
                    if(file2.mkdirs()){
                        Utils.logPrint(getClass(),"inner_Backup_folder","created");
                    }else {
                        Utils.logPrint(getClass(),"inner_Backup_folder","not created");
                    }
                }else {
                    Utils.logPrint(getClass(),"inner_Backup_folder","not created");
                }
            }else {
                Utils.logPrint(getClass(),"Backup_folder","not created");
            }
        }else {
            Utils.logPrint(getClass(),"Backup_folder","exist");
        }

    }

}
