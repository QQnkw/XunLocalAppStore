package com.xiaoxun.appstorelocal.bean;

public class AppReasonBean {
    public String app_id;//应用包名
    public String name;//应用名称
    public String reason;//安装信息说明
    public String actionType;//install或uninstall
    public int optype;//数据库字段  0:add  1:modify  2:delete
    public String installSourceDevice;//安装应用的命令来源APP或WATCH
    public String installSourceAppId;//安装应用的命令来源哪个应用
    public String installAppNetType;//安装应用的时的网络类型
}
