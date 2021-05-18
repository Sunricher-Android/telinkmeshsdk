package com.sunricher.telinkblemeshlib.db;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
interface MeshAddressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(MeshAddress addresses);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MeshAddress address);

    @Query("DELETE FROM MeshAddress WHERE address = :address AND name LIKE :name AND password LIKE:password")
    void delete(int address, String name, String password);

    @Query("DELETE FROM MeshAddress WHERE name LIKE :name AND password LIKE:password")
    void deleteAll(String name, String password);

    @Query("SELECT * FROM MeshAddress WHERE name LIKE :name AND password LIKE:password")
    List<MeshAddress> selectAll(String name, String password);

    @Query("SELECT * FROM MeshAddress WHERE address = :address AND name LIKE :name AND password LIKE:password")
    List<MeshAddress> select(int address, String name, String password);

}
