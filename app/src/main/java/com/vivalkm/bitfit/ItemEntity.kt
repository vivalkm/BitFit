package com.vivalkm.bitfit

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_table")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "amount") val amount: Float,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "isLiked") val isLiked: Int
)