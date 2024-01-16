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

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Function3;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SynchronizeDataManager {
    private static volatile SynchronizeDataManager sSynchronizeDataManager;
    private final String SP_KEY_UPLOAD_FAIL_APP = "UploadFailApp";

    private SynchronizeDataManager() {
        App.sAppDatabase.appInfoDao().queryAllWhenTableDataChangeOnIO()
                .subscribeOn(Schedulers.io())
                .subscribe(new FlowableSubscriber<List<AppInfo>>() {
                    @Override
                    public void onSubscribe(@NonNull Subscription s) {

                    }

                    @Override
                    public void onNext(List<AppInfo> appInfos) {
                        LogUtils.d(appInfos);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
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
    public synchronized void synchronizeData() {
        //1.上传安装成功或失败的信息2.同步服务信息到本地3.上传未被管控的应用4.对服务器应用信息进行分析:卸载,安装等
        String json = SPUtils.getInstance().getString(SP_KEY_UPLOAD_FAIL_APP);
        LogUtils.d("上报失败的数据:" + json);
        List<AppReasonBean> appReasonBeanList = GsonUtils.fromJson(json, new TypeToken<List<AppReasonBean>>() {
        }.getType());
        Observable<Boolean> observable = null;
        if (CollectionUtils.isEmpty(appReasonBeanList)) {
            //没有上报失败的应用
            observable = Observable.just(true);
        } else {
            //先上报失败的应用
            observable = uploadAppInstallState(appReasonBeanList).toObservable();
        }
        observable.flatMap(new Function<Boolean, ObservableSource<List<AppInfo>>>() {
                    @Override
                    public ObservableSource<List<AppInfo>> apply(Boolean aBoolean) throws Throwable {
                        return NetRepository.getInstance().queryAllAppInfoFromNet();
                    }
                }).zipWith(queryInstalledThirdAppListFromSystem(), new BiFunction<List<AppInfo>, List<AppInfo>, List<AppInfo>>() {
                    @Override
                    public List<AppInfo> apply(List<AppInfo> appInfoNetList, List<AppInfo> appInfoLocalList) throws Throwable {
                        App.sAppDatabase.appInfoDao().emptyTable();
                        ArrayList<AppInfo> noControlAppList = new ArrayList<>();
                        for (AppInfo appInfoLocal : appInfoLocalList) {
                            boolean has = false;
                            for (AppInfo appInfoNet : appInfoNetList) {
                                if (appInfoLocal.app_id.equals(appInfoNet.app_id)) {
                                    has = true;
                                    break;
                                }
                            }
                            if (!has) {
                                noControlAppList.add(appInfoLocal);
                            }
                        }
                        ArrayList<AppInfo> insertDBAppInfoList = new ArrayList<>(appInfoNetList);
                        if (!noControlAppList.isEmpty()) {
                            insertDBAppInfoList.addAll(noControlAppList);
                        }
                        App.sAppDatabase.appInfoDao().insert(insertDBAppInfoList);
                        return noControlAppList;
                    }
                }).flatMap(new Function<List<AppInfo>, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(List<AppInfo> appInfos) throws Throwable {
                        ArrayList<AppReasonBean> list = new ArrayList<>();
                        for (AppInfo appInfo : appInfos) {
                            AppReasonBean appReasonBean = new AppReasonBean();
                            appReasonBean.app_id = appInfo.app_id;
                            appReasonBean.name = appInfo.name;
                            appReasonBean.reason = "未通过下载安装的应用";
                            appReasonBean.actionType = "install";
                            appReasonBean.optype = 0;
                            appReasonBean.installSourceDevice = "未知来源";
                            appReasonBean.installSourceAppId = "未知来源";
                            appReasonBean.installAppNetType = "未知来源";
                            list.add(appReasonBean);
                        }
                        String json = SPUtils.getInstance().getString(SP_KEY_UPLOAD_FAIL_APP);
                        LogUtils.d("未添加管控前未上报数据:" + json);
                        List<AppReasonBean> appReasonBeanList = GsonUtils.fromJson(json, new TypeToken<List<AppReasonBean>>() {
                        }.getType());
                        if (appReasonBeanList == null) {
                            appReasonBeanList = new ArrayList<>(list);
                        } else {
                            appReasonBeanList.addAll(list);
                        }
                        String toJson = GsonUtils.toJson(appReasonBeanList);
                        LogUtils.d("添加管控后未上报数据:" + toJson);
                        SPUtils.getInstance().put(SP_KEY_UPLOAD_FAIL_APP, toJson);
                        return uploadAppInstallState(list).toObservable();
                    }
                }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private Single<Boolean> uploadAppInstallState(List<AppReasonBean> appReasonBeanList) {
        List<Observable<Object[]>> observableList = new ArrayList<>();
        for (AppReasonBean appReasonBean : appReasonBeanList) {
            Observable<Object[]> uploadAppOptypeObservable = NetRepository.getInstance().uploadAppOptype(appReasonBean);
            Observable<Object[]> uploadInstallAppReasonObservable = NetRepository.getInstance().uploadInstallAppReason(appReasonBean);
            Observable<Object[]> observable = uploadAppOptypeObservable.flatMap(new Function<Object[], ObservableSource<Object[]>>() {
                @Override
                public ObservableSource<Object[]> apply(Object[] objectArr) throws Throwable {
                    boolean result = (boolean) objectArr[1];
                    if (result) {
                        return uploadInstallAppReasonObservable;
                    } else {
                        return Observable.just(objectArr);
                    }
                }
            });
            observableList.add(observable);
        }
        return Observable.concat(observableList).collect(new Supplier<List<Object[]>>() {
            @Override
            public List<Object[]> get() throws Throwable {
                return new ArrayList<>();
            }
        }, new BiConsumer<List<Object[]>, Object[]>() {
            @Override
            public void accept(List<Object[]> list, Object[] objectArr) throws Throwable {
                boolean result = (boolean) objectArr[1];
                if (result) {
                    list.add(objectArr);
                }
            }
        }).map(new Function<List<Object[]>, Boolean>() {
            @Override
            public Boolean apply(List<Object[]> objects) throws Throwable {
                String json = SPUtils.getInstance().getString(SP_KEY_UPLOAD_FAIL_APP);
                LogUtils.d("上报前数据:" + json);
                List<AppReasonBean> appReasonBeanList = GsonUtils.fromJson(json, new TypeToken<List<AppReasonBean>>() {
                }.getType());
                if (appReasonBeanList != null) {
                    ListIterator<AppReasonBean> listIterator = appReasonBeanList.listIterator();
                    while (listIterator.hasNext()) {
                        AppReasonBean appReasonBean = listIterator.next();
                        for (Object[] object : objects) {
                            String app_id = (String) object[0];
                            if (appReasonBean.app_id.equals(app_id)) {
                                listIterator.remove();
                                break;
                            }
                        }
                    }
                    String toJson = GsonUtils.toJson(appReasonBeanList);
                    LogUtils.d("上报后数据:" + toJson);
                    SPUtils.getInstance().put(SP_KEY_UPLOAD_FAIL_APP, toJson);
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
