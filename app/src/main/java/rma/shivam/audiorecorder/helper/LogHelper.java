package rma.shivam.audiorecorder.helper;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import rma.shivam.audiorecorder.ARapp;
import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.global.Values;
import rma.shivam.audiorecorder.model.AutoStartHandler;
import rma.shivam.audiorecorder.services.AudioRecordingService;
import rma.shivam.audiorecorder.services.BackgroundRunner;
import rma.shivam.audiorecorder.services.JitterService;

public class LogHelper {

    private static FileOutputStream fileOutputStream;
    private static OutputStreamWriter outputStreamWriter;

    public static void createCsv(){
        String path = ARapp.FILE_DIR;//PRCApplication.STORAGE_DIR + "";//
        String fileName = AudioRecordingService.SESSION + "_" + AudioRecordingService.appToRecordName+ Values.FILE_NAME;
        String indexLine = "MAKE" + Values.DELIMETER +
                "MODEL" + Values.DELIMETER +
                "OS" + Values.DELIMETER +
                "appVersion" + Values.DELIMETER +
                "IMEI" + Values.DELIMETER +
                "TIMESTAMP" + Values.DELIMETER +
                "LAT" + Values.DELIMETER +
                "LON" + Values.DELIMETER +
                "ACCURACY" + Values.DELIMETER +
                "TECH" + Values.DELIMETER +
                "SUB_TECH" + Values.DELIMETER +
                "ASU" + Values.DELIMETER +
                "RSRP" + Values.DELIMETER +
                "RSCP" + Values.DELIMETER +
                "RX_LEVEL" + Values.DELIMETER +
                "RSRQ" + Values.DELIMETER +
                "ECIO" + Values.DELIMETER +
                "RX_QUAL" + Values.DELIMETER +
                "EARFCN" + Values.DELIMETER +
                "UARFCN" + Values.DELIMETER +
                "ARFCN" + Values.DELIMETER +
                "SINR" + Values.DELIMETER +
                "MCC" + Values.DELIMETER +
                "MNC" + Values.DELIMETER +
                "LAC_TAC" + Values.DELIMETER +
                "CELL_ID" + Values.DELIMETER +
                "PSC_PCI" + Values.DELIMETER +
                "SPN" + Values.DELIMETER +
                "DATA_STATE" + Values.DELIMETER +
                "SERVICE_STATE" + Values.DELIMETER +
                "RNC" + Values.DELIMETER +
                "CQI" + Values.DELIMETER +
                "FREQ" + Values.DELIMETER +
                "BAND" + Values.DELIMETER +
                "TA" + Values.DELIMETER +
                "CALL_STATE" + Values.DELIMETER +
                "CALL_DURATION" + Values.DELIMETER +
                "TEST_STATE" + Values.DELIMETER +
                "RSSI" + Values.DELIMETER +
                "SS" + Values.DELIMETER +
                "RECORDING" + Values.DELIMETER +
                "RUNNING APP" + Values.DELIMETER +
                "INITIAL BUFFER TIME" + Values.DELIMETER +
                "THROUGHPUT" + Values.DELIMETER +
                "SESSION" + Values.DELIMETER +
                "FIRST BIT REC TIME" + Values.DELIMETER +
                "JITTER" + Values.DELIMETER +
                "DATE TIME" + Values.DELIMETER +
                "APP DATA USAGE" + Values.DELIMETER +
                "APP ON SCREEN TIME" + Values.DELIMETER +//wifi_state`, `wifi_ssid`, `wifi_rssi`, `wifi_ip`, `wifi_freq`, `wifi_link_speed`
                "PACKAGE LOAD TIME" + Values.DELIMETER +
                "CIRCLE SESSION" + Values.DELIMETER +
                "WIFI STATE" + Values.DELIMETER +
                "WIFI SSID" + Values.DELIMETER +
                "WIFI RSSI" + Values.DELIMETER +
                "WIFI IP" + Values.DELIMETER +
                "WIFI FREQ" + Values.DELIMETER +
                "WIFI LINK SPEED" + Values.DELIMETER +
//                "LAT2" + Values.DELIMETER +
//                "LON2" + Values.DELIMETER +
//                "PROVIDER" + Values.LINE_SEPARATOR;
                "CITY" + Values.LINE_SEPARATOR;
        File dir = new File(path, Values.SAVE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path +"/"+Values.SAVE_PATH+"/"+ fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file, true);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(indexLine);
            Utils.logPrint(LogHelper.class,"new_csv_created",fileName);
        } catch (Exception e) {
            Log.println(Log.ASSERT,"Error creating csv",Log.getStackTraceString(e));
        }
    }

    public static void appendToCsv(){
        String data =
                ARapp.MAKE + Values.DELIMETER +
                ARapp.MODEL + Values.DELIMETER +
                ARapp.OS + Values.DELIMETER +
                ARapp.appVersion + Values.DELIMETER +
                ARapp.IMEI + Values.DELIMETER +
                BackgroundRunner.TIMESTAMP + Values.DELIMETER +
                BackgroundRunner.LAT + Values.DELIMETER +
                BackgroundRunner.LON + Values.DELIMETER +
                BackgroundRunner.ACCURACY + Values.DELIMETER +
                BackgroundRunner.TECH + Values.DELIMETER +
                BackgroundRunner.SUB_TECH + Values.DELIMETER +
                BackgroundRunner.ASU + Values.DELIMETER +
                BackgroundRunner.RSRP + Values.DELIMETER +
                BackgroundRunner.RSCP + Values.DELIMETER +
                BackgroundRunner.RX_LEVEL + Values.DELIMETER +
                BackgroundRunner.RSRQ + Values.DELIMETER +
                BackgroundRunner.ECIO + Values.DELIMETER +
                BackgroundRunner.RX_QUAL + Values.DELIMETER +
                BackgroundRunner.EARFCN + Values.DELIMETER +
                BackgroundRunner.UARFCN + Values.DELIMETER +
                BackgroundRunner.ARFCN + Values.DELIMETER +
                BackgroundRunner.SINR + Values.DELIMETER +
                BackgroundRunner.MCC + Values.DELIMETER +
                BackgroundRunner.MNC + Values.DELIMETER +
                BackgroundRunner.LAC_TAC + Values.DELIMETER +
                BackgroundRunner.CELL_ID + Values.DELIMETER +
                BackgroundRunner.PSC_PCI + Values.DELIMETER +
                BackgroundRunner.SPN + Values.DELIMETER +
                BackgroundRunner.DATA_STATE + Values.DELIMETER +
                BackgroundRunner.SERVICE_STATE + Values.DELIMETER +
                BackgroundRunner.RNC + Values.DELIMETER +
                BackgroundRunner.CQI + Values.DELIMETER +
                BackgroundRunner.FREQ + Values.DELIMETER +
                BackgroundRunner.BAND + Values.DELIMETER +
                BackgroundRunner.TA + Values.DELIMETER +
                BackgroundRunner.CALL_STATE + Values.DELIMETER +
                BackgroundRunner.CALL_DURATION + Values.DELIMETER +
                BackgroundRunner.TEST_STATE + Values.DELIMETER +
                BackgroundRunner.RSSI + Values.DELIMETER +
                BackgroundRunner.SS + Values.DELIMETER +
                AudioRecordingService.isRecording + Values.DELIMETER +
                AudioRecordingService.appToRecordName.replaceAll(" ", "") + Values.DELIMETER +
                AudioRecordingService.initialBufferTime + Values.DELIMETER +
                (AudioRecordingService.throughput*8) + Values.DELIMETER +//*8 TO CONVERT TO BIT
                AudioRecordingService.SESSION + Values.DELIMETER +
                AudioRecordingService.firstByteRecTime + Values.DELIMETER +
                JitterService.JITTER + Values.DELIMETER +
                Utils.getTimestampInFormat(Long.parseLong(BackgroundRunner.TIMESTAMP), Constant.LOG_DATE_FORMAT) + Values.DELIMETER +
                (AudioRecordingService.appDataUsage*8) + Values.DELIMETER +//*8 TO CONVERT TO BIT
                AudioRecordingService.appInitiationTime + Values.DELIMETER +
                AudioRecordingService.packageLoadTime + Values.DELIMETER +
                AutoStartHandler.session.replaceAll("_","") + Values.DELIMETER +
                BackgroundRunner.WIFI_STATE + Values.DELIMETER +
                BackgroundRunner.WIFI_SSID.replace("\"","") + Values.DELIMETER +
                BackgroundRunner.WIFI_RSSI + Values.DELIMETER +
                BackgroundRunner.WIFI_IP + Values.DELIMETER +
                BackgroundRunner.WIFI_FREQ + Values.DELIMETER +
                BackgroundRunner.WIFI_LINK_SPEED + Values.DELIMETER +
//                BackgroundRunner.LAT2 + Values.DELIMETER +
//                BackgroundRunner.LON2 + Values.DELIMETER +
//                BackgroundRunner.PROVIDER + Values.LINE_SEPARATOR;
                BackgroundRunner.GEO_STATE + Values.LINE_SEPARATOR;
        try {
            outputStreamWriter.append(data);
        } catch (Exception e) {
            Log.println(Log.ASSERT,"Error appending to csv",Log.getStackTraceString(e));
        }
    }

    public static void closeCsv(){
        try {
            fileOutputStream.flush();
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception e) {
            Log.println(Log.ASSERT,"Error closing csv",Log.getStackTraceString(e));
        }
    }

    public static void writeToLog(String data){
        String path = ARapp.FILE_DIR;//PRCApplication.STORAGE_DIR + "";//
        long now = System.currentTimeMillis();
        String fileName = Utils.getTimestampInFormat(now, "dd_MMM_kk");
        String time = Utils.getTimestampInFormat(now, "yyyy-MM-dd kk:mm:ss.SSS");
        fileName = fileName + ".ardt";
        File dir = new File(path, Values.LOG_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path +"/"+Values.LOG_PATH+"/"+ fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStreamL = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriterL = new OutputStreamWriter(fileOutputStreamL);
            outputStreamWriterL.write("\n" + time + " -> " + data);
            fileOutputStreamL.flush();
            outputStreamWriterL.flush();
            outputStreamWriterL.close();
            fileOutputStreamL.close(); } catch (Exception e) {
            Log.println(Log.ASSERT,"Error creating log",Log.getStackTraceString(e));
        }
    }
}
