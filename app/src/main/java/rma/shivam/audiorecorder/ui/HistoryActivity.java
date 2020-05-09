package rma.shivam.audiorecorder.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import rma.shivam.audiorecorder.ARapp;
import rma.shivam.audiorecorder.R;
import rma.shivam.audiorecorder.controller.HistoryAdapter;
import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.local.CookiesAdapter;
import rma.shivam.audiorecorder.model.History;

public class HistoryActivity extends AppCompatActivity {

    private View view;
    private Context context;
    private ListView historyList;
    private HistoryAdapter historyAdapter;
    private IntentFilter uploadStateFilter;
    private ProgressDialog loading;
    private Snackbar snackbar;
    private int totalLeft = 0;
    private int uploadedCsv = 0, failedCsv = 0, notUploadedCsv = 0, invalidCsv = 0, uploadingCsv = 0;
    private int uploadedAudio = 0, failedAudio = 0, notUploadedAudio = 0, invalidAudio = 0, uploadingAudio = 0;
    private TextView tvAudioState, tvCsvState;

    private HistoryAdapter.EventCallback eventCallback = new HistoryAdapter.EventCallback() {
        @Override
        public void onDeleteClick(History history) {
            showAlert(history);
        }
    };

    private void showAlert(final History history) {
        String dt = Utils.getTimestampInFormat(Long.parseLong(history.getDate_time()),"dd MMM yyyy, hh:mm:ss a");
        CharSequence msg = TextUtils.concat("Sure to delete " , Utils.boldText(history.getApp_name()) , " test of " , Utils.boldText(dt) , "?\n", Utils.boldGreyText("(" + history.getSession_id() + ")"));
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CookiesAdapter cookiesAdapter = new CookiesAdapter(context);
                        cookiesAdapter.openWritable();
                        cookiesAdapter.deleteHistory(history.getSession_id());
                        cookiesAdapter.close();
                        setupListView();
                        Utils.showShortToast(context, "Deleted " + history.getSession_id());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setMessage(msg)
                .create();
        alertDialog.show();
    }


    BroadcastReceiver uploadStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupListView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        init();
        setUpToolbar();
        setupListView();
        registerReceiver(uploadStateReceiver, uploadStateFilter);
    }

    private void init() {
        context = this;
        historyList = findViewById(R.id.history_list);
        tvCsvState = findViewById(R.id.history_log_state);
        tvAudioState = findViewById(R.id.history_audio_state);
        uploadStateFilter = new IntentFilter(Constant.ACTION_UPLOAD_TRIGGER);
        loading = new ProgressDialog(context);
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        view = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE);
    }

    private  void setUpToolbar(){
        Toolbar historyToolbar;
        historyToolbar = findViewById(R.id.history_toolbar);
        historyToolbar.setTitle(R.string.history);
        historyToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        historyToolbar.setNavigationIcon(R.drawable.keyboard_arrow_left_black);
        historyToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupListView() {
        CookiesAdapter cookiesAdapter = new CookiesAdapter(context);
        cookiesAdapter.openReadable();
        ArrayList<History> histories = cookiesAdapter.getHistoryList();
        cookiesAdapter.close();
        historyAdapter = new HistoryAdapter(context, R.layout.history_card, histories, eventCallback);
        historyList.setAdapter(historyAdapter);
        totalLeft = 0;
        uploadedCsv = 0; failedCsv = 0; notUploadedCsv = 0; invalidCsv = 0; uploadingCsv = 0;
        uploadedAudio = 0; failedAudio = 0; notUploadedAudio = 0; invalidAudio = 0; uploadingAudio = 0;
        for (History history : histories){
            String csvState = history.getCsv_uploaded();
            switch (csvState){
                case Constant.UPLOADED:
                    uploadedCsv++;
                    break;
                case Constant.NOT_UPLOADED:
                    notUploadedCsv++;
                    totalLeft++;
                    break;
                case Constant.UPLOADING:
                    uploadingCsv++;
                    break;
                case Constant.UPLOADING_FAILED:
                    failedCsv++;
                    totalLeft++;
                    break;
                case Constant.INVALID:
                    invalidCsv++;
                    break;
                default:
            }
            String audioState = history.getAudio_uploaded();
            switch (audioState){
                case Constant.UPLOADED:
                    uploadedAudio++;
                    break;
                case Constant.NOT_UPLOADED:
                    notUploadedAudio++;
                    totalLeft++;
                    break;
                case Constant.UPLOADING:
                    uploadingAudio++;
                    break;
                case Constant.UPLOADING_FAILED:
                    failedAudio++;
                    totalLeft++;
                    break;
                case Constant.INVALID:
                    invalidAudio++;
                    break;
                default:
            }
        }
        setupInfoPanel();
    }

    private void setupInfoPanel() {
        CharSequence audioState = TextUtils.concat(Utils.boldText("AUDIO"),
                "\n",Constant.UPLOADING , ": " , Utils.boldGreyText(uploadingAudio) ,
                "\n",Constant.UPLOADED , ": " , Utils.boldGreyText(uploadedAudio) ,
                "\n",Constant.NOT_UPLOADED , ": " , Utils.boldGreyText(notUploadedAudio));
//                "\n",Constant.UPLOADING_FAILED , ": " , Utils.boldGreyText(failedAudio) ,
//                "\n",Constant.INVALID , ": " , Utils.boldGreyText(invalidAudio));
        tvAudioState.setText(audioState);

        CharSequence csvState = TextUtils.concat(Utils.boldText("LOG"),
                "\n",Constant.UPLOADING , ": " , Utils.boldGreyText(uploadingCsv) ,
                "\n",Constant.UPLOADED , ": " , Utils.boldGreyText(uploadedCsv) ,
                "\n",Constant.NOT_UPLOADED , ": " , Utils.boldGreyText(notUploadedCsv));
//                "\n",Constant.UPLOADING_FAILED , ": " , Utils.boldGreyText(failedCsv) ,
//                "\n",Constant.INVALID , ": " , Utils.boldGreyText(invalidCsv));
        tvCsvState.setText(csvState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unregisterReceiver(uploadStateReceiver);
    }

    public void uploadClicked(View view) {
        view.setBackgroundColor(getResources().getColor(R.color.text_color));
        if(totalLeft == 0){
            Utils.showSnackBar(context, "Nothing to upload");
            return;
        }
        if(isInternetConnectionStrong()) {
            ARapp.autoUpload();
            totalLeft = 0;
            Utils.showSnackBar(context, "Uploading...");
        }else {
            Utils.showSnackBar(context, "Internet too slow");
        }
    }

    private boolean isInternetConnectionStrong() {
        double THRESHOLD_PING = 500;
        Utils.logPrint(Utils.class, "checking", "latency");
        double latency = getLatency();
        Utils.logPrint(Utils.class, "latency", latency+" ms");
        return true;// latency > 0.0 && latency < THRESHOLD_PING;
    }

    private double getLatency(){
        snackbar.setText("Checking your Internet connection");
        snackbar.show();
        String ipAddress = "8.8.8.8";
        String pingCommand = "/system/bin/ping -c 1 " + ipAddress;
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
        snackbar.dismiss();
        return avgRtt;
    }


}
