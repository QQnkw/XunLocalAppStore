package com.xiaoxun.appstorelocal.synchronize_data;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.MapUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.google.gson.reflect.TypeToken;
import com.xiaoxun.appstorelocal.App;
import com.xiaoxun.appstorelocal.bean.AppInfo;
import com.xiaoxun.appstorelocal.bean.AppReasonBean;
import com.xiaoxun.appstorelocal.repository.NetRepository;
import com.xiaoxun.sdk.IResponseDataCallBack;
import com.xiaoxun.sdk.ResponseData;
import com.xiaoxun.sdk.XiaoXunNetworkManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Function3;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SynchronizeDataManager {
    private static volatile SynchronizeDataManager sSynchronizeDataManager;
    private final String SP_KEY_UPLOAD_FAIL_APP = "UploadFailApp";

    private SynchronizeDataManager() {
    }

    public static SynchronizeDataManager getInstance() {
        if (sSynchronizeDataManager == null) {
            synchronized (SynchronizeDataManager.class) {
                if (sSynchronizeDataManager == null) {
                    sSynchronizeDataManager = new SynchronizeDataManager();
                }
            }
        }
        return sSynchronizeDataManager;
    }

    /**
     *
     */
    public void synchronizeData() {
        // TODO: 2024/1/2 1.上传安装成功或失败的信息2.上传本地未被管理的应用3.同步服务信息到本地
        // TODO: 2023/12/29 1.拉取服务器的信息2.和本地数据库比较3.同步本地应用到既不在服务器也不在本地应用的4.对服务器应用信息进行分析:卸载,安装等
        String json = SPUtils.getInstance().getString(SP_KEY_UPLOAD_FAIL_APP);
        List<AppReasonBean> appReasonBeanList = GsonUtils.fromJson(json, new TypeToken<List<AppReasonBean>>() {
        }.getType());
        Observable observable = null;
        if (CollectionUtils.isEmpty(appReasonBeanList)) {
            //没有上传失败的应用
        } else {
            //先上传失败的应用
            observable = uploadAppInstallState(appReasonBeanList);
        }
        if (observable == null) {
            observable = queryInstalledThirdAppListFromSystem();
        } else {
            observable.flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
                @Override
                public ObservableSource<Boolean> apply(Boolean aBoolean) throws Throwable {
                    Observable.zip(queryInstalledThirdAppListFromSystem()
                            , App.sAppDatabase.appInfoDao().queryAllOnIO().toObservable(), new BiFunction<List<AppInfo>, List<AppInfo>, Boolean>() {
                                @Override
                                public Boolean apply(List<AppInfo> appInfos, List<AppInfo> appInfos2) throws Throwable {
                                    return null;
                                }
                            });
                    return Observable.just(true);
                }
            });
        }
    }

    private Observable<Boolean> uploadAppInstallState(List<AppReasonBean> appReasonBeanList) {
        List<Observable<Boolean>> observableList = new ArrayList<>();
        for (AppReasonBean appReasonBean : appReasonBeanList) {
            Observable<Boolean> uploadAppOptypeObservable = NetRepository.getInstance().uploadAppOptype(appReasonBean);
            Observable<Boolean> uploadInstallAppSourceObservable = NetRepository.getInstance().uploadInstallAppSource(appReasonBean);
            Observable<Boolean> uploadInstallAppReasonObservable = NetRepository.getInstance().uploadInstallAppReason(appReasonBean);
            Observable zipObservable = Observable.zip(uploadAppOptypeObservable, uploadInstallAppSourceObservable, uploadInstallAppReasonObservable, new Function3<Boolean, Boolean, Boolean, Boolean>() {
                @Override
                public Boolean apply(Boolean aBoolean, Boolean aBoolean2, Boolean aBoolean3) throws Throwable {
                    return aBoolean && aBoolean2 && aBoolean3;
                }
            });
            observableList.add(zipObservable);
        }
        return Observable.zip(observableList, new Function<Object[], Boolean>() {
            @Override
            public Boolean apply(Object[] objects) throws Throwable {
                String string = Arrays.toString(objects);
                LogUtils.d(string);
                return true;
            }
        });
    }

    private Observable<List<AppInfo>> queryInstalledThirdAppListFromSystem() {
        return Observable.create(new ObservableOnSubscribe<List<AppInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<AppInfo>> emitter) throws Throwable {
                String eid = "";
                String gid = "";
                XiaoXunNetworkManager xiaoXunNetworkManager = App.sApp.getXiaoXunNetworkManager();
                if (xiaoXunNetworkManager != null) {
                    eid = xiaoXunNetworkManager.getWatchEid();
                    gid = xiaoXunNetworkManager.getWatchGid();
                }
                PackageManager packageManager = App.sApp.getPackageManager();
                List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
                List<AppInfo> thirdAppInstalledList = new ArrayList<AppInfo>();
                for (PackageInfo installedPackage : installedPackages) {
                    if ((installedPackage.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        String packagesName = installedPackage.applicationInfo.packageName;
                        if ("com.lctautotest.camera".equals(packagesName) ||
                                "com.example.android.factoryreset".equals(packagesName) ||
                                "com.android.camera2".equals(packagesName)
                        ) {
                            continue;
                        }
                        int versionCode = installedPackage.versionCode;
                        String versionName = installedPackage.versionName;
                        String appName;
                        CharSequence loadLabel = installedPackage.applicationInfo.loadLabel(packageManager);
                        if (loadLabel == null) {
                            appName = packagesName;
                        } else {
                            appName = loadLabel.toString();
                        }
                        LogUtils.d("非系统应用名称:" + appName +
                                ",包名:" + packagesName +
                                ",versionCode:" + versionCode +
                                ",versionName:" + versionName);
                        AppInfo listBean = new AppInfo();
                        listBean.name = appName;
                        listBean.type = 1;
                        listBean.app_id = packagesName;
                        listBean.EID = eid;
                        listBean.GID = gid;
                        listBean.optype = 0;
                        listBean.icon = packagesName;
                        listBean.status = 0;
                        listBean.hidden = 0;
                        listBean.version = versionName;
                        listBean.version_code = versionCode;
                        listBean.download_url = "";
                        listBean.wifi = 0;
                        listBean.size = 0;
                        listBean.md5 = "";
                        listBean.updateTS = "";
                        thirdAppInstalledList.add(listBean);
                    }
                }
                emitter.onNext(thirdAppInstalledList);
                emitter.onComplete();
            }
        });
    }
}
