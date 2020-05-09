package rma.shivam.audiorecorder.services;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import rma.shivam.audiorecorder.global.Constant;

public class AudioRecording {

    private static MediaRecorder mRecorder;
    private static boolean isRecording = false;

    public static boolean startRecording() {
        if(isRecording)
            return false;
        try {
            String savePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            savePath += Constant.RECORD_PATH;
            File file = new File(savePath);
            if (!file.exists()) {
                file.mkdir();
            }

            savePath += Constant.RECORD_PREFIX + System.currentTimeMillis() + Constant.RECORD_RORMAT;
            mRecorder = new MediaRecorder();

            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(savePath);
            mRecorder.prepare();
            mRecorder.start();
            isRecording = true;
            Log.println(Log.ASSERT,"Recording started",savePath);
            return true;
        } catch (Exception ex) {
            Log.println(Log.ASSERT,"Recording failed",Log.getStackTraceString(ex));
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean stopRecording(){
        if (isRecording) {
            isRecording = false;
            mRecorder.stop();
            mRecorder.release();
            Log.println(Log.ASSERT,"Recording","stopped");
            return true;
        }
        Log.println(Log.ASSERT,"Recording","not started");
        return false;
    }
}
