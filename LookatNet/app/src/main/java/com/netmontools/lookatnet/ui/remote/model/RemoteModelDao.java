package com.netmontools.lookatnet.ui.remote.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface  RemoteModelDao {
    @Query("SELECT * FROM RemoteModel")
    LiveData<List<RemoteModel>> getAll();

    @Query("SELECT * FROM RemoteModel WHERE id = :id")
    RemoteModel getById(long id);

    @Query("SELECT * FROM RemoteModel WHERE addr = :addr")
    RemoteModel getByAddr(String addr);

    @Query("SELECT * FROM RemoteModel WHERE bssid = :bssid AND addr = :addr")
    RemoteModel getByBssidAndAddress(String bssid, String addr);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(RemoteModel remoteDataModel);

    @Update
    void update(RemoteModel remoteDataModel);

    @Delete
    void delete(RemoteModel remoteDataModel);
}
