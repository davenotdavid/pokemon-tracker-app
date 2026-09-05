package com.davenotdavid.pokemontrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon")
data class Pokemon(
    @PrimaryKey val id: Int,
    val name: String,
    val type: List<String> = emptyList(),
    val hp: Int = 0,
    val isCaptured: Boolean = false,
)