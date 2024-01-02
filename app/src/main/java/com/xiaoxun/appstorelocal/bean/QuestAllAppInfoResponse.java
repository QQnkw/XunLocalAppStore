package com.xiaoxun.appstorelocal.bean;

import java.util.List;

public class QuestAllAppInfoResponse {


    /**
     * PL : {"List":[{"icon":"com.xxun.watch.studywords","optype":1,"updateTS":"20231226151020591","status":0,"attr":"0","EID":"2BBFCA8DCDAC44DFB6C1DAD06D40B240","type":2,"GID":"DE09985E7A01BA0F634B2EDE4267DF7F","version_code":1,"version":"1.0.0.20230605","size":0,"wifi":0,"hidden":0,"download_url":"","name":"背单词","md5":"","sub_action":0,"app_id":"com.xxun.watch.studywords"},{"icon":"com.xxun.sport.xunsports2","optype":1,"updateTS":"20231226151020626","status":0,"attr":"0","EID":"2BBFCA8DCDAC44DFB6C1DAD06D40B240","type":2,"GID":"DE09985E7A01BA0F634B2EDE4267DF7F","version_code":23,"version":"2.1.0.20230421","size":0,"wifi":0,"hidden":0,"download_url":"","name":"运动","md5":"","sub_action":0,"app_id":"com.xxun.sport.xunsports2"},{"icon":"com.xxun.watch.xunqrcode","optype":1,"updateTS":"20231226151020627","status":0,"attr":"0","EID":"2BBFCA8DCDAC44DFB6C1DAD06D40B240","type":2,"GID":"DE09985E7A01BA0F634B2EDE4267DF7F","version_code":19,"version":"4.4.4-eng.niukangwei.20230718.114524","size":0,"wifi":0,"hidden":0,"download_url":"","name":"二维码","md5":"","sub_action":0,"app_id":"com.xxun.watch.xunqrcode"},{"icon":"https://webfile.cdn.bcebos.com/appstore/com.xxun.watch.dialstore/2021-07-06%2014:40:50/icon/ic_launcher.png","optype":1,"updateTS":"20231229173013588","status":0,"attr":"0","EID":"2BBFCA8DCDAC44DFB6C1DAD06D40B240","type":2,"GID":"DE09985E7A01BA0F634B2EDE4267DF7F","version_code":37,"version":"1.4.0.20231012","size":10322475,"wifi":0,"hidden":0,"download_url":"https://webfile.cdn.bcebos.com/appstore/com.xxun.watch.dialstore/20230914110903/apk/DialStore_v1.4.0.20230913_v4_4.apk","name":"表盘商店","md5":"f37f6011bf3282831c39337985afb9fb","sub_action":0,"app_id":"com.xxun.watch.dialstore"},{"icon":"com.xxun.watch.xuncalculator","optype":1,"updateTS":"20231226151020774","status":0,"attr":"0","EID":"2BBFCA8DCDAC44DFB6C1DAD06D40B240","type":2,"GID":"DE09985E7A01BA0F634B2EDE4267DF7F","version_code":1,"version":"1.0.0.20230425","size":0,"wifi":0,"hidden":0,"download_url":"","name":"计算器","md5":"","sub_action":0,"app_id":"com.xxun.watch.xuncalculator"},{"icon":"com.ximalayaos.wearkid.study","optype":1,"updateTS":"20231226151020774","status":0,"attr":"0","EID":"2BBFCA8DCDAC44DFB6C1DAD06D40B240","type":2,"GID":"DE09985E7A01BA0F634B2EDE4267DF7F","version_code":68068,"version":"4.1.04.03","size":0,"wifi":0,"hidden":0,"download_url":"","name":"早晚听","md5":"","sub_action":0,"app_id":"com.ximalayaos.wearkid.study"},{"icon":"com.xxun.watch.stepstart","optype":1,"updateTS":"20231226151021447","status":0,"attr":"0","EID":"2BBFCA8DCDAC44DFB6C1DAD06D40B240","type":2,"GID":"DE09985E7A01BA0F634B2EDE4267DF7F","version_code":19,"version":"4.4.4-eng.niukangwei.20230718.114524","size":0,"wifi":0,"hidden":0,"download_url":"","name":"运动计步","md5":"","sub_action":0,"app_id":"com.xxun.watch.stepstart"},{"icon":"com.xiaoxun.appstorelocal","optype":1,"updateTS":"20231226151021447","status":0,"attr":"0","EID":"2BBFCA8DCDAC44DFB6C1DAD06D40B240","type":2,"GID":"DE09985E7A01BA0F634B2EDE4267DF7F","version_code":19,"version":"4.4.4-eng.niukangwei.20230718.114524","size":0,"wifi":0,"hidden":0,"download_url":"","name":"应用商店","md5":"","sub_action":0,"app_id":"com.xiaoxun.appstorelocal"}]}
     * SN : 394593790
     * Version : 01410000
     * RC : 1
     * CID : 80142
     */

    private PLBean PL;
    private int SN;
    private String Version;
    private int RC;
    private int CID;

    public PLBean getPL() {
        return PL;
    }

    public void setPL(PLBean PL) {
        this.PL = PL;
    }

    public int getSN() {
        return SN;
    }

    public void setSN(int SN) {
        this.SN = SN;
    }

    public String getVersion() {
        return Version;
    }

    public void setVersion(String Version) {
        this.Version = Version;
    }

    public int getRC() {
        return RC;
    }

    public void setRC(int RC) {
        this.RC = RC;
    }

    public int getCID() {
        return CID;
    }

    public void setCID(int CID) {
        this.CID = CID;
    }

    public static class PLBean {
        private List<AppInfo> List;

        public List<AppInfo> getList() {
            return List;
        }

        public void setList(List<AppInfo> List) {
            this.List = List;
        }
    }
}
