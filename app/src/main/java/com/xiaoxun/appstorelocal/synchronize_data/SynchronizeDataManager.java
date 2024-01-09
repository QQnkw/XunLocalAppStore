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
import io.reactivex.rxjava3.functions.Predicate;
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
        // TODO: 2024/1/2 1.上传安装成功或失败的信息2.上传本地未被管理的应用3.同步服务信息到本地4.对服务器应用信息进行分析:卸载,安装等
        String json = SPUtils.getInstance().getString(SP_KEY_UPLOAD_FAIL_APP);
        List<AppReasonBean> appReasonBeanList = GsonUtils.fromJson(json, new TypeToken<List<AppReasonBean>>() {
        }.getType());
        Observable<Boolean> observable = null;
        if (CollectionUtils.isEmpty(appReasonBeanList)) {
            //没有上报失败的应用
            observable = Observable.just(true);
        } else {
            //先上包失败的应用
            observable = uploadAppInstallState(appReasonBeanList);
        }
        observable.flatMap(new Function<Boolean, ObservableSource<List<AppInfo>>>() {
            @Override
            public ObservableSource<List<AppInfo>> apply(Boolean aBoolean) throws Throwable {
                return queryInstalledThirdAppListFromSystem();
            }
        }).flatMap(new Function<List<AppInfo>, ObservableSource<AppInfo>>() {
            @Override
            public ObservableSource<AppInfo> apply(List<AppInfo> appInfos) throws Throwable {
                List<AppInfo> dbList = App.sAppDatabase.appInfoDao().queryAll();
                ArrayList<AppInfo> needManageAppList = new ArrayList<AppInfo>();
                boolean isExit;
                for (AppInfo appInfo : appInfos) {
                    isExit = false;
                    for (AppInfo info : dbList) {
                        if (appInfo.app_id.equals(info.app_id)) {
                            isExit = true;
                            break;
                        }
                    }
                    if (!isExit) {
                        needManageAppList.add(appInfo);
                    }
                }

                return Observable.fromIterable(appInfos);
            }
        });
        /*if (observable == null) {
            observable = queryInstalledThirdAppListFromSystem();
        } else {
            observable = observable.flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
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
        }*/
    }

    private Observable<Boolean> uploadAppInstallState(List<AppReasonBean> appReasonBeanList) {
        List<Observable<Boolean>> observableList = new ArrayList<>();
        for (AppReasonBean appReasonBean : appReasonBeanList) {
            Observable<Boolean> uploadAppOptypeObservable = NetRepository.getInstance().uploadAppOptype(appReasonBean);
            Observable<Boolean> uploadInstallAppReasonObservable = NetRepository.getInstance().uploadInstallAppReason(appReasonBean);
            Observable<Boolean> observable = uploadAppOptypeObservable.flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
                @Override
                public ObservableSource<Boolean> apply(Boolean aBoolean) throws Throwable {
                    return uploadInstallAppReasonObservable;
                }
            });
            observableList.add(observable);
        }
        return Observable.zip(observableList, new Function<Object[], Boolean>() {
            @Override
            public Boolean apply(Object[] objects) throws Throwable {
                for (Object object : objects) {
                    if (object instanceof Boolean) {
                        Boolean result = (Boolean) object;
                        if (!result) {
                            throw new Exception("上报的信息有异常");
                        }
                    }
                }
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
