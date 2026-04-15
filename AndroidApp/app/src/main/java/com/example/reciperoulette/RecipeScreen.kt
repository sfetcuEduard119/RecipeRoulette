package com.reciperoulette.ui.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reciperoulette.viewmodel.RecipeState
import com.reciperoulette.viewmodel.RecipeViewModel

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel,
    onScanAgain: () -> Unit
) {
    val state by viewModel.recipeState.collectAsStateWithLifecycle()
    val saveMessage by viewModel.saveMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Snackbar for save/favorite feedback
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(saveMessage) {
        saveMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        when (val s = state) {
            is RecipeState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Spinning the roulette... 🎲", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            is RecipeState.Error -> {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😬 Oops!", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(s.message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onScanAgain) { Text("Try Again") }
                    }
                }
            }

            is RecipeState.Success -> {
                val recipe = s.recipe
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // Hero header
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    )
                                    .padding(24.dp)
                            ) {
                                Column {
                                    Text(
                                        "🍽️",
                                        fontSize = 48.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = recipe.recipeName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        // Action buttons row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Save button
                                FilledTonalButton(
                                    onClick = { viewModel.saveCurrentRecipe() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Save,
                                        contentDescription = "Save",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Save")
                                }

                                // Favorite button
                                FilledTonalButton(
                                    onClick = { viewModel.toggleFavorite() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (recipe.isFavorite)
                                            MaterialTheme.colorScheme.errorContainer
                                        else
                                            MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (recipe.isFavorite) Icons.Default.Favorite
                                                      else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        modifier = Modifier.size(18.dp),
                                        tint = if (recipe.isFavorite)
                                            MaterialTheme.colorScheme.error
                                        else
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (recipe.isFavorite) "Saved ♥" else "Favorite")
                                }

                                // Share button
                                FilledTonalButton(
                                    onClick = { viewModel.shareRecipe(context) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Share",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share")
                                }
                            }
                        }

                        // All detected ingredients
                        item {
                            SectionCard(title = "🔍 All Detected Ingredients") {
                                recipe.allIngredients.forEach { ingredient ->
                                    val isSelected = ingredient in recipe.selectedIngredients
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isSelected) "✅" else "⬜",
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = ingredient,
                                            fontWeight = if (isSelected) FontWeight.SemiBold
                                                         else FontWeight.Normal,
                                            color = if (isSelected)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Selected ingredients
                        item {
                            SectionCard(title = "🎲 Roulette Pick (${recipe.selectedIngredients.size} ingredients)") {
                                recipe.selectedIngredients.forEach { ingredient ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(ingredient) },
                                        modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        }

                        // Steps
                        item {
                            SectionCard(title = "👨‍🍳 Steps") {
                                recipe.steps.forEachIndexed { index, step ->
                                    Row(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = step,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Chef tip
                        item {
                            SectionCard(title = "💡 Chef's Tip") {
                                Text(
                                    text = recipe.chefTip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Scan again
                        item {
                            Button(
                                onClick = {
                                    viewModel.reset()
                                    onScanAgain()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(52.dp)
                            ) {
                                Text("🎲 Spin Again!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            else -> Unit
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            content()
        }
    }
}
