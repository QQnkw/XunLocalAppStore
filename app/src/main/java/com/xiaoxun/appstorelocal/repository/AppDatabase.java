package com.xiaoxun.appstorelocal.repository;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.xiaoxun.appstorelocal.bean.AppInfo;

@Database(entities = {AppInfo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppInfoDao appInfoDao();
}
