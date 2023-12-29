package com.xiaoxun.appstorelocal.repository;

import com.blankj.utilcode.util.LogUtils;
import com.xiaoxun.appstorelocal.App;
import com.xiaoxun.sdk.IResponseDataCallBack;
import com.xiaoxun.sdk.XiaoXunNetworkManager;

import org.json.JSONObject;

public class NetRepository {
    private static volatile NetRepository sNetRepository;
    private final String XIAO_XUN_NET_SERVICE = "xun.network.Service";
    private XiaoXunNetworkManager mXiaoXunNetworkManager;

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

    public void queryAllAppInfo(IResponseDataCallBack.Stub callBack) throws Exception {
        if (mXiaoXunNetworkManager == null) {
            mXiaoXunNetworkManager = (XiaoXunNetworkManager) App.sApp.getSystemService(XIAO_XUN_NET_SERVICE);
        }
        if (mXiaoXunNetworkManager == null) {
            LogUtils.e(new Exception("小寻服务为空"));
            return;
        }
        JSONObject inner = new JSONObject();
        inner.put(RequestKey.STRING_EID, mXiaoXunNetworkManager.getWatchEid());
        JSONObject outer = new JSONObject();
        outer.put(RequestKey.INT_CID, 80141);
        outer.put(RequestKey.INT_SN, mXiaoXunNetworkManager.getMsgSN());
        outer.put(RequestKey.STRING_SID, mXiaoXunNetworkManager.getSID());
        outer.put(RequestKey.BEAN_PL, inner);
        LogUtils.d("请求服务器所有应用信息:" + (outer.toString()));
        mXiaoXunNetworkManager.sendJsonMessage(outer.toString(), callBack);
    }
}
