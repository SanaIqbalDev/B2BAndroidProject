package com.codeseven.pos.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.codeseven.pos.database.Dao.CatalogItemDao;
import com.codeseven.pos.database.Dao.CategoryItemsDao;
import com.codeseven.pos.database.Entity.CategoryItem;
import com.codeseven.pos.model.CatalogItem;

@Database(entities = {CatalogItem.class, CategoryItem.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CategoryItemsDao categoryItemsDao();
    public abstract CatalogItemDao catalogItemDao();



}
