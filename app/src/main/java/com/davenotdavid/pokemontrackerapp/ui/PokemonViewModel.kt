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
    data class Success(val pokemon: List<Pokemon>, val isOffline: Boolean = false) : PokemonUiState
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
                // Network first: if it succeeds, the repository has already refreshed the cache too.
                PokemonUiState.Success(repository.refreshFromNetwork().sortedBy { it.id })
            } catch (ex: Exception) {
                // Offline or the API is unreachable: fall back to whatever was cached last time.
                val cached = repository.getCached().sortedBy { it.id }
                if (cached.isNotEmpty()) {
                    PokemonUiState.Success(cached, isOffline = true)
                } else {
                    PokemonUiState.Error(ex.message ?: "Failed to load Pokemon")
                }
            }
        }
    }

    fun toggleCaptured(pokemon: Pokemon) {
        viewModelScope.launch {
            val updated = repository.setCaptured(pokemon, !pokemon.isCaptured)
            _uiState.update { state ->
                if (state is PokemonUiState.Success) {
                    state.copy(pokemon = state.pokemon.map { if (it.id == updated.id) updated else it })
                } else {
                    state
                }
            }
        }
    }
}