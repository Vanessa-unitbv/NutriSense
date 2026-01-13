package com.example.nutrisense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSearchScreen(
    onBackClick: () -> Unit,
    onSearchClick: (ingredients: String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    recipes: List<RecipeItem> = emptyList(),
    onRecipeClick: (recipe: RecipeItem) -> Unit = {},
    onFavoriteClick: (recipe: RecipeItem) -> Unit = {},
    onDeleteClick: (recipe: RecipeItem) -> Unit = {}
) {
    var ingredients by remember { mutableStateOf("") }
    var ingredientsError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        TopAppBar(
            title = { Text("Search Recipes", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6200EE)
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Find Recipes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Ingredients Input
                        OutlinedTextField(
                            value = ingredients,
                            onValueChange = {
                                ingredients = it
                                ingredientsError = ""
                            },
                            label = { Text("Ingredients (comma separated)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = false,
                            maxLines = 3,
                            isError = ingredientsError.isNotEmpty(),
                            supportingText = {
                                if (ingredientsError.isNotEmpty()) {
                                    Text(ingredientsError, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        // Search Button
                        Button(
                            onClick = {
                                if (ingredients.isEmpty()) {
                                    ingredientsError = "Ingredients cannot be empty!"
                                } else {
                                    onSearchClick(ingredients)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6200EE)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Search Recipes", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Error Message
            if (!errorMessage.isNullOrEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = "⚠️ $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Empty State
            if (recipes.isEmpty() && !isLoading && errorMessage == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🍳", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
                            Text(
                                "No recipes yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Search for ingredients to discover recipes",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Recipes List
            items(recipes) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onCardClick = { onRecipeClick(recipe) },
                    onFavoriteClick = { onFavoriteClick(recipe) },
                    onDeleteClick = { onDeleteClick(recipe) }
                )
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: RecipeItem,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Recipe Title and Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Row {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (recipe.isFavorite) Color(0xFFE91E63) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Recipe Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "👥 ${recipe.servings} servings",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Ingredients Preview
            Text(
                text = "Ingredients:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = recipe.ingredients.take(100) + if (recipe.ingredients.length > 100) "..." else "",
                fontSize = 11.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // View Details Button
            Button(
                onClick = onCardClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("View Recipe", fontSize = 12.sp)
            }
        }
    }
}

data class RecipeItem(
    val id: Int,
    val title: String,
    val ingredients: String,
    val instructions: String,
    val servings: Int,
    val isFavorite: Boolean = false
)
