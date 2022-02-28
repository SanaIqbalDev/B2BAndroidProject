package com.codeseven.pos.database.Dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.codeseven.pos.database.Entity.CategoryItem;
import com.codeseven.pos.model.CatalogItem;

import java.util.List;

@Dao
public interface CategoryItemsDao {

    @Query("SELECT * FROM categoryitem WHERE categoryId LIKE :categoryId")
    List<CategoryItem> loadAllByCategoryId(String categoryId);


}
