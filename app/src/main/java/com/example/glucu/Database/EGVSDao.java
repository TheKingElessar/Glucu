package com.example.glucu.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.glucu.Database.EGVS;

import java.util.Date;
import java.util.List;

@Dao
public interface EGVSDao {
    @Query("SELECT * FROM egvs")
    List<EGVS> getAll();

 //   @Query("SELECT * FROM egvs WHERE systemTimeEGVS IN (:systemTimeEGVSs)")
//    List<EGVS> loadAllByTimes(List<Date> systemTimeEGVSs);

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Since the values should never change, it's okay to just ignore those already inserted (likewise, there's really no need for the update method)
    void insertList(List<EGVS> egvsList);

    @Insert
    void insert(EGVS egvs);

    @Update
    public void updateEGVSList(List<EGVS> egvsList);

    @Delete
    void delete(EGVS egvs);

    @Delete
    void deleteList(List<EGVS> egvsList);

}