package rma.shivam.audiorecorder.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import rma.shivam.audiorecorder.global.Utils;

public class ConnectivityStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.logPrint(getClass(), "connectivity","changed");

    }
}
