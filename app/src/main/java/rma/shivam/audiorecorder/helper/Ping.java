package rma.shivam.audiorecorder.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.Nullable;

import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

import rma.shivam.audiorecorder.global.Utils;

public class Ping {
    public String net = "NO_CONNECTION";
    public String host = "";
    public String ip = "";
    public int dns = Integer.MAX_VALUE;
    public int cnt = Integer.MAX_VALUE;

    public static Ping ping(URL url, Context context) {
        Ping r = new Ping();
        if (isNetworkConnected(context)) {
            r.net = getNetworkType(context);
            try {
                String hostAddress;
                long start = System.currentTimeMillis();
                hostAddress = InetAddress.getByName(url.getHost()).getHostAddress();
                long dnsResolved = System.currentTimeMillis();
                Socket socket = new Socket(hostAddress, url.getPort());
                socket.close();
                long probeFinish = System.currentTimeMillis();
                r.dns = (int) (dnsResolved - start);
                r.cnt = (int) (probeFinish - dnsResolved);
                r.host = url.getHost();
                r.ip = hostAddress;
            } catch (Exception ex) {
                Utils.logPrint(Ping.class, "ping error", Log.getStackTraceString(ex));
            }
        }
        return r;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Nullable
    public static String getNetworkType(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return activeNetwork.getTypeName();
        }
        return null;
    }

}