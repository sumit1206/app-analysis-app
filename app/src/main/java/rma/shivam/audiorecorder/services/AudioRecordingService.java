package rma.shivam.audiorecorder.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;

import java.io.File;
import java.util.ArrayList;

import rma.shivam.audiorecorder.ARapp;
import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.local.CookiesAdapter;
import rma.shivam.audiorecorder.local.CookiesAttribute;
import rma.shivam.audiorecorder.model.AutoStartHandler;
import rma.shivam.audiorecorder.helper.Hashids;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.global.Values;
import rma.shivam.audiorecorder.model.DataUsageStat;
import rma.shivam.audiorecorder.model.History;
import rma.shivam.audiorecorder.ui.MainActivity;

public class AudioRecordingService extends Service {
    public AudioRecordingService() {
    }

    Context context;
    public static String SESSION = Values.NULL_VALUE;
    private static final int ONE_SECOND = 1000;
    public static long THREE_MIN = 60 * 3;//10;//
    private static final int THROUGHPUT_REFRESH_RATE = 10;
    private static ArrayList<DataUsageStat> dataUsageStats = null;
    static AudioManager audioManager;
    private static MediaRecorder mRecorder;
    private static Intent backgroundService;
    public static Intent jitterService;
    public static boolean isRecording = false;
    public static boolean processRunning = false;
    private static long startTime = 0;
    private static int counter = 0;
    private static String appToRecord;
    public static int SPEAKER_STATE = 0;
    private static boolean killProcess = Constant.DEFAULT_KILL_PROCESS;

    private static long prevDownByte = 0;
//    private static long prevDownByteForIniBuff = 0;
    ArrayList<DataUsageStat> duses = new ArrayList<>();

    public static long appInitiationTime = 0;
    public static long packageLoadTime = 0;
    public static long firstByteRecTime = 0;
    private static long videoPayingTime = 0;
    public static long initialBufferTime = 0;

    private static long initialAppDataUsage = 0;
    public static long appDataUsage = 0;
    public static String appToRecordName;
    public static double throughput = 0;

    Handler timeHandler;
    Runnable timeHandlerTask;
    Handler loopHandler;
    Runnable loopHandlerTask;

