package com.davenotdavid.pokemontrackerapp.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromTypeList(types: List<String>): String = types.joinToString(",")

    @TypeConverter
    fun toTypeList(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split(",")
}