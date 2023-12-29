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
                        // TODO: 2023/12/29 1.拉取服务器的信息2.和本地数据库比较3.同步本地应用到既不在服务器也不在本地应用的4.对服务器应用信息进行分析:卸载,安装等
                        NetRepository.getInstance().queryAllAppInfo(new IResponseDataCallBack.Stub() {
                            @Override
                            public void onSuccess(ResponseData responseData) throws RemoteException {
                                if (responseData != null) {
                                    LogUtils.d(responseData.getResponseData());
                                }
                            }

                            @Override
                            public void onError(int i, String s) throws RemoteException {

                            }
                        });
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
