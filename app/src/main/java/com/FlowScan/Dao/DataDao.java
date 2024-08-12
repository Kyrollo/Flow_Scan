package com.FlowScan.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.FlowScan.Table.Data;

import java.util.List;

@Dao
public interface DataDao {
    @Insert
    void insert(Data data);

    @Query("DELETE FROM data")
    void deleteAll();

    @Query("SELECT * FROM data")
    List<Data> getAllData();

    @Query("SELECT * FROM data WHERE barcode = :barcode")
    Data getDataByBarcode(String barcode);
}