    private static Intent floatingService;
    BroadcastReceiver floatingActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopWholeService();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processRunning = true;
        resetValues();
        init();
        createSession();
        createAutoStartSession();
        loopHandlerTask.run();
        registerFloatingCallback();
        startService(floatingService);
        startService(backgroundService);
//        startService(jitterService);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            processRunning = false;
            stopRecording();
            stopService(backgroundService);
            stopService(floatingService);
            unregisterFloatingCallback();
            loopHandler.removeCallbacks(loopHandlerTask);
            timeHandler.removeCallbacks(timeHandlerTask);
            Utils.killPackage(context, appToRecord);
            Utils.clearCache(appToRecord);
            handleAutoStart();
        }catch (Exception ignored){}
    }

    private void registerFloatingCallback() {
        IntentFilter floatingActionFilter = new IntentFilter(Constant.ACTION_FLOATING_BUTTON_CLICK);
        registerReceiver(floatingActionReceiver, floatingActionFilter);
    }

    private void unregisterFloatingCallback() {
        unregisterReceiver(floatingActionReceiver);
    }

    private void createAutoStartSession() {
        if(AutoStartHandler.isActivated){
            if(AutoStartHandler.currentIteration == 0 && AutoStartHandler.runningAppNo == 0){
                AutoStartHandler.createSession();
            }
        }
    }

    private void handleAutoStart() {
        if(AutoStartHandler.isActivated){
            Utils.logPrint(getClass(),"AutoStartHandler:currentIteration", String.valueOf(AutoStartHandler.currentIteration));
            Utils.logPrint(getClass(),"AutoStartHandler:runningAppNo", String.valueOf(AutoStartHandler.runningAppNo));
            if(AutoStartHandler.runningAppNo == AutoStartHandler.appCount-1){
                AutoStartHandler.currentIteration++;
                AutoStartHandler.runningAppNo = 0;
            }else{
                AutoStartHandler.runningAppNo = AutoStartHandler.runningAppNo + 1;
            }
            if(AutoStartHandler.currentIteration >= AutoStartHandler.iteration){
                AutoStartHandler.resetSession();
                AutoStartHandler.currentIteration = 0;
                sendBroadcast(Constant.KEY_RECORDING_CALLBACK, Constant.CODE_ITERATION_STOPPED);
                return;
            }
            int appToStart = AutoStartHandler.runningAppNo;
            String appUri = AutoStartHandler.appUris.get(appToStart);
            Utils.saveString(context, Constant.KEY_APP_TO_RECORD_NAME, AutoStartHandler.appNames.get(appToStart));
            Utils.saveString(context, Constant.KEY_APP_TO_RECORD_PACKAGE, AutoStartHandler.packages.get(appToStart));
            Utils.saveString(context, Constant.KEY_INTENT_URI, appUri);
            Utils.logPrint(getClass(),"appUri to S.Pref", appUri);
            sendBroadcast(Constant.KEY_RECORDING_START_NEXT,AutoStartHandler.packages.get(appToStart) +
                    Values.NULL_VALUE + AutoStartHandler.appNames.get(appToStart));
            Utils.logPrint(getClass(),"AutoStartHandler:appToStart", String.valueOf(appToStart));
        }
    }

    private void resetValues() {
        dataUsageStats =new ArrayList<>();
        startTime = 0;
        counter = 0;
        prevDownByte = 0;
//        prevDownByteForIniBuff = 0;
        appInitiationTime = 0;
        packageLoadTime = 0;
        firstByteRecTime = 0;
        videoPayingTime = 0;
        initialBufferTime = 0;
        throughput = 0;
        initialAppDataUsage = 0;
    }

    private void init(){
        context = this;
        THREE_MIN = Utils.getLong(context, Constant.KEY_PLAYBACK_TIME, THREE_MIN);
        floatingService = new Intent(context, FloatingViewService.class);
        jitterService = new Intent(this, JitterService.class);
        backgroundService = new Intent(context, BackgroundRunner.class);
        killProcess = Utils.getBoolean(context, Constant.KEY_KILL_PROCESS, Constant.DEFAULT_KILL_PROCESS);
        appToRecord = Utils.getString(context, Constant.KEY_APP_TO_RECORD_PACKAGE, Constant.DEFAULT_APP);
        appToRecordName = Utils.getString(context, Constant.KEY_APP_TO_RECORD_NAME, Constant.DEFAULT_APP_NAME);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRecorder = new MediaRecorder();
        initialAppDataUsage = Utils.getPackageBytesReceived(context, appToRecord);
        timeHandler = new Handler();
        timeHandlerTask = new Runnable() {
            @Override
            public void run() {
                long duration = (System.currentTimeMillis() - startTime)/ONE_SECOND;
                sendBroadcast(Constant.KEY_RECORDING_DURATION, String.valueOf(duration));
//                Utils.b(context);
                timeHandler.postDelayed(timeHandlerTask, ONE_SECOND);
                if(duration >= THREE_MIN){
                    Utils.killPackage(context,appToRecord);
                }
            }
        };
        loopHandler = new Handler();
        loopHandlerTask = new Runnable() {
            @Override
            public void run() {
                /**
                 * when audio playing starts
                 * todo start recording
                 * */
                SPEAKER_STATE = audioManager.isMusicActive()?1:0;
//                Utils.logPrint(getClass(),"SPEAKER_STATE", String.valueOf(SPEAKER_STATE));
                if(!isRecording && audioManager.isMusicActive()){
                    firstByteRecTime = DataUsageStat.getFirstBitRecTime(dataUsageStats);
                    videoPayingTime = System.currentTimeMillis();
                    if(firstByteRecTime != 0){
                        initialBufferTime = videoPayingTime - firstByteRecTime;
                        for(DataUsageStat dataUsageStat: dataUsageStats){
                            Utils.logPrint(getClass(),"DataUsageStat",dataUsageStat.toString());
                        }
                        Utils.logPrint(getClass(),"videoPayingTime", String.valueOf(videoPayingTime));
                        Utils.logPrint(getClass(),"initialBufferTime", String.valueOf(initialBufferTime));
                    }else {
                        initialBufferTime = 0;
                    }
                    if(appInitiationTime != 0 && firstByteRecTime != 0){
                        packageLoadTime = firstByteRecTime - appInitiationTime;
                    }else {
                        packageLoadTime = 0;
                    }
//                    if(Utils.isDisplayPotrait(context)){
//                        Utils.logPrint(getClass(),"display","potrait");
//                        Utils.setDisplayLandscape(MainActivity.activity);
//                    }else {
//                        Utils.logPrint(getClass(),"display","lanndscape");
//                    }
                    startRecording();
                }
                /**
                 * when app to record removes from foreground
                 * todo stop recording
                 * */
                if(!isRecording && Utils.foreGroundApp(context).equals(appToRecord)){
                    if(appInitiationTime == 0){
                        appInitiationTime = System.currentTimeMillis();
                    }
                }
                if(isRecording && !Utils.foreGroundApp(context).equals(appToRecord)){
                    stopSelf();
                }
                /**
                 * encounters after every 1 second
                 * todo calculate throughput
                 * */
                if(counter % 10 == 0) {
                    long downbyte = Utils.getPackageBytesReceived(context, appToRecord);
                    if (prevDownByte != 0) {
                        long d_byte = downbyte - prevDownByte;
                        throughput = Double.valueOf(d_byte);
                        throughput = (throughput) / 1024;
                        dataUsageStats.add(new DataUsageStat(System.currentTimeMillis(),throughput));
                    }
                    prevDownByte = downbyte;
                }
                /**
                 * todo first byte receive time
                 * */
                long downByte = Utils.getPackageBytesReceived(context, appToRecord);
                appDataUsage = downByte - initialAppDataUsage;
                /**todo firstByteRecTime calculation algorithm changed
                 * if (prevDownByteForIniBuff != 0 && firstByteRecTime == 0) {
                    long d_byte = downByte - prevDownByte;
                    if(d_byte > 0.0){
                        firstByteRecTime = System.currentTimeMillis();
                        Utils.logPrint(getClass(),"check firstByteRecTime", String.valueOf(firstByteRecTime));
                    }
                }
                prevDownByteForIniBuff = downByte;
                 */
                counter++;
                loopHandler.postDelayed(loopHandlerTask, ONE_SECOND / THROUGHPUT_REFRESH_RATE);
            }
        };
    }

    private void createSession(){
        long now = System.currentTimeMillis();
        SESSION = Hashids.hash12(ARapp.IMEI+now+appToRecord);
    }

    private void startRecording() {
        if(isRecording) {
            sendBroadcast(Constant.KEY_RECORDING_CALLBACK, "Recording ongoing");
            return;
        }
        try {
            String savePath = ARapp.FILE_DIR;
            savePath += Constant.RECORD_PATH;
            File file = new File(savePath);
            if (!file.exists()) {
                file.mkdir();
            }
            startTime = System.currentTimeMillis();
            savePath += "/"+ startTime + "_" + ARapp.IMEI + "_" + SESSION + AutoStartHandler.session + Constant.RECORD_PREFIX + appToRecordName + Constant.RECORD_RORMAT;

            mRecorder.setAudioSource(MediaRecorder.AudioSource.REMOTE_SUBMIX);
            Utils.logPrint(getClass(),"source" ,"REMOTE_SUBMIX");
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(savePath);
            mRecorder.prepare();
            mRecorder.start();
            timeHandlerTask.run();
            isRecording = true;
            sendBroadcast(Constant.KEY_RECORDING_CALLBACK, "Recording started");
//            updateStatus(SESSION);
            createHistory();
            Utils.logPrint(getClass(),"Recording started",savePath);
        } catch (Exception ex) {
            Utils.logPrint(getClass(),"Recording failed",Log.getStackTraceString(ex));
            ex.printStackTrace();
            sendBroadcast(Constant.KEY_RECORDING_CALLBACK, Constant.CODE_EXIT);
            sendBroadcast(Constant.KEY_RECORDING_CALLBACK, "Recording failed");
        }
    }

    private void stopRecording(){
        if (isRecording) {
            isRecording = false;
            mRecorder.stop();
            mRecorder.release();
            Utils.logPrint(getClass(),"Recording","stopped");
            sendBroadcast(Constant.KEY_RECORDING_CALLBACK, "Recording stopped");
            return;
        }
        Utils.logPrint(getClass(),"Recording","not started");
        sendBroadcast(Constant.KEY_RECORDING_CALLBACK, "Recording not started yet");
    }

    void sendBroadcast(String key, String message){
        Intent intent = new Intent(Constant.ACTION_RECORDING);    //action: "msg"
        intent.setPackage(getPackageName());
        intent.putExtra(key, message);
        getApplicationContext().sendBroadcast(intent);
        Utils.logPrint(getClass(),"broadcast " + key, message);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
//        if(killProcess && processRunning) {
//            try {
//                stopSelf();
//            }catch (Exception ignored){}
//        }
        stopWholeService();
    }

