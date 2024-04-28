package fr.nextu.licha_ilan.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDAO {
    @Query("SELECT language FROM settings LIMIT 1")
    fun getLanguage(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(settings: Settings): Long
}