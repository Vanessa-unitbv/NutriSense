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
import com.example.nutrisense.ui.components.NutriSenseButton
import com.example.nutrisense.ui.components.NutriSenseTextField
import com.example.nutrisense.ui.theme.NutriSenseColors
import com.example.nutrisense.ui.theme.NutriSenseTheme

data class RecipeSearchState(
    val ingredients: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val recipes: List<RecipeItem> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSearchScreenCompose(
    state: RecipeSearchState,
    onIngredientsChange: (String) -> Unit,
    onSearchClick: () -> Unit,
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
                            text = "🔍 Search Recipes",
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🥗 Find recipes by ingredients",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        NutriSenseTextField(
                            value = state.ingredients,
                            onValueChange = onIngredientsChange,
                            label = "Enter ingredients (comma separated)",
                            singleLine = false
                        )

                        NutriSenseButton(
                            text = "Search Recipes",
                            onClick = onSearchClick,
                            isLoading = state.isLoading
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

                if (state.recipes.isEmpty() && !state.isLoading) {
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
                                text = "🍳",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Enter ingredients to find recipes",
                                fontSize = 16.sp,
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
                            RecipeSearchCard(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeSearchCard(
    recipe: RecipeItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
                Text(
                    text = recipe.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    TextButton(onClick = onFavoriteClick) {
                        Text(
                            text = if (recipe.isFavorite) "❤️" else "🤍",
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
                text = "📝 ${recipe.ingredients}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (recipe.instructions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👨‍🍳 ${recipe.instructions}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
