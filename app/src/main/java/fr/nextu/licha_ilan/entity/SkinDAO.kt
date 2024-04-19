package fr.nextu.licha_ilan.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SkinDAO {
    @Query("SELECT * FROM skin")
    fun getAll(): List<Skin>

    @Query("SELECT * FROM skin WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Skin>

//    @Query("SELECT * FROM skin WHERE title LIKE :title LIMIT 1")
//    fun findByTitle(title: String): Skin

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg skins: Skin)

    @Delete
    fun delete(skin: Skin)

    @Query("SELECT * FROM skin")
    fun getFlowData(): Flow<List<Skin>>

    @Query("SELECT * FROM skin WHERE id = :id")
    fun get(id: Int): Skin

    @Query("SELECT * FROM skin WHERE id = :id")
    fun getFlow(id: Int): Flow<Skin>
}