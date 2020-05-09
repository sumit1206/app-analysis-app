package rma.shivam.audiorecorder.services;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import rma.shivam.audiorecorder.ARapp;
import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.model.AppData;

import static rma.shivam.audiorecorder.ARapp.STORAGE_DIR;

public class UpdateAppDataCsv extends AsyncTask<Void, Void, String> {

    private int version;

    public UpdateAppDataCsv() {
        this.version = Utils.getInt(ARapp.context, Constant.KEY_APP_DATA_VERSION,0);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            String u = Constant.APP_DATA_URL+"?v="+version;
            URL url = new URL(u);

            Log.println(Log.ASSERT,"hitting",url.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(Constant.SERVER_TIMEOUT);

            Log.println(Log.ASSERT,"getResponseMessage",connection.getResponseMessage());
            Log.println(Log.ASSERT,"getResponseCode",connection.getResponseCode()+"");

            int rCode = connection.getResponseCode();
            if(rCode != 200){
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = in.readLine();

            Log.println(Log.ASSERT, "ApiResponse",result+"");

            in.close();
            connection.disconnect();
            return result;

        } catch (Exception e) {
            Log.println(Log.ASSERT,"error",Log.getStackTraceString(e));
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s == null) {
//            return;
        }else {
            try {
                JSONObject jsonObject = new JSONObject(s);
                int success = jsonObject.getInt("success");
                if (success == 0) {
//                    return;
                }else if (success == 1) {
                    boolean savedSuccessfully = updateClassSectionList(s);
                    Utils.logPrint(getClass(),"savedSuccessfully",savedSuccessfully+"");
                    if (savedSuccessfully) {
                        int newVersion = jsonObject.getInt("version");
                        Utils.saveInt(ARapp.context, Constant.KEY_APP_DATA_VERSION, newVersion);
                    }
                }
            } catch (JSONException e) {
                Utils.logPrint(getClass(),"error parsing json",Log.getStackTraceString(e));
            }
        }
    }

    private boolean updateClassSectionList(String response) {
        try {
            ArrayList<AppData> appDataArrayList = new ArrayList<>();
            AppData appData;
            JSONObject jsonObject = new JSONObject(response);
            JSONArray appDataJsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < appDataJsonArray.length(); i++){
                JSONObject appObject = appDataJsonArray.getJSONObject(i);
                String name = appObject.getString("app_name");
                String pakage = appObject.getString("package_name");
                String uri = appObject.getString("url");
                appData = new AppData(name, pakage, uri, false);
                appDataArrayList.add(appData);
            }
            return createAppDataCsv(appDataArrayList);
        } catch (Exception e) {
            Utils.logPrint(getClass(),"error parsing json",Log.getStackTraceString(e));
            return false;
        }
    }

    private boolean createAppDataCsv(ArrayList<AppData> appDataArrayList){
        if(appDataArrayList == null) {
            Utils.logPrint(getClass(),"appDataArrayList","null");
            return false;
        }
        if(appDataArrayList.isEmpty()) {
            Utils.logPrint(getClass(),"appDataArrayList","empty");
            return false;
        }
        FileOutputStream fileOutputStream;
        OutputStreamWriter outputStreamWriter;
        String path = STORAGE_DIR + "";
        String fileName = Constant.FILE_NAME_APP_LIST;
        File dir = new File(path, Constant.SAVE_PATH_CACHE_DUMP);
        if (!dir.exists()) {
            dir.mkdirs();
            Utils.logPrint(getClass(),"dir","created");
        }else {
            dir.delete();
            dir.mkdirs();
            Utils.logPrint(getClass(),"dir","deleted & created");
        }
        File file = new File(path +"/"+ Constant.SAVE_PATH_CACHE_DUMP+"/"+ fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
                Utils.logPrint(getClass(),"file","created");
            }else {
                file.delete();
                file.createNewFile();
                Utils.logPrint(getClass(),"file","deleted & created");
            }
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

            StringBuilder data = new StringBuilder();
            for(AppData appData: appDataArrayList){
                data.append(appData.getAppName()).append(Constant.DELIMETER).append(appData.getAppPackage()).append(Constant.DELIMETER).append(appData.getAppUri()).append(Constant.LINE_SEPARATOR);
            }
            fileOutputStream = new FileOutputStream(file, true);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(data.toString());
            fileOutputStream.flush();
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            Log.println(Log.ASSERT,"Error creating csv",Log.getStackTraceString(e));
            return false;
        }
    }


}
