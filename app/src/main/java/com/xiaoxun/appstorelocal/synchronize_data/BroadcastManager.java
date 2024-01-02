package com.xiaoxun.appstorelocal.synchronize_data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;

import com.blankj.utilcode.util.LogUtils;
import com.xiaoxun.appstorelocal.repository.NetRepository;
import com.xiaoxun.sdk.IResponseDataCallBack;
import com.xiaoxun.sdk.ResponseData;

public class BroadcastManager {
    private static volatile BroadcastManager sBroadcastManager;
    private IntentFilter mIntentFilter = new IntentFilter();
    private Context mContext;
    private final String LOGIN_OK_ACTION = "com.xiaoxun.sdk.action.LOGIN_OK";
    private final String SESSION_OK_ACTION = "com.xiaoxun.sdk.action.SESSION_OK";
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                switch (action) {
                    case LOGIN_OK_ACTION:
                    case SESSION_OK_ACTION:
                        SynchronizeDataManager.getInstance().synchronizeData();
                        break;
                }
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }
    };

    private BroadcastManager() {
        mIntentFilter.addAction(LOGIN_OK_ACTION);
        mIntentFilter.addAction(SESSION_OK_ACTION);
    }

    public static BroadcastManager getInstance() {
        if (sBroadcastManager == null) {
            synchronized (BroadcastManager.class) {
                if (sBroadcastManager == null) {
                    sBroadcastManager = new BroadcastManager();
                }
            }
        }
        return sBroadcastManager;
    }

    public void registerBroadcastReceiver(Context context) {
        if (mContext == null && context != null) {
            mContext = context;
            context.registerReceiver(mBroadcastReceiver, mIntentFilter);
        }
    }

    public void unregisterBroadcastReceiver() {
        if (mContext != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
        }
    }
}
