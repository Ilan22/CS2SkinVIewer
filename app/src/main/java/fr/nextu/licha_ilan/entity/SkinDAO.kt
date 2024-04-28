package fr.nextu.licha_ilan.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface SkinDAO {

    /**
     * Skin
     */
    @RawQuery(observedEntities = [Skin::class])
    fun getFlowData(sortQuery: SupportSQLiteQuery): Flow<List<Skin>>

    @Insert
    fun insertSkin(skin: Skin): Long

    @Query("SELECT * FROM skins WHERE id = :id")
    fun getSkinById(id: String?): Skin?

    @Query("SELECT id FROM skins WHERE category_id = :categoryId ORDER BY RANDOM() LIMIT 1")
    fun getRandomSkinIdByCategory(categoryId: String): String

    @Query("DELETE FROM skins")
    fun deleteAllSkins()

    @Query("SELECT COUNT(*) FROM skins")
    suspend fun countSkins(): Int

    suspend fun isTableEmpty(): Boolean = countSkins() == 0

    /**
     * Category
     */
    @Query("DELETE FROM categories")
    fun deleteAllCategories()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
   fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: String?): Category?

    @Query("SELECT DISTINCT * FROM categories")
    fun getCategories(): List<Category>

    /**
     * Rarity
     */
    @Query("SELECT * FROM rarities ORDER BY name")
    fun getAllRarities(): List<Rarity>

    @Query("DELETE FROM rarities")
    fun deleteAllRarities()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRarity(rarity: Rarity): Long

    @Query("SELECT * FROM rarities WHERE name = :name")
    fun getRarityByName(name: String?): Rarity?
}
