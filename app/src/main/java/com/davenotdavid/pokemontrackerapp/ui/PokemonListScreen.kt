package com.davenotdavid.pokemontrackerapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davenotdavid.pokemontrackerapp.data.Pokemon
import com.davenotdavid.pokemontrackerapp.ui.theme.PokemonTrackerAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    uiState: PokemonUiState,
    onPokemonClick: (Pokemon) -> Unit,
    onIntent: (PokemonIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.errorMessage, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { onIntent(PokemonIntent.Refresh) }) { Text("Retry") }
                }
            }
        }

        else -> {
            val capturedCount = uiState.pokemon.count { it.isCaptured }
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { onIntent(PokemonIntent.Refresh) },
                modifier = modifier.fillMaxSize(),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Captured $capturedCount / ${uiState.pokemon.size}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                    if (uiState.isOffline) {
                        Text(
                            text = "Offline — showing cached data",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.pokemon, key = { it.id }) { pokemon ->
                            PokemonRow(
                                pokemon = pokemon,
                                onClick = { onPokemonClick(pokemon) },
                                onToggleCaptured = { onIntent(PokemonIntent.ToggleCaptured(pokemon)) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonRow(
    pokemon: Pokemon,
    onClick: () -> Unit,
    onToggleCaptured: () -> Unit,
) {
    ListItem(
        headlineContent = { Text("#${pokemon.id} ${pokemon.name}") },
        supportingContent = { Text(pokemon.type.joinToString(", ")) },
        trailingContent = {
            Checkbox(checked = pokemon.isCaptured, onCheckedChange = { onToggleCaptured() })
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

private val previewPokemon = listOf(
    Pokemon(id = 1, name = "Bulbasaur", type = listOf("Grass", "Poison"), hp = 45, isCaptured = true),
    Pokemon(id = 4, name = "Charmander", type = listOf("Fire"), hp = 39),
    Pokemon(id = 7, name = "Squirtle", type = listOf("Water"), hp = 44),
)

@Preview(showBackground = true)
@Composable
private fun PokemonListScreenSuccessPreview() {
    PokemonTrackerAppTheme {
        PokemonListScreen(
            uiState = PokemonUiState(pokemon = previewPokemon),
            onPokemonClick = {},
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonListScreenOfflinePreview() {
    PokemonTrackerAppTheme {
        PokemonListScreen(
            uiState = PokemonUiState(pokemon = previewPokemon, isOffline = true),
            onPokemonClick = {},
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonListScreenLoadingPreview() {
    PokemonTrackerAppTheme {
        PokemonListScreen(
            uiState = PokemonUiState(isLoading = true),
            onPokemonClick = {},
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonListScreenErrorPreview() {
    PokemonTrackerAppTheme {
        PokemonListScreen(
            uiState = PokemonUiState(errorMessage = "Unable to reach the server"),
            onPokemonClick = {},
            onIntent = {},
        )
    }
}