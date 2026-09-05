package com.davenotdavid.pokemontrackerapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davenotdavid.pokemontrackerapp.data.Pokemon
import com.davenotdavid.pokemontrackerapp.ui.theme.PokemonTrackerAppTheme

@Composable
fun PokemonDetailScreen(
    pokemon: Pokemon,
    onToggleCaptured: (Pokemon) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "#${pokemon.id} ${pokemon.name}", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Type: ${pokemon.type.joinToString(", ")}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "HP: ${pokemon.hp}", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = if (pokemon.isCaptured) "Captured" else "Not captured",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = { onToggleCaptured(pokemon) },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(if (pokemon.isCaptured) "Mark as not captured" else "Mark as captured")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonDetailScreenNotCapturedPreview() {
    PokemonTrackerAppTheme {
        PokemonDetailScreen(
            pokemon = Pokemon(id = 1, name = "Bulbasaur", type = listOf("Grass", "Poison"), hp = 45),
            onToggleCaptured = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonDetailScreenCapturedPreview() {
    PokemonTrackerAppTheme {
        PokemonDetailScreen(
            pokemon = Pokemon(id = 4, name = "Charmander", type = listOf("Fire"), hp = 39, isCaptured = true),
            onToggleCaptured = {},
        )
    }
}