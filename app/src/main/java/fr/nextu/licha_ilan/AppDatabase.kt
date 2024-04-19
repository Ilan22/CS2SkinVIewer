package fr.nextu.licha_ilan

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import fr.nextu.licha_ilan.entity.Category
import fr.nextu.licha_ilan.entity.Rarity
import fr.nextu.licha_ilan.entity.Skin
import fr.nextu.licha_ilan.entity.SkinDAO
import fr.nextu.licha_ilan.entity.Weapon
import org.json.JSONObject

@Database(entities = [Skin::class], version = 1)
@TypeConverters(WeaponTypeConverter::class, CategoryTypeConverter::class, RarityTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun skinDao(): SkinDAO

    companion object {
        fun getInstance(applicationContext: Context): AppDatabase {
            return Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "cs2_skin_viewer.db"
            ).build()
        }
    }
}

class WeaponTypeConverter {
    @TypeConverter
    fun fromWeapon(weapon: Weapon): String {
        return JSONObject().apply {
            put("name", weapon.name)
        }.toString()
    }

    @TypeConverter
    fun toWeapon(weapon: String): Weapon {
        val json = JSONObject(weapon)
        return Weapon(json.getString("name"))
    }
}

class CategoryTypeConverter {
    @TypeConverter
    fun fromCategory(category: Category): String {
        return JSONObject().apply {
            put("id", category.id?: "")
            put("name", category.name?: "")
        }.toString()
    }

    @TypeConverter
    fun toCategory(category: String): Category {
        val json = JSONObject(category)
        return Category(json.getString("id"), json.getString("name"))
    }
}

class RarityTypeConverter {
    @TypeConverter
    fun fromRarity(rarity: Rarity): String {
        return JSONObject().apply {
            put("name", rarity.name)
            put("color", rarity.color)
        }.toString()
    }

    @TypeConverter
    fun toRarity(rarity: String): Rarity {
        val json = JSONObject(rarity)
        return Rarity(json.getString("name"), json.getString("color"))
    }
}