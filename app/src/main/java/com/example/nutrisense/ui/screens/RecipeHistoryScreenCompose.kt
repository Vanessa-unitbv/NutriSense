package com.example.nutrisense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrisense.ui.theme.NutriSenseColors
import com.example.nutrisense.ui.theme.NutriSenseTheme

data class RecipeHistoryState(
    val recipes: List<RecipeItem> = emptyList(),
    val totalRecipes: Int = 0,
    val favoriteCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeHistoryScreenCompose(
    state: RecipeHistoryState,
    onRecipeClick: (RecipeItem) -> Unit,
    onFavoriteClick: (RecipeItem) -> Unit,
    onDeleteClick: (RecipeItem) -> Unit,
    onBackClick: () -> Unit
) {
    NutriSenseTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "❤️ Saved Recipes",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NutriSenseColors.Brown
                    )
                )
            },
            containerColor = NutriSenseColors.Background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NutriSenseColors.CardOrange),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryItem(
                            emoji = "📖",
                            value = "${state.totalRecipes}",
                            label = "Total Recipes"
                        )
                        SummaryItem(
                            emoji = "❤️",
                            value = "${state.favoriteCount}",
                            label = "Favorites"
                        )
                    }
                }

                state.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriSenseColors.Error.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "❌ $error",
                            modifier = Modifier.padding(12.dp),
                            color = NutriSenseColors.Error,
                            fontSize = 14.sp
                        )
                    }
                }

                state.successMessage?.let { success ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriSenseColors.Success.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "✅ $success",
                            modifier = Modifier.padding(12.dp),
                            color = NutriSenseColors.Success,
                            fontSize = 14.sp
                        )
                    }
                }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NutriSenseColors.Brown)
                    }
                } else if (state.recipes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "��",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No saved recipes yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = NutriSenseColors.Brown
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Search for recipes and save your favorites!",
                                fontSize = 14.sp,
                                color = NutriSenseColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.recipes) { recipe ->
                            SavedRecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe) },
                                onFavoriteClick = { onFavoriteClick(recipe) },
                                onDeleteClick = { onDeleteClick(recipe) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    emoji: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp
        )
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedRecipeCard(
    recipe: RecipeItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isFavorite by remember(recipe) { mutableStateOf(recipe.isFavorite) }

    LaunchedEffect(key1 = recipe.isFavorite) {
        isFavorite = recipe.isFavorite
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NutriSenseColors.CardOrange),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🍽️",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = recipe.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Row {
                    TextButton(onClick = {
                        isFavorite = !isFavorite
                        onFavoriteClick()
                    }) {
                        Text(
                            text = if (isFavorite) "❤️" else "🤍",
                            fontSize = 20.sp
                        )
                    }
                    TextButton(onClick = onDeleteClick) {
                        Text(
                            text = "🗑️",
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recipe.ingredients,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
