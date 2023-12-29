package com.xiaoxun.appstorelocal;

import android.app.Application;
import android.view.Gravity;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;

public class App extends Application {
    public static App sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        LogUtils.getConfig().setGlobalTag("XunLocalAppStore");
        Utils.init(this);
    }
}
