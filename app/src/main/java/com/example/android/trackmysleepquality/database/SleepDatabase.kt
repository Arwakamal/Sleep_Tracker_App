package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/*RoomDatabase:Base class for all Room databases. All classes that are annotated with Database must extend this class.

RoomDatabase provides direct access to the underlying database implementation but you should prefer using Dao classes.*/
@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {

    abstract val sleepDatabaseDao: SleepDatabaseDao
/*
the companion object allows clients to access the methods for creating or getting the database without instantiating the class.
*/
    companion object {
/*-we declare a private nullable variable for the database and initialize it to null.
  -INSTANCE will keep a reference to the database once we have one.(This will help us avoid repeatedly opening connections to the database, which is expensive.)
  -@Volatile:This helps us make sure the value of INSTANCE is always up to date and the same to all execution threats.
* */
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getInstance(context: Context): SleepDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SleepDatabase::class.java,
                            "sleep_history_database"
                    )
                            /*Migration means, if we change the database schema, for example by changing the number or type of columns,
                             we need a way to convert the existing tables and data into the new schema.*/
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
