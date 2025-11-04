package com.runningcity.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.runningcity.data.local.dao.WorkoutDao
import com.runningcity.data.local.entity.*

@Database(
    entities = [
        WorkoutSessionEntity::class,
        HeartRateRecordEntity::class,
        LocationRecordEntity::class,
        CadenceRecordEntity::class,
        CalorieRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                    .fallbackToDestructiveMigration()  // 개발 중에는 이렇게, 프로덕션에서는 Migration 필요
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}