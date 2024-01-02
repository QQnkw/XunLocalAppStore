package com.xiaoxun.appstorelocal.repository;

import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.WorkerThread;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.google.gson.reflect.TypeToken;
import com.xiaoxun.appstorelocal.App;
import com.xiaoxun.appstorelocal.bean.AppInfo;
import com.xiaoxun.appstorelocal.bean.AppReasonBean;
import com.xiaoxun.sdk.IResponseDataCallBack;
import com.xiaoxun.sdk.ResponseData;
import com.xiaoxun.sdk.XiaoXunNetworkManager;
import com.xiaoxun.statistics.XiaoXunStatisticsManager;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;

public class NetRepository {
    private static volatile NetRepository sNetRepository;

    private NetRepository() {
    }

    public static NetRepository getInstance() {
        if (sNetRepository == null) {
            synchronized (NetRepository.class) {
                if (sNetRepository == null) {
                    sNetRepository = new NetRepository();
                }
            }
        }
        return sNetRepository;
    }

    public Observable<List<AppInfo>> queryAllAppInfoFromNet() {
        return Observable.create(new ObservableOnSubscribe<List<AppInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<AppInfo>> emitter) throws Throwable {
                XiaoXunNetworkManager xiaoXunNetworkManager = App.sApp.getXiaoXunNetworkManager();
                if (xiaoXunNetworkManager == null) {
                    emitter.onError(new Exception("小寻服务为空"));
                    return;
                }
                JSONObject inner = new JSONObject();
                inner.put(RequestKey.STRING_EID, xiaoXunNetworkManager.getWatchEid());
                JSONObject outer = new JSONObject();
                outer.put(RequestKey.INT_CID, 80141);
                outer.put(RequestKey.INT_SN, xiaoXunNetworkManager.getMsgSN());
                outer.put(RequestKey.STRING_SID, xiaoXunNetworkManager.getSID());
                outer.put(RequestKey.BEAN_PL, inner);
                LogUtils.d("请求服务器所有应用信息:" + (outer.toString()));
                xiaoXunNetworkManager.sendJsonMessage(outer.toString(), new IResponseDataCallBack.Stub() {
                    @Override
                    public void onSuccess(ResponseData responseData) throws RemoteException {
                        if (responseData == null) {
                            emitter.onError(new Exception("返回数据为空"));
                            return;
                        }
                        String responseDataTxt = responseData.getResponseData();
                        LogUtils.d("从服务器拉取应用信息:\n" + responseDataTxt);
                        if (TextUtils.isEmpty(responseDataTxt)) {
                            emitter.onError(new Exception("返回数据为空"));
                            return;
                        }
                        List<AppInfo> netList = GsonUtils.fromJson(responseDataTxt, new TypeToken<List<AppInfo>>() {
                        }.getType());
                        emitter.onNext(netList);
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(int i, String s) throws RemoteException {
                        emitter.onError(new Exception(s));
                    }
                });
            }
        });
    }

    @WorkerThread
    public Observable<Boolean> uploadAppOptype(AppReasonBean appReasonBean) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) throws Throwable {
                XiaoXunNetworkManager xiaoXunNetworkManager = App.sApp.getXiaoXunNetworkManager();
                if (xiaoXunNetworkManager == null) {
                    emitter.onError(new Exception("小寻服务为空"));
                    return;
                }
                AppInfo appInfo = App.sAppDatabase.appInfoDao().query(appReasonBean.app_id);
                appInfo.optype = appReasonBean.optype;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestKey.BEAN_PL, appInfo);
                jsonObject.put(RequestKey.STRING_SID, xiaoXunNetworkManager.getSID());
                jsonObject.put(RequestKey.INT_CID, 80131);
                jsonObject.put(RequestKey.INT_SN, xiaoXunNetworkManager.getMsgSN());
                LogUtils.d("上传应用安装状态:" + (jsonObject.toString()));
                xiaoXunNetworkManager.sendJsonMessage(jsonObject.toString(), new IResponseDataCallBack.Stub() {
                    @Override
                    public void onSuccess(ResponseData responseData) throws RemoteException {
                        emitter.onNext(true);
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(int i, String s) throws RemoteException {
                        emitter.onError(new Exception(s));
                    }
                });
            }
        });
    }

    public Observable<Boolean> uploadInstallAppSource(AppReasonBean appReasonBean) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) throws Throwable {
                XiaoXunStatisticsManager xiaoXunStatisticsManager = App.sApp.getXiaoXunStatisticsManager();
                if (xiaoXunStatisticsManager == null) {
                    emitter.onError(new Exception("小寻统计服务为空"));
                    return;
                }
                xiaoXunStatisticsManager.stats(appReasonBean.app_id
                        , appReasonBean.actionType,
                        appReasonBean.installSourceDevice + "," +
                                appReasonBean.installAppNetType);
                emitter.onNext(true);
                emitter.onComplete();
            }
        });
    }

    public Observable<Boolean> uploadInstallAppReason(AppReasonBean appReasonBean) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) throws Throwable {
                XiaoXunNetworkManager xiaoXunNetworkManager = App.sApp.getXiaoXunNetworkManager();
                if (xiaoXunNetworkManager == null) {
                    emitter.onError(new Exception("小寻服务为空"));
                    return;
                }
                xiaoXunNetworkManager.uploadNotice(
                        xiaoXunNetworkManager.getWatchEid(),
                        xiaoXunNetworkManager.getWatchGid(),
                        appReasonBean.installSourceDevice + "," + appReasonBean.installSourceAppId,
                        appReasonBean.reason,
                        new IResponseDataCallBack.Stub() {
                            @Override
                            public void onSuccess(ResponseData responseData) throws RemoteException {
                                emitter.onNext(true);
                                emitter.onComplete();
                            }

                            @Override
                            public void onError(int i, String s) throws RemoteException {
                                emitter.onError(new Exception(s));
                            }
                        }
                );
            }
        });
    }
}
