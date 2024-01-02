package com.xiaoxun.appstorelocal;

import android.app.Application;
import android.view.Gravity;

import androidx.room.Room;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.xiaoxun.appstorelocal.repository.AppDatabase;
import com.xiaoxun.sdk.XiaoXunNetworkManager;
import com.xiaoxun.statistics.XiaoXunStatisticsManager;

public class App extends Application {
    public static App sApp;
    public static AppDatabase sAppDatabase;
    private XiaoXunNetworkManager mXiaoXunNetworkManager;
    private XiaoXunStatisticsManager mXiaoXunStatisticsManager;
    private final String XIAO_XUN_NET_SERVICE = "xun.network.Service";
    private final String XIAO_XUN_STATISTICS_SERVICE = "xun.statistics.service";

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        LogUtils.getConfig().setGlobalTag("XunLocalAppStore");
        Utils.init(this);
        sAppDatabase = Room.databaseBuilder(this, AppDatabase.class, "XunAppStore")
                .enableMultiInstanceInvalidation()
                .build();
    }

    public XiaoXunNetworkManager getXiaoXunNetworkManager() {
        if (mXiaoXunNetworkManager == null) {
            mXiaoXunNetworkManager = (XiaoXunNetworkManager) getSystemService(XIAO_XUN_NET_SERVICE);
        }
        if (mXiaoXunNetworkManager == null) {
            LogUtils.d("小寻网络服务为空");
            return null;
        }
        return mXiaoXunNetworkManager;
    }

    public XiaoXunStatisticsManager getXiaoXunStatisticsManager() {
        if (mXiaoXunStatisticsManager == null) {
            mXiaoXunStatisticsManager = (XiaoXunStatisticsManager) getSystemService(XIAO_XUN_NET_SERVICE);
        }
        if (mXiaoXunStatisticsManager == null) {
            LogUtils.d("小寻统计服务为空");
            return null;
        }
        return mXiaoXunStatisticsManager;
    }
}
