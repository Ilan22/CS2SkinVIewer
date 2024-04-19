package fr.nextu.licha_ilan.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skin")
data class Skin(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "weapon") val weapon: Weapon,
    @ColumnInfo(name = "category") val category: Category,
    @ColumnInfo(name = "rarity") val rarity: Rarity,
    @ColumnInfo(name = "stattrak") val stattrak: Boolean,
    @ColumnInfo(name = "image") val image: String
)

data class Weapon(
    val name: String
)

data class Category(
    val id: String,
    val name: String
)

data class Rarity(
    val name: String,
    val color: String
)