package fr.nextu.licha_ilan.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.json.JSONObject

@Database(entities = [Skin::class, Category::class, Rarity::class, Settings::class], version = 1)
@TypeConverters(
    WeaponTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun skinDao(): SkinDAO
    abstract fun settingsDao(): SettingsDAO

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