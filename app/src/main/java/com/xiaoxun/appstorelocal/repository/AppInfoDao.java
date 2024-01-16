package com.xiaoxun.appstorelocal.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.xiaoxun.appstorelocal.bean.AppInfo;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface AppInfoDao {

    @Insert
    Completable insertOnIO(List<AppInfo> appInfoList);

    @Insert
    void insert(List<AppInfo> appInfoList);

    @Delete
    Completable deleteOnIO(AppInfo appInfo);

    @Delete
    void delete(AppInfo appInfo);

    @Update
    Completable updateOnIO(List<AppInfo> appInfoList);

    @Update
    void update(List<AppInfo> appInfoList);

    @Query("SELECT * FROM AppInfo")
    Single<List<AppInfo>> queryAllOnIO();

    @Query("SELECT * FROM AppInfo")
    List<AppInfo> queryAll();

    @Query("SELECT * FROM AppInfo WHERE app_id = :app_id")
    Single<AppInfo> queryOnIO(String app_id);

    @Query("SELECT * FROM AppInfo WHERE app_id = :app_id")
    AppInfo query(String app_id);

    @Query("DELETE FROM AppInfo")
    void emptyTable();

    /**
     * 通过distinctUntilChanged() 运算符，可以确保仅在实际查询结果发生更改时通知界面。
     */
    @Query("SELECT * FROM AppInfo")
    Flowable<List<AppInfo>> queryAllWhenTableDataChangeOnIO();
}
