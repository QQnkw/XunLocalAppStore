package com.xiaoxun.appstorelocal.synchronize_data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        BroadcastManager.getInstance().registerBroadcastReceiver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BroadcastManager.getInstance().unregisterBroadcastReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}