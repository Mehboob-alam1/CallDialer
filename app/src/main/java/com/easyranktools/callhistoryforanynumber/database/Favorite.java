package com.easyranktools.callhistoryforanynumber.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_table")
public class Favorite {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String mobile;

    public Favorite(String username, String mobile) {
        this.username = username;
        this.mobile = mobile;
    }
}
