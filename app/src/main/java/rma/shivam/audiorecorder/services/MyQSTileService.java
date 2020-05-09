package rma.shivam.audiorecorder.services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.TileService;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import rma.shivam.audiorecorder.global.Utils;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MyQSTileService extends TileService {

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Utils.logPrint(getClass(),"onTileAdded","onTileAdded");
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Utils.logPrint(getClass(),"onTileRemoved","onTileRemoved");
    }
}
