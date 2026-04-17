package com.avish.pashupatinath.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animal_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AnimalRecord>>

    @Insert
    suspend fun insertRecord(record: AnimalRecord)

    @Query("SELECT COUNT(*) FROM animal_records WHERE timestamp > :since")
    suspend fun getRecentCount(since: Long): Int

    @Query("DELETE FROM animal_records")
    suspend fun clearAll()
}

@Database(entities = [AnimalRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pashupatinath_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
