package com.reciperoulette.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reciperoulette.data.RecipeResult
import com.reciperoulette.viewmodel.FavoritesState
import com.reciperoulette.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    onSignInClick: () -> Unit,
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val state by favoritesViewModel.state.collectAsStateWithLifecycle()
    val selectedTab by favoritesViewModel.selectedTab.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row
        TabRow(
            selectedTabIndex = if (selectedTab == "favorites") 0 else 1
        ) {
            Tab(
                selected = selectedTab == "favorites",
                onClick = { favoritesViewModel.selectTab("favorites") },
                icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                text = { Text("Favorites") }
            )
            Tab(
                selected = selectedTab == "history",
                onClick = { favoritesViewModel.selectTab("history") },
                icon = { Icon(Icons.Default.History, contentDescription = null) },
                text = { Text("History") }
            )
        }

        when (val s = state) {
            is FavoritesState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is FavoritesState.NotSignedIn -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔒", fontSize = 56.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Sign in to see your saved recipes",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onSignInClick) {
                            Text("Sign In / Register")
                        }
                    }
                }
            }

            is FavoritesState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕 ${s.message}")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { favoritesViewModel.load() }) { Text("Retry") }
                    }
                }
            }

            is FavoritesState.Success -> {
                if (s.recipes.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (selectedTab == "favorites") "💔" else "📭",
                                fontSize = 56.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (selectedTab == "favorites")
                                    "No favorites yet.\nHit ♥ on a recipe to save it here!"
                                else
                                    "No recipe history yet.\nScan your fridge to get started!",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(s.recipes, key = { it.recipeName }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                showRemove = selectedTab == "favorites",
                                onRemove = { favoritesViewModel.removeFavorite(recipe) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: RecipeResult,
    showRemove: Boolean,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.recipeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = recipe.selectedIngredients.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${recipe.steps.size} steps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (showRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from favorites",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
