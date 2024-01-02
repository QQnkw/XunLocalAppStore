package com.xiaoxun.appstorelocal.bean;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Fts4
@Entity
public class AppInfo {
    /**
     * icon : com.xxun.watch.studywords
     * optype : 1
     * updateTS : 20231226151020591
     * status : 0
     * attr : 0
     * EID : 2BBFCA8DCDAC44DFB6C1DAD06D40B240
     * type : 2
     * GID : DE09985E7A01BA0F634B2EDE4267DF7F
     * version_code : 1
     * version : 1.0.0.20230605
     * size : 0
     * wifi : 0
     * hidden : 0
     * download_url :
     * name : 背单词
     * md5 :
     * sub_action : 0
     * app_id : com.xxun.watch.studywords
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    public int id;
    public String icon;//应用图标字段
    public int optype;//数据库字段  0:add  1:modify  2:delete
    public String updateTS;//跟新数据时间
    public int status;//    安装状态    ；  3  是卸载、    2更新；1安装；  0  已安装
    public String attr; //标记高功耗应用    0  不是，  1  是
    public String EID;//设备EID
    public int type;//  标记应用类别    2  系统应用  1是三方应用
    public String GID;//设备GID
    public int version_code;//应用版本
    public String version; //应用版本号
    public int size; //应用大小
    public int wifi;//  是否使用WiFi:0,1;1表示连接
    public int hidden; //应用显示隐藏    0  显示、1隐藏
    public String download_url; //应用下载网址
    public String name;//应用名
    public String md5;//MD5  码
    @Ignore
    public int sub_action;
    public String app_id;//应用包名
}
