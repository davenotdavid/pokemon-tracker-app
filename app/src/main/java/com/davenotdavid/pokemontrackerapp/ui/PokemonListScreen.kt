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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davenotdavid.pokemontrackerapp.data.Pokemon

@Composable
fun PokemonListScreen(
    uiState: PokemonUiState,
    onPokemonClick: (Pokemon) -> Unit,
    onToggleCaptured: (Pokemon) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is PokemonUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PokemonUiState.Error -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.message, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRetry) { Text("Retry") }
                }
            }
        }

        is PokemonUiState.Success -> {
            val capturedCount = uiState.pokemon.count { it.isCaptured }
            Column(modifier = modifier.fillMaxSize()) {
                Text(
                    text = "Captured $capturedCount / ${uiState.pokemon.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.pokemon, key = { it.id }) { pokemon ->
                        PokemonRow(
                            pokemon = pokemon,
                            onClick = { onPokemonClick(pokemon) },
                            onToggleCaptured = { onToggleCaptured(pokemon) },
                        )
                        HorizontalDivider()
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