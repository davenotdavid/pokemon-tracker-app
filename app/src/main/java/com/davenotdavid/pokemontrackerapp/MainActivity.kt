package com.davenotdavid.pokemontrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.davenotdavid.pokemontrackerapp.ui.PokemonDetailScreen
import com.davenotdavid.pokemontrackerapp.ui.PokemonListScreen
import com.davenotdavid.pokemontrackerapp.ui.PokemonUiState
import com.davenotdavid.pokemontrackerapp.ui.PokemonViewModel
import com.davenotdavid.pokemontrackerapp.ui.theme.PokemonTrackerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokemonTrackerAppTheme {
                PokemonTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonTrackerApp(viewModel: PokemonViewModel = viewModel()) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pokémon Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "list",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("list") {
                PokemonListScreen(
                    uiState = uiState,
                    onPokemonClick = { pokemon -> navController.navigate("detail/${pokemon.id}") },
                    onToggleCaptured = { pokemon -> viewModel.toggleCaptured(pokemon) },
                    onRetry = { viewModel.loadPokemon() },
                )
            }
            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id")
                val pokemon = (uiState as? PokemonUiState.Success)?.pokemon?.find { it.id == id }
                if (pokemon != null) {
                    PokemonDetailScreen(
                        pokemon = pokemon,
                        onToggleCaptured = { viewModel.toggleCaptured(it) },
                    )
                }
            }
        }
    }
}