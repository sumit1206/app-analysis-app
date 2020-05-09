package rma.shivam.audiorecorder.services;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import rma.shivam.audiorecorder.global.Constant;

public class UploadFile extends AsyncTask<Void, Void, UploadFile.Result>
{
    public static final int INTERNAL_ERROR_FILE_NOT_EXIST = 1;

    private String url;
    private File file;
    private String fileKey;
    private Map<String, String> paramss = null;
    private Callback callback = null;

    public UploadFile(String url, FileSet fileSet) {
        this.url = url;
        this.file = fileSet.getFile();
        this.fileKey = fileSet.getKey();
    }

    public void setParamss(Map<String, String> paramss) {
        this.paramss = paramss;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Result doInBackground(Void ...params) {
        File sourceFile = file;
        String response;
//        Utils.logPrint(this.getClass(),"uploading",sourceFile.getName());
        String upLoadServerUri = url;
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
//            Utils.logPrint(getClass(),"source file", "file does not exist");
            return new Result(Result.INTERNAL_ERROR, INTERNAL_ERROR_FILE_NOT_EXIST);
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

            if(paramss != null) {
                for (Map.Entry<String, String> param : paramss.entrySet()) {
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + param.getKey() + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(param.getValue() + lineEnd);
                }
            }

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
                return new Result(Result.CONNECTION_ERROR, serverResponseCode);
            }

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (Exception ex) {
//            Log.println(Log.ASSERT,"uploading error",String.valueOf(serverResponseCode)+Log.getStackTraceString(ex));
            return new Result(Result.ERROR_RESPONSE, ex);
        }
        return new Result(Result.RESPONSE, response);
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if(callback == null){
            return;
        }
        int resultCode = result.getCode();
        switch (resultCode){
            case Result.RESPONSE:
                try {
                    callback.onResponse((String) result.getObject());
                }catch (Exception e){
                    callback.onResponse(null);
                }

                break;
            case Result.ERROR_RESPONSE:
                callback.onErrorResponse((Exception) result.getObject());
                break;
            case Result.CONNECTION_ERROR:
                callback.onConnectionError((int) result.getObject());
                break;
            case Result.INTERNAL_ERROR:
                callback.onInternalError((int) result.getObject());
                break;
            default:
                break;
        }
    }

    static class FileSet
    {
        File file;
        String key;

        public FileSet(String key, File file) {
            this.file = file;
            this.key = key;
        }

        private File getFile() {
            return file;
        }

        private String getKey() {
            return key;
        }

    }

    static abstract class Callback
    {
        abstract void onResponse(String response);
        abstract void onErrorResponse(Exception e);
        void onConnectionError(int errorCode){}
        void onInternalError(int errorCode){}
    }

    class Result
    {
        private static final int RESPONSE = 1;
        private static final int ERROR_RESPONSE = 2;
        private static final int CONNECTION_ERROR = 3;
        private static final int INTERNAL_ERROR = 4;

        private int code;
        private Object object;

        private Object getObject() {
            return object;
        }

        private int getCode() {
            return code;
        }

        private Result(int code, Object object) {
            this.code = code;
            this.object = object;
        }

    }

    public interface HttpStatus {
        int SC_CONTINUE = 100;
        int SC_SWITCHING_PROTOCOLS = 101;
        int SC_PROCESSING = 102;
        int SC_OK = 200;
        int SC_CREATED = 201;
        int SC_ACCEPTED = 202;
        int SC_NON_AUTHORITATIVE_INFORMATION = 203;
        int SC_NO_CONTENT = 204;
        int SC_RESET_CONTENT = 205;
        int SC_PARTIAL_CONTENT = 206;
        int SC_MULTI_STATUS = 207;
        int SC_MULTIPLE_CHOICES = 300;
        int SC_MOVED_PERMANENTLY = 301;
        int SC_MOVED_TEMPORARILY = 302;
        int SC_SEE_OTHER = 303;
        int SC_NOT_MODIFIED = 304;
        int SC_USE_PROXY = 305;
        int SC_TEMPORARY_REDIRECT = 307;
        int SC_BAD_REQUEST = 400;
        int SC_UNAUTHORIZED = 401;
        int SC_PAYMENT_REQUIRED = 402;
        int SC_FORBIDDEN = 403;
        int SC_NOT_FOUND = 404;
        int SC_METHOD_NOT_ALLOWED = 405;
        int SC_NOT_ACCEPTABLE = 406;
        int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
        int SC_REQUEST_TIMEOUT = 408;
        int SC_CONFLICT = 409;
        int SC_GONE = 410;
        int SC_LENGTH_REQUIRED = 411;
        int SC_PRECONDITION_FAILED = 412;
        int SC_REQUEST_TOO_LONG = 413;
        int SC_REQUEST_URI_TOO_LONG = 414;
        int SC_UNSUPPORTED_MEDIA_TYPE = 415;
        int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
        int SC_EXPECTATION_FAILED = 417;
        int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
        int SC_METHOD_FAILURE = 420;
        int SC_UNPROCESSABLE_ENTITY = 422;
        int SC_LOCKED = 423;
        int SC_FAILED_DEPENDENCY = 424;
        int SC_INTERNAL_SERVER_ERROR = 500;
        int SC_NOT_IMPLEMENTED = 501;
        int SC_BAD_GATEWAY = 502;
        int SC_SERVICE_UNAVAILABLE = 503;
        int SC_GATEWAY_TIMEOUT = 504;
        int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
        int SC_INSUFFICIENT_STORAGE = 507;
    }
}
