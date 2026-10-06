package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RehanGold
import com.example.ui.theme.RehanGreenSuccess

data class CategoryInfo(
    val name: String,
    val icon: ImageVector,
    val offer: String,
    val itemCount: String,
    val tagColor: Color
)

@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit
) {
    val categoryList = listOf(
        CategoryInfo("Mobiles", Icons.Default.PhoneAndroid, "Up to 40% OFF", "Latest 5G, Flagships", Color(0xFF2563EB)),
        CategoryInfo("Electronics", Icons.Default.Headphones, "Min 50% OFF", "Audio, Smartwatches", Color(0xFF7C3AED)),
        CategoryInfo("Fashion", Icons.Default.Checkroom, "Starts @ ₹299", "Men, Women, Ethnic", Color(0xFFDB2777)),
        CategoryInfo("Footwear", Icons.Default.DirectionsRun, "Under ₹999", "Sneakers, Formal, Sports", Color(0xFFD97706)),
        CategoryInfo("Home & Kitchen", Icons.Default.Kitchen, "Up to 65% OFF", "Cookware, Decor", Color(0xFF059669)),
        CategoryInfo("Grocery", Icons.Default.LocalGroceryStore, "Flat 30% OFF", "Dry Fruits, Daily Needs", Color(0xFF16A34A)),
        CategoryInfo("Accessories", Icons.Default.Watch, "Buy 1 Get 1", "Belts, Wallets, Caps", Color(0xFFEA580C)),
        CategoryInfo("Personal Care", Icons.Default.Spa, "Best Combos", "Skincare, Grooming", Color(0xFF0284C7))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "All Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Browse curated departments across Rehan Store",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(categoryList) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable { onCategoryClick(item.name) }
                        .testTag("category_card_${item.name}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(item.tagColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name,
                                    tint = item.tagColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RehanGreenSuccess.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.offer,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RehanGreenSuccess
                                )
                            }
                        }

                        Column {
                            Text(
                                text = item.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.itemCount,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }
        }
    }
}
