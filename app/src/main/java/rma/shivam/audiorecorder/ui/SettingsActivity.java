package rma.shivam.audiorecorder.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import rma.shivam.audiorecorder.R;
import rma.shivam.audiorecorder.global.Values;
import rma.shivam.audiorecorder.controller.AppSelectListAdapter;
import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.model.AutoStartHandler;
import rma.shivam.audiorecorder.model.AppData;
import rma.shivam.audiorecorder.services.AudioRecordingService;

import static rma.shivam.audiorecorder.ARapp.STORAGE_DIR;

public class SettingsActivity extends AppCompatActivity {

    Context context;
    LinearLayout llOneAppLayout;
    Spinner appSpinner;
    ArrayList<String> appNames, appPackages, appUris;
    TextView selectedPackage, selectedUri;
    Switch killProcess, autoRecord;
    EditText etIteration, etPlaybackTime;

    Toolbar settingsToolbar;
    ListView selectedAppList;
    AppSelectListAdapter appSelectListAdapter;
    ArrayList<AppData> appDatas = new ArrayList<>();

    View.OnClickListener toggleView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(autoRecord.isChecked()){
                findViewById(R.id.auto_record_layout).setVisibility(View.VISIBLE);
                llOneAppLayout.setVisibility(View.GONE);
            }else {
                findViewById(R.id.auto_record_layout).setVisibility(View.GONE);
                llOneAppLayout.setVisibility(View.VISIBLE);
            }
        }
    };

    AdapterView.OnItemSelectedListener appSelectListenor = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedPackage.setText(appPackages.get(position));
            selectedUri.setText(appUris.get(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        readFile();
        setUpListView();
        populateAppSpinner();
        setUpRestUi();
        updateView();
        int selectedSerial = Utils.getInt(context, Constant.KEY_SELECTED_APP_SERIAL, 0);
        appSpinner.setSelection(selectedSerial);
        selectedPackage.setText(appPackages.get(selectedSerial));
        selectedUri.setText(appUris.get(selectedSerial));
        setUpToolbar();
    }

    private void init() {
        context = this;
        llOneAppLayout = findViewById(R.id.one_app_layout);
        appSpinner = findViewById(R.id.app_spinner);
        appSpinner.setOnItemSelectedListener(appSelectListenor);
        selectedPackage = findViewById(R.id.package_name);
        selectedUri = findViewById(R.id.uri);
        killProcess = findViewById(R.id.kill_process);
        autoRecord = findViewById(R.id.auto_record);
        autoRecord.setOnClickListener(toggleView);
        etIteration = findViewById(R.id.iteration);
        etPlaybackTime = findViewById(R.id.play_back_time);
        appNames = new ArrayList<>();
        appPackages = new ArrayList<>();
        appUris = new ArrayList<>();
    }

    private void readFile() {
        AppData appData;
        String path = STORAGE_DIR + "";
        String fileName = Constant.FILE_NAME_APP_LIST;
        File file = new File(path +"/"+ Constant.SAVE_PATH_CACHE_DUMP+"/"+ fileName);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                appNames.add(data[0]);
                appPackages.add(data[1]);
                appUris.add(data[2]);
                boolean isSelected = false;
                if(AutoStartHandler.packages.contains(data[1])){
                    isSelected = true;
                }
                appData = new AppData(data[0],data[1],data[2],isSelected);
                appDatas.add(appData);
            }
            scanner.close();
        }catch (Exception e){
            Utils.logPrint(getClass(),"Error reading travelMode", Log.getStackTraceString(e));
        }

    }

    private  void setUpToolbar(){
        settingsToolbar = findViewById(R.id.settings_toolbar);
        settingsToolbar.setTitle(R.string.settings);
        settingsToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        settingsToolbar.setNavigationIcon(R.drawable.keyboard_arrow_left_black);
        settingsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setUpListView(){
        appSelectListAdapter = new AppSelectListAdapter(context,R.layout.custom_list_item_app_select,appDatas);
        selectedAppList=(ListView)findViewById(R.id.appSelectionListView);
        selectedAppList.setAdapter(appSelectListAdapter);
    }
    private void populateAppSpinner() {
        ArrayAdapter<String> appAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, appNames);
        appAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appSpinner.setAdapter(appAdapter);
    }

    private void setUpRestUi() {
        long tmpPbt = Utils.getLong(context, Constant.KEY_PLAYBACK_TIME, AudioRecordingService.THREE_MIN);
        etPlaybackTime.setText(tmpPbt+"");
        boolean killProc = Utils.getBoolean(context, Constant.KEY_KILL_PROCESS, Constant.DEFAULT_KILL_PROCESS);
        killProcess.setChecked(killProc);
    }

    private void updateView(){
        etIteration.setText(String.valueOf(AutoStartHandler.iteration));
        if(!AutoStartHandler.isActivated){
            llOneAppLayout.setVisibility(View.VISIBLE);
            return;
        }
        autoRecord.performClick();
    }
    public void saveClicked(View view) {
        //todo VALIDATE PLAYBACK TIME
        String tmpPbt= etPlaybackTime.getText().toString().trim();
        if(tmpPbt.equals("")){
            Utils.showShortToast(context,"playback time needed");
            return;
        }
        long playbackTime = Long.parseLong(tmpPbt);
        if(playbackTime < 10){
            Utils.showShortToast(context,"playback time minimum 10secs");
            return;
        }
        //todo AUTO START SECTION
        if(autoRecord.isChecked()){
            AutoStartHandler.isActivated = true;
            AutoStartHandler.packages.clear();
            AutoStartHandler.appNames.clear();
            AutoStartHandler.appUris.clear();
            int appCount = 0;
            for(AppData appData: appDatas){
                if(appData.isSelected()){
                    if(!Utils.isPackageInstalled(context,appData.getAppPackage())){
                        Utils.showShortToast(context, appData.getAppName() + Values.NULL_VALUE + getString(R.string.app_not_installed));
                        continue;
                    }
                    AutoStartHandler.packages.add(appData.getAppPackage());
                    AutoStartHandler.appNames.add(appData.getAppName());
                    AutoStartHandler.appUris.add(appData.getAppUri());
                    appCount++;
                }
            }
            if(appCount == 0){
                Utils.showShortToast(context, getString(R.string.no_app_selected));
                return;
            }AutoStartHandler.appCount = appCount;
            String strIteration = etIteration.getText().toString().trim();
            int iteration = Integer.parseInt(strIteration.equals("")?"0":strIteration);
            if(iteration < 1){
                Utils.showShortToast(context, getString(R.string.invalid_iteration));
                return;
            }AutoStartHandler.iteration = iteration;
            AutoStartHandler.currentIteration = 0;
            AutoStartHandler.runningAppNo = 0;
            Utils.saveString(context, Constant.KEY_APP_TO_RECORD_PACKAGE, AutoStartHandler.packages.get(0));
            Utils.saveString(context, Constant.KEY_APP_TO_RECORD_NAME, AutoStartHandler.appNames.get(0));
            Utils.saveString(context, Constant.KEY_INTENT_URI, AutoStartHandler.appUris.get(0));
            Utils.logPrint(getClass(),"packages",AutoStartHandler.packages.toString());
            Utils.logPrint(getClass(),"appNames",AutoStartHandler.appNames.toString());
            Utils.logPrint(getClass(),"appUris",AutoStartHandler.appUris.toString());
        }
        //todo MANUAL START SECTION
        else {
            AutoStartHandler.isActivated = false;
            int appSrlNo = appSpinner.getSelectedItemPosition();
            String selectedPackage = appPackages.get(appSrlNo);
            if (Utils.isPackageInstalled(context, selectedPackage)) {
                String selectedUri = appUris.get(appSrlNo);
                String selectedName = appNames.get(appSrlNo);
                Utils.saveString(context, Constant.KEY_APP_TO_RECORD_PACKAGE, selectedPackage);
                Utils.saveString(context, Constant.KEY_APP_TO_RECORD_NAME, selectedName);
                Utils.saveString(context, Constant.KEY_INTENT_URI, selectedUri);
                Utils.saveInt(context, Constant.KEY_SELECTED_APP_SERIAL, appSrlNo);
            } else {
                Utils.showShortToast(context, getString(R.string.app_not_installed));
                return;
            }
        }
        //todo COMMON PARTS
        Utils.saveLong(context, Constant.KEY_PLAYBACK_TIME, playbackTime);
        Utils.saveBoolean(context, Constant.KEY_KILL_PROCESS, killProcess.isChecked());
        AutoStartHandler.saveClass();
        Utils.showShortToast(context, getString(R.string.saved));
        onBackPressed();
    }
}
