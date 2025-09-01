package com.easyranktools.callhistoryforanynumber.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert
    void insertUser(Favorite favorite);

    @Query("SELECT * FROM favorite_table")
    List<Favorite> getAllUsers();

}
