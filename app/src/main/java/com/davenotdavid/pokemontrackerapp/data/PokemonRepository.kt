package com.davenotdavid.pokemontrackerapp.data

import com.davenotdavid.pokemontrackerapp.network.PokemonService

class PokemonRepository(private val service: PokemonService) {

    suspend fun getAll(): List<Pokemon> = service.getAll()

    suspend fun setCaptured(pokemon: Pokemon, captured: Boolean): Pokemon =
        service.update(pokemon.id, pokemon.copy(isCaptured = captured))
}