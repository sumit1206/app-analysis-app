package rma.shivam.audiorecorder.services;

import android.app.Service;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.global.Values;

public class JitterService extends Service {
    public JitterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    public static String JITTER = Values.NULL_VALUE;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int pingHit = 4;
        double jitter = 0.0;
        double iniJitter;
        double[] pings = new double[pingHit];
        for(int i = 0; i<pingHit; i++){
            pings[i] = getLatency();
        }
        for(int i = 0; i<pingHit-1; i++){
            iniJitter = Math.abs(pings[i] - pings[i+1]);
            jitter += iniJitter;

        }
        jitter /= pingHit-1;
        JITTER = String.valueOf(jitter);
        Utils.logPrint(getClass(),"jitter", String.valueOf(jitter));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public double getLatency(){
        String ipAddress = "8.8.8.8";
        String pingCommand = "/system/bin/ping -c 1 "+ ipAddress;
        String inputLine = "";
        double avgRtt = 0.0;

        try {
            // execute the command on the environment interface
            Process process = Runtime.getRuntime().exec(pingCommand);
            // gets the input stream to get the output of the executed command
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            inputLine = bufferedReader.readLine();
            while ((inputLine != null)) {
                Utils.logPrint(getClass(),"inputline",inputLine);
                if (inputLine.length() > 0 && inputLine.contains("time=")) {  // when we get to the last line of executed ping command
                    String ping = inputLine.substring(inputLine.lastIndexOf('=')+1,inputLine.lastIndexOf(' '));
                    Utils.logPrint(getClass(),"ping",ping);
                    avgRtt = Double.parseDouble(ping);
                    break;
                }
                inputLine = bufferedReader.readLine();
            }
        }
        catch (IOException e){
            Utils.logPrint(getClass(),"error ping", Log.getStackTraceString(e));
        }
        return avgRtt;
    }
}
