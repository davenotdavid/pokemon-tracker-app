package com.davenotdavid.pokemontrackerapp.data

import com.davenotdavid.pokemontrackerapp.data.local.PokemonDao
import com.davenotdavid.pokemontrackerapp.network.PokemonService
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val service: PokemonService,
    private val dao: PokemonDao,
) {

    suspend fun refreshFromNetwork(): List<Pokemon> {
        val pokemon = service.getAll()
        dao.upsertAll(pokemon)
        return pokemon
    }

    suspend fun getCached(): List<Pokemon> = dao.getAll()

    suspend fun setCaptured(pokemon: Pokemon, captured: Boolean): Pokemon {
        val updated = try {
            service.update(pokemon.id, pokemon.copy(isCaptured = captured))
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            Timber.w(ex, "Failed to sync captured state for Pokemon %d, applying it locally only", pokemon.id)
            pokemon.copy(isCaptured = captured)
        }
        dao.upsertAll(listOf(updated))
        return updated
    }
}