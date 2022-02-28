package com.codeseven.pos.database.Dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.codeseven.pos.model.CatalogItem;

import java.util.List;

@Dao
public interface CatalogItemDao {

    @Query("SELECT * FROM catalogitem")
    List<CatalogItem> getAll();
}
