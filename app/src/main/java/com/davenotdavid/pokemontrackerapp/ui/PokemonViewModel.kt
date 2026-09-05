package com.davenotdavid.pokemontrackerapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davenotdavid.pokemontrackerapp.data.Pokemon
import com.davenotdavid.pokemontrackerapp.data.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PokemonUiState {
    data object Loading : PokemonUiState
    data class Error(val message: String) : PokemonUiState
    data class Success(val pokemon: List<Pokemon>) : PokemonUiState
}

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val repository: PokemonRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Loading)
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()

    init {
        loadPokemon()
    }

    fun loadPokemon() {
        viewModelScope.launch {
            _uiState.value = PokemonUiState.Loading
            _uiState.value = try {
                PokemonUiState.Success(repository.getAll().sortedBy { it.id })
            } catch (e: Exception) {
                PokemonUiState.Error(e.message ?: "Failed to load Pokemon")
            }
        }
    }

    fun toggleCaptured(pokemon: Pokemon) {
        val newCaptured = !pokemon.isCaptured
        updateLocalPokemon(pokemon.copy(isCaptured = newCaptured))

        viewModelScope.launch {
            try {
                val updated = repository.setCaptured(pokemon, newCaptured)
                updateLocalPokemon(updated)
            } catch (ex: Exception) {
                updateLocalPokemon(pokemon)
            }
        }
    }

    private fun updateLocalPokemon(updated: Pokemon) {
        _uiState.update { state ->
            if (state is PokemonUiState.Success) {
                PokemonUiState.Success(state.pokemon.map { if (it.id == updated.id) updated else it })
            } else {
                state
            }
        }
    }
}