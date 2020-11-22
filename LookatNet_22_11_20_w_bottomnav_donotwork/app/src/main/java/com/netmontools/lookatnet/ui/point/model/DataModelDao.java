package com.netmontools.lookatnet.ui.point.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DataModelDao {
    @Query("SELECT * FROM DataModel")
    LiveData<List<DataModel>> getAll();

    @Query("SELECT * FROM DataModel WHERE id = :id")
    DataModel getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DataModel dataModel);

    @Update
    void update(DataModel dataModel);

    @Delete
    void delete(DataModel dataModel);
}

