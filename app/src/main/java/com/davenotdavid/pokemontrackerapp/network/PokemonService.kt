package com.davenotdavid.pokemontrackerapp.network

import com.davenotdavid.pokemontrackerapp.data.Pokemon
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface PokemonService {

    @GET("pokemon")
    suspend fun getAll(): List<Pokemon>

    @PUT("pokemon/{id}")
    suspend fun update(@Path("id") id: Int, @Body pokemon: Pokemon): Pokemon
}