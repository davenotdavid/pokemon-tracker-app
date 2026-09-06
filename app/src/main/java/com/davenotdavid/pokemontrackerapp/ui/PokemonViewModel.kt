package com.davenotdavid.pokemontrackerapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davenotdavid.pokemontrackerapp.data.Pokemon
import com.davenotdavid.pokemontrackerapp.data.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class PokemonUiState(
    val pokemon: List<Pokemon> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PokemonIntent {
    data object LoadPokemon : PokemonIntent
    data object Refresh : PokemonIntent
    data class ToggleCaptured(val pokemon: Pokemon) : PokemonIntent
}

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val repository: PokemonRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonUiState(isLoading = true))
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()

    init {
        onIntent(PokemonIntent.LoadPokemon)
    }

    fun onIntent(intent: PokemonIntent) {
        when (intent) {
            is PokemonIntent.LoadPokemon, is PokemonIntent.Refresh -> loadPokemon()
            is PokemonIntent.ToggleCaptured -> toggleCaptured(intent.pokemon)
        }
    }

    private fun loadPokemon() {
        viewModelScope.launch {
            // Only blank the screen on the very first load. A pull-to-refresh while Pokemon are
            // already showing should keep them on screen and just spin the refresh indicator.
            val isFirstLoad = _uiState.value.pokemon.isEmpty()
            _uiState.update {
                if (isFirstLoad) it.copy(isLoading = true, errorMessage = null) else it.copy(isRefreshing = true)
            }

            try {
                // Network first: if it succeeds, the repository has already refreshed the cache too.
                val pokemon = repository.refreshFromNetwork().sortedBy { it.id }
                _uiState.update {
                    it.copy(
                        pokemon = pokemon,
                        isLoading = false,
                        isRefreshing = false,
                        isOffline = false,
                        errorMessage = null,
                    )
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                // Offline or the API is unreachable: fall back to whatever was cached last time.
                Timber.w(ex, "Failed to refresh Pokemon from the network")
                val cached = repository.getCached().sortedBy { it.id }
                _uiState.update {
                    if (cached.isNotEmpty()) {
                        it.copy(pokemon = cached, isLoading = false, isRefreshing = false, isOffline = true)
                    } else {
                        it.copy(isLoading = false, isRefreshing = false, errorMessage = ex.message ?: "Failed to load Pokemon")
                    }
                }
            }
        }
    }

    private fun toggleCaptured(pokemon: Pokemon) {
        viewModelScope.launch {
            val updated = repository.setCaptured(pokemon, !pokemon.isCaptured)
            _uiState.update { state ->
                state.copy(pokemon = state.pokemon.map { if (it.id == updated.id) updated else it })
            }
        }
    }
}