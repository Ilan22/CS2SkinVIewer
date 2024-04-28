package fr.nextu.licha_ilan.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skins")
data class Skin(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "weapon") val weapon: Weapon,
    @Embedded(prefix = "category_") val category: Category,
    @Embedded(prefix = "rarity_") val rarity: Rarity,
    @ColumnInfo(name = "stattrak") val stattrak: Boolean,
    @ColumnInfo(name = "image") val image: String
)

data class Weapon(
    val name: String
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String
)

@Entity(tableName = "rarities")
data class Rarity(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "color") val color: String
)