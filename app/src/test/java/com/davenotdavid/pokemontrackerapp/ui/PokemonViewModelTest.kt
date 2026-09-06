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
import org.junit.Assert.assertNull
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
    fun `LoadPokemon emits pokemon sorted by id when the network succeeds`() = runTest {
        coEvery { repository.refreshFromNetwork() } returns listOf(charmander, bulbasaur)

        val viewModel = PokemonViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(listOf(bulbasaur, charmander), state.pokemon)
        assertFalse(state.isLoading)
        assertFalse(state.isOffline)
        assertNull(state.errorMessage)
    }

    @Test
    fun `LoadPokemon falls back to the cache and flags offline when the network fails`() = runTest {
        coEvery { repository.refreshFromNetwork() } throws IOException("no network")
        coEvery { repository.getCached() } returns listOf(bulbasaur)

        val viewModel = PokemonViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(listOf(bulbasaur), state.pokemon)
        assertTrue(state.isOffline)
        assertNull(state.errorMessage)
    }

    @Test
    fun `LoadPokemon surfaces an error when the network fails and nothing is cached`() = runTest {
        coEvery { repository.refreshFromNetwork() } throws IOException("boom")
        coEvery { repository.getCached() } returns emptyList()

        val viewModel = PokemonViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals("boom", state.errorMessage)
        assertTrue(state.pokemon.isEmpty())
    }

    @Test
    fun `ToggleCaptured updates only the toggled pokemon`() = runTest {
        coEvery { repository.refreshFromNetwork() } returns listOf(bulbasaur, charmander)
        val updatedBulbasaur = bulbasaur.copy(isCaptured = true)
        coEvery { repository.setCaptured(bulbasaur, true) } returns updatedBulbasaur

        val viewModel = PokemonViewModel(repository)
        viewModel.onIntent(PokemonIntent.ToggleCaptured(bulbasaur))

        val state = viewModel.uiState.value
        assertEquals(updatedBulbasaur, state.pokemon.first { it.id == bulbasaur.id })
        assertEquals(charmander, state.pokemon.first { it.id == charmander.id })
    }
}