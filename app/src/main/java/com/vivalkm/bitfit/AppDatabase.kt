package com.vivalkm.bitfit

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// The class must be an abstract class that extends RoomDatabase
@Database(entities = [ItemEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    // For each DAO class that is associated with the database,
    // the database class must define an abstract method that has zero arguments
    // and returns an instance of the DAO class
    abstract fun itemDao(): ItemDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "Items-db"
            ).fallbackToDestructiveMigration().build()
    }
}