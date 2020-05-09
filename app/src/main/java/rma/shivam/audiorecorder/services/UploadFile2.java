package rma.shivam.audiorecorder.services;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;

public class UploadFile2 {
    private String execute(String URL, File file, String fileKey){
        File sourceFile = file;
        String response;
        Utils.logPrint(this.getClass(),"uploading",sourceFile.getName());
        String upLoadServerUri = URL;
        int serverResponseCode = 0;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "apiclient-" + System.currentTimeMillis();// = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 10 * 1024 * 1024;
        if (!sourceFile.isFile())
        {
            Utils.logPrint(getClass(),"source file", "file does not exist");
            return null;
        }
        try {
            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setConnectTimeout(Constant.SERVER_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty(fileKey, sourceFile.getName());
            //conn.setRequestProperty("pid", "4");
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\""+ fileKey +"\";filename=\""+ sourceFile.getName() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = bufferedReader.readLine();

//            Utils.logPrint(this.getClass(),"serverResponse", String.valueOf(serverResponseCode) + serverResponseMessage);
//            Utils.logPrint(this.getClass(),"apiResponse", response);
            if(serverResponseCode != 200)
            {
                Utils.logPrint(getClass(), "error server response", serverResponseCode + ":" + serverResponseMessage);
                return null;
            }

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (Exception ex) {
            Utils.logPrint(getClass(),"catch block", Log.getStackTraceString(ex));
            return null;
        }
        return response;
    }
}
