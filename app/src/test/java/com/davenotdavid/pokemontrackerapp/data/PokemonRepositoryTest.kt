package com.davenotdavid.pokemontrackerapp.data

import com.davenotdavid.pokemontrackerapp.data.local.PokemonDao
import com.davenotdavid.pokemontrackerapp.network.PokemonService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.IOException

class PokemonRepositoryTest {

    private val service = mockk<PokemonService>()
    private val dao = mockk<PokemonDao>(relaxUnitFun = true)
    private lateinit var repository: PokemonRepository

    private val pokemon = Pokemon(id = 1, name = "Bulbasaur", type = listOf("Grass", "Poison"), hp = 45)

    @Before
    fun setUp() {
        repository = PokemonRepository(service, dao)
    }

    @Test
    fun `refreshFromNetwork writes the network result through to the cache`() = runTest {
        coEvery { service.getAll() } returns listOf(pokemon)

        val result = repository.refreshFromNetwork()

        assertEquals(listOf(pokemon), result)
        coVerify { dao.upsertAll(listOf(pokemon)) }
    }

    @Test
    fun `getCached reads straight from the DAO`() = runTest {
        coEvery { dao.getAll() } returns listOf(pokemon)

        assertEquals(listOf(pokemon), repository.getCached())
    }

    @Test
    fun `setCaptured persists the server response on success`() = runTest {
        val updated = pokemon.copy(isCaptured = true)
        coEvery { service.update(pokemon.id, pokemon.copy(isCaptured = true)) } returns updated

        val result = repository.setCaptured(pokemon, true)

        assertEquals(updated, result)
        coVerify { dao.upsertAll(listOf(updated)) }
    }

    @Test
    fun `setCaptured falls back to a local update when the network call fails`() = runTest {
        coEvery { service.update(any(), any()) } throws IOException("offline")

        val result = repository.setCaptured(pokemon, true)

        val expected = pokemon.copy(isCaptured = true)
        assertEquals(expected, result)
        coVerify { dao.upsertAll(listOf(expected)) }
    }

    @Test
    fun `setCaptured rethrows cancellation instead of swallowing it`() {
        coEvery { service.update(any(), any()) } throws CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runTest { repository.setCaptured(pokemon, true) }
        }

        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }
}