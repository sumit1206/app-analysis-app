package rma.shivam.audiorecorder.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadPoolExecutor;

import rma.shivam.audiorecorder.ARapp;
import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.global.Values;
import rma.shivam.audiorecorder.local.CookiesAdapter;
import rma.shivam.audiorecorder.local.CookiesAttribute;

public class CsvUploadingService extends Service {
    public CsvUploadingService() {
    }

    private static Context context;
    private static int BOUND = 60;
    private static int COUNT = 0;
    private static int RESPONDED = 0;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.logPrint(getClass(), "CsvUploadingService", "triggered");
        init();
        readDirectory();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.logPrint(getClass(), "CsvUploadingService", "destroyed");
    }

    private void init() {
        context = this;
    }

    void readDirectory(){
        try {
            COUNT = 0;
            RESPONDED = 0;
            String basePath = ARapp.FILE_DIR + Values.SAVE_PATH;
            File folderDirectory = new File(basePath);//Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorded"
            folderDirectory.mkdirs();
            String[] files = folderDirectory.list();
            if(files == null || files.length == 0){
                stopSelf();
                return;
            }
            BOUND = Math.min(Constant.BOUND, files.length);
            for (String file : files) {
                if(COUNT == BOUND){
                    break;
                }
                Utils.logPrint(getClass(), "file", file);
                if(file.contains(Values.FILE_NAME)) {
                    File fileToUpload = new File(basePath + "/" + file);
                    if(fileToUpload.isFile() && !(fileToUpload.length() < 10)){
                        upload(fileToUpload);
                    }else {
                        if(fileToUpload.delete()){
                            updateStatus(fileToUpload, Constant.INVALID);
                            Utils.logPrint(getClass(),"empty file","deleted");
                        }else {
                            Utils.logPrint(getClass(),"empty file","not deleted");
                        }
                    }
                }
            }
            //get all the files from a directory
        }catch (Exception e){
            Log.println(Log.ASSERT,"ERROR",Log.getStackTraceString(e));
        }
    }

    private void upload(final File fileToUpload) {
        COUNT ++;
        Utils.logPrint(getClass(), "upload", fileToUpload.toString());
        UploadFile.FileSet fileSet = new UploadFile.FileSet("files", fileToUpload);
        UploadFile uploadFile = new UploadFile(Constant.CSV_URL,fileSet);
        uploadFile.setCallback(new UploadFile.Callback() {
            @Override
            void onResponse(String response){
                try {
                    if(response == null || response.equals("null")){
                        return;
                    }
                    Utils.logPrint(getClass(),"response22",response);
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("success");
                    if(status == 1){
                        updateStatus(fileToUpload, Constant.UPLOADED);
//                        fileToUpload.delete();
                        removeFile(fileToUpload);
                    }else {
                        updateStatus(fileToUpload, Constant.UPLOADING_FAILED);
                    }
                } catch (Exception e) {
                    Utils.logPrint(getClass(), "JSONException", Log.getStackTraceString(e));
                    updateStatus(fileToUpload, Constant.NOT_UPLOADED);
                }
                onResponded();
            }

            @Override
            void onErrorResponse(Exception e) {
                Utils.logPrint(getClass(), "error",Log.getStackTraceString(e));
                updateStatus(fileToUpload, Constant.NOT_UPLOADED);
                onResponded();
            }

            @Override
            void onConnectionError(int errorCode) {
                super.onConnectionError(errorCode);
                updateStatus(fileToUpload, Constant.NOT_UPLOADED);
                onResponded();
            }

            @Override
            void onInternalError(int errorCode) {
                super.onInternalError(errorCode);
                updateStatus(fileToUpload, Constant.NOT_UPLOADED);
                onResponded();
            }
        });
        updateStatus(fileToUpload, Constant.UPLOADING);
        uploadFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void onResponded(){
        RESPONDED++;
        Utils.logPrint(getClass(),"CSV Responded: ",RESPONDED + " of " + BOUND);
        if(RESPONDED == BOUND){
            readDirectory();
        }
    }

    private void updateStatus(File file, String status){
        try {
            String sessionId = file.getName().split("_")[0];
            CookiesAdapter cookiesAdapter = new CookiesAdapter(context);
            cookiesAdapter.openWritable();
            String prevStatus = cookiesAdapter.getFromHistory(sessionId, CookiesAttribute.history_csv_uploaded);
            if(prevStatus != null) {
                if (!prevStatus.equals(Constant.UPLOADED)) {
                    cookiesAdapter.updateHistory(CookiesAttribute.history_csv_uploaded, status, sessionId);
                    Utils.showNotification(context, status, sessionId + " Log");
                }
            }else {
                cookiesAdapter.updateHistory(CookiesAttribute.history_csv_uploaded, status, sessionId);
                Utils.showNotification(context, status, sessionId + " Log");
            }
            cookiesAdapter.close();
            sendBroadcast(Constant.ACTION_UPLOAD_TRIGGER, Constant.ACTION_UPLOAD_TRIGGER);
        }catch (Exception ignored){}
    }

    private void removeFile(File fileToRemove){
        String basePath = ARapp.FILE_DIR + Values.BACKUP_PATH;
        File baseFolder = new File(basePath);
        if(!baseFolder.exists()){
            Utils.logPrint(getClass(),"basefolder","not exist");
            if(baseFolder.mkdir()){
                Utils.logPrint(getClass(),"basefolder","created");
            }else {
                Utils.logPrint(getClass(),"basefolder","not created");
            }
        }else {
            Utils.logPrint(getClass(),"basefolder","exist");
        }
        File to = new File(basePath+"/"+fileToRemove.getName());
        fileToRemove.renameTo(to);
        if(fileToRemove.renameTo(to)){
            Utils.logPrint(getClass(),"renamed",to.toString());
        }else{
            Utils.logPrint(getClass(),"not renamed",to.toString());
        }
    }

    void sendBroadcast(String key, String message){
        Intent intent = new Intent(Constant.ACTION_UPLOAD_TRIGGER);    //action: "msg"
        intent.setPackage(getPackageName());
        intent.putExtra(key, message);
        getApplicationContext().sendBroadcast(intent);
        Utils.logPrint(getClass(),key, message);
    }
}