//    private void updateStatus(String sessionId){
//        try {
//            Utils.logPrint(getClass(), "CHANGING", sessionId);
//            CookiesAdapter cookiesAdapter = new CookiesAdapter(context);
//            cookiesAdapter.openWritable();
//            cookiesAdapter.updateHistory(CookiesAttribute.history_audio_uploaded, Constant.NOT_UPLOADED, sessionId);
//            cookiesAdapter.close();
//            cookiesAdapter.openReadable();
//            String newState = cookiesAdapter.getFromHistory(sessionId, CookiesAttribute.history_audio_uploaded);
//            Utils.logPrint(getClass(), "CHANGING NEW STATE", newState );
//            cookiesAdapter.close();
//        }catch (Exception ex){
//            Utils.logPrint(getClass(), "CHANGING ERROR", Log.getStackTraceString(ex));
//
//        }
//    }

    private void createHistory() {
        long now = System.currentTimeMillis();
        History history = new History(String.valueOf(now),AudioRecordingService.SESSION, AutoStartHandler.session,
                AudioRecordingService.appToRecordName, Constant.NOT_UPLOADED, Constant.NOT_UPLOADED);
        CookiesAdapter cookiesAdapter = new CookiesAdapter(context);
        cookiesAdapter.openWritable();
        cookiesAdapter.addHisory(history);
        cookiesAdapter.close();
    }



    public void stopWholeService() {
        Utils.logPrint(getClass(),"stopWholeService", "triggered");
        Utils.killPackage(context,appToRecord);
        AutoStartHandler.runningAppNo = AutoStartHandler.appCount - 1;
        AutoStartHandler.currentIteration = AutoStartHandler.iteration - 1;
        if(!isRecording) {
            stopSelf();
        }
    }
}
