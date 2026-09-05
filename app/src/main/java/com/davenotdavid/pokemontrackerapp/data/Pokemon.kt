package com.davenotdavid.pokemontrackerapp.data

data class Pokemon(
    val id: Int,
    val name: String,
    val type: List<String> = emptyList(),
    val hp: Int = 0,
    val isCaptured: Boolean = false,
)