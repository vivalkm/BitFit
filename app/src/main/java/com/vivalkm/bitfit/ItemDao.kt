package com.vivalkm.bitfit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM item_table")
    fun getAll(): Flow<List<ItemEntity>>

    @Insert
    fun insert(item: ItemEntity)

    @Query("DELETE FROM item_table WHERE id = :id")
    fun delete(id: Long)

    @Query("UPDATE item_table SET isLiked = :isLiked WHERE id = :id")
    fun update(id: Long, isLiked: Int)
}