package com.example.nutrisense.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nutrisense.data.entity.DayOfWeek
import com.example.nutrisense.data.entity.MealType
import com.example.nutrisense.ui.theme.NutriSenseColors
import com.example.nutrisense.ui.theme.NutriSenseTheme

data class MealPlanItem(
    val id: Long,
    val recipeId: Long,
    val recipeTitle: String,
    val recipeIngredients: String,
    val dayOfWeek: Int,
    val mealType: String,
    val isFavorite: Boolean = false
)

data class MealPlanScreenState(
    val mealPlans: List<MealPlanItem> = emptyList(),
    val availableRecipes: List<RecipeItem> = emptyList(),
    val selectedDay: DayOfWeek = DayOfWeek.MONDAY,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreenCompose(
    state: MealPlanScreenState,
    onDaySelected: (DayOfWeek) -> Unit,
    onAddRecipeToDay: (RecipeItem, DayOfWeek, MealType) -> Unit,
    onRemoveMealPlan: (Long) -> Unit,
    onClearDay: (DayOfWeek) -> Unit,
    onSearchRecipesClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showAddRecipeDialog by remember { mutableStateOf(false) }

    NutriSenseTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "📅 Meal Planner",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { onClearDay(state.selectedDay) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Day", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NutriSenseColors.Brown
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddRecipeDialog = true },
                    containerColor = NutriSenseColors.Brown,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                }
            },
            containerColor = NutriSenseColors.Background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                DaySelector(
                    selectedDay = state.selectedDay,
                    onDaySelected = onDaySelected,
                    mealPlans = state.mealPlans
                )

                state.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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

                val mealsForDay = state.mealPlans.filter { it.dayOfWeek == state.selectedDay.value }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NutriSenseColors.Brown)
                    }
                } else if (mealsForDay.isEmpty()) {
                    EmptyDayContent(
                        dayName = state.selectedDay.displayName,
                        onAddClick = { showAddRecipeDialog = true }
                    )
                } else {
                    MealsList(
                        meals = mealsForDay,
                        onRemoveMeal = onRemoveMealPlan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (showAddRecipeDialog) {
            AddRecipeDialog(
                availableRecipes = state.availableRecipes,
                selectedDay = state.selectedDay,
                onDismiss = {
                    showAddRecipeDialog = false
                },
                onAddRecipe = { recipe, mealType ->
                    onAddRecipeToDay(recipe, state.selectedDay, mealType)
                    showAddRecipeDialog = false
                },
                onSearchRecipesClick = onSearchRecipesClick
            )
        }
    }
}

@Composable
private fun DaySelector(
    selectedDay: DayOfWeek,
    onDaySelected: (DayOfWeek) -> Unit,
    mealPlans: List<MealPlanItem>
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DayOfWeek.values().forEach { day ->
            val isSelected = day == selectedDay
            val mealsCount = mealPlans.count { it.dayOfWeek == day.value }

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) NutriSenseColors.Brown else NutriSenseColors.CardOrange,
                label = "dayBackground"
            )

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .clickable { onDaySelected(day) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day.displayName.take(3),
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = Color.White
                )
                if (mealsCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$mealsCount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDayContent(
    dayName: String,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📭",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No meals planned for $dayName",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = NutriSenseColors.Brown,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to add recipes from your collection",
                fontSize = 14.sp,
                color = NutriSenseColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NutriSenseColors.Brown
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Recipe")
            }
        }
    }
}

@Composable
private fun MealsList(
    meals: List<MealPlanItem>,
    onRemoveMeal: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        MealType.values().forEach { mealType ->
            val mealsOfType = meals.filter { it.mealType == mealType.value }
            if (mealsOfType.isNotEmpty()) {
                item {
                    MealTypeHeader(mealType)
                }
                items(mealsOfType) { meal ->
                    MealCard(
                        meal = meal,
                        onRemove = { onRemoveMeal(meal.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MealTypeHeader(mealType: MealType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mealType.emoji,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = mealType.displayName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NutriSenseColors.Brown
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealCard(
    meal: MealPlanItem,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NutriSenseColors.CardOrange),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.recipeTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meal.recipeIngredients,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRecipeDialog(
    availableRecipes: List<RecipeItem>,
    selectedDay: DayOfWeek,
    onDismiss: () -> Unit,
    onAddRecipe: (RecipeItem, MealType) -> Unit,
    onSearchRecipesClick: () -> Unit
) {
    var selectedRecipe by remember { mutableStateOf<RecipeItem?>(null) }
    var selectedMealType by remember { mutableStateOf<MealType?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NutriSenseColors.Background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add to ${selectedDay.displayName}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NutriSenseColors.Brown
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Meal Type Selection
                Text(
                    text = "Select Meal Type:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NutriSenseColors.Brown
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealType.values().forEach { mealType ->
                        val isSelected = selectedMealType == mealType
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMealType = mealType },
                            label = {
                                Text(
                                    text = "${mealType.emoji} ${mealType.displayName}",
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NutriSenseColors.Brown,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Recipe:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NutriSenseColors.Brown
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (availableRecipes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📭",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No recipes available",
                                fontSize = 16.sp,
                                color = NutriSenseColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Search for recipes first!",
                                fontSize = 14.sp,
                                color = NutriSenseColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    onDismiss()
                                    onSearchRecipesClick()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NutriSenseColors.Brown
                                )
                            ) {
                                Text("🔍 Search Recipes")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableRecipes) { recipe ->
                            val isSelected = selectedRecipe?.id == recipe.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRecipe = recipe },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        NutriSenseColors.Brown
                                    else
                                        NutriSenseColors.CardOrange
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 1.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (recipe.isFavorite) "❤️" else "🍽️",
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = recipe.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = recipe.ingredients,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        selectedRecipe?.let { recipe ->
                            selectedMealType?.let { mealType ->
                                onAddRecipe(recipe, mealType)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedRecipe != null && selectedMealType != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NutriSenseColors.Brown
                    )
                ) {
                    Text("Add to Meal Plan")
                }
            }
        }
    }
}
