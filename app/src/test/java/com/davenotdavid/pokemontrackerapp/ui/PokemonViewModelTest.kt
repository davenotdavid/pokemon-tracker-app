package com.davenotdavid.pokemontrackerapp.ui

import com.davenotdavid.pokemontrackerapp.data.Pokemon
import com.davenotdavid.pokemontrackerapp.data.PokemonRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModelTest {

    private val repository = mockk<PokemonRepository>()

    private val bulbasaur = Pokemon(id = 1, name = "Bulbasaur", type = listOf("Grass", "Poison"), hp = 45)
    private val charmander = Pokemon(id = 4, name = "Charmander", type = listOf("Fire"), hp = 39)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPokemon emits Success sorted by id when the network succeeds`() = runTest {
        coEvery { repository.refreshFromNetwork() } returns listOf(charmander, bulbasaur)

        val viewModel = PokemonViewModel(repository)

        val state = viewModel.uiState.value as PokemonUiState.Success
        assertEquals(listOf(bulbasaur, charmander), state.pokemon)
        assertFalse(state.isOffline)
    }

    @Test
    fun `loadPokemon falls back to the cache and flags offline when the network fails`() = runTest {
        coEvery { repository.refreshFromNetwork() } throws IOException("no network")
        coEvery { repository.getCached() } returns listOf(bulbasaur)

        val viewModel = PokemonViewModel(repository)

        val state = viewModel.uiState.value as PokemonUiState.Success
        assertEquals(listOf(bulbasaur), state.pokemon)
        assertTrue(state.isOffline)
    }

    @Test
    fun `loadPokemon emits Error when the network fails and nothing is cached`() = runTest {
        coEvery { repository.refreshFromNetwork() } throws IOException("boom")
        coEvery { repository.getCached() } returns emptyList()

        val viewModel = PokemonViewModel(repository)

        val state = viewModel.uiState.value
        assertTrue(state is PokemonUiState.Error)
        assertEquals("boom", (state as PokemonUiState.Error).message)
    }

    @Test
    fun `toggleCaptured updates only the toggled pokemon`() = runTest {
        coEvery { repository.refreshFromNetwork() } returns listOf(bulbasaur, charmander)
        val updatedBulbasaur = bulbasaur.copy(isCaptured = true)
        coEvery { repository.setCaptured(bulbasaur, true) } returns updatedBulbasaur

        val viewModel = PokemonViewModel(repository)
        viewModel.toggleCaptured(bulbasaur)

        val state = viewModel.uiState.value as PokemonUiState.Success
        assertEquals(updatedBulbasaur, state.pokemon.first { it.id == bulbasaur.id })
        assertEquals(charmander, state.pokemon.first { it.id == charmander.id })
    }
}