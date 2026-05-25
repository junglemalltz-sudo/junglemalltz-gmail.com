package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.*
import com.example.ui.CheckoutState
import com.example.ui.JungleMallViewModel
import com.example.ui.JungleMallViewModelFactory
import com.example.ui.theme.JungleMallTheme
import com.example.ui.theme.MpesaRed
import com.example.ui.theme.TigoBlue
import com.example.ui.theme.AirtelYellow
import com.example.ui.theme.HalopesaOrange
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JungleMallTheme {
                val context = LocalContext.current
                val database = remember { AppDatabase.getDatabase(context) }
                val repository = remember { JungleMallRepository(database.dao()) }
                val viewModel: JungleMallViewModel = viewModel(
                    factory = JungleMallViewModelFactory(repository)
                )

                JungleMallApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JungleMallApp(viewModel: JungleMallViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            JungleBottomBar(
                currentTab = currentTab,
                cartCount = cartItems.sumOf { it.quantity },
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "shop" -> ShopScreen(viewModel)
                "cart" -> CartScreen(viewModel, cartItems)
                "orders" -> OrdersScreen(viewModel, orders)
                "profile" -> ProfileScreen(viewModel, orders)
            }

            // Product Detail Overlay screen
            selectedProduct?.let { product ->
                ProductDetailOverlay(
                    product = product,
                    onDismiss = { viewModel.selectProduct(null) },
                    onAddToCart = { qty ->
                        viewModel.addToCart(product, qty)
                        viewModel.selectProduct(null)
                    }
                )
            }
        }
    }
}

@Composable
fun JungleBottomBar(
    currentTab: String,
    cartCount: Int,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == "shop",
            onClick = { onTabSelected("shop") },
            label = { Text("Shop") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Shop Tab") },
            modifier = Modifier.testTag("nav_shop_tab")
        )
        NavigationBarItem(
            selected = currentTab == "cart",
            onClick = { onTabSelected("cart") },
            label = { Text("Cart") },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge {
                                Text(
                                    text = cartCount.toString(),
                                    modifier = Modifier.testTag("cart_badge_count")
                                )
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart Tab")
                }
            },
            modifier = Modifier.testTag("nav_cart_tab")
        )
        NavigationBarItem(
            selected = currentTab == "orders",
            onClick = { onTabSelected("orders") },
            label = { Text("Orders") },
            icon = { Icon(Icons.Default.List, contentDescription = "Orders Tab") },
            modifier = Modifier.testTag("nav_orders_tab")
        )
        NavigationBarItem(
            selected = currentTab == "profile",
            onClick = { onTabSelected("profile") },
            label = { Text("Profile") },
            icon = { Icon(Icons.Default.Info, contentDescription = "Hub Info") },
            modifier = Modifier.testTag("nav_profile_tab")
        )
    }
}

// FORMAT TZS CURRENCY
fun formatTzs(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "${formatter.format(amount)} TZS"
}

@Composable
fun ShopScreen(viewModel: JungleMallViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // GEOMETRIC HEADER
        item {
            JungleHeader()
        }

        // SEAMLESS GEOMETRIC SEARCH BAR
        item {
            SearchBarAndFilters(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) }
            )
        }

        // TOP CATEGORIES GEOMETRIC GRID CHIPS
        item {
            CategoryChipsSelector(
                selectedCategory = selectedCategory,
                onSelectCategory = { viewModel.selectCategory(it) }
            )
        }

        // GEOMETRIC ATMOSPHERE PROMO CARD
        item {
            PromoCard()
        }

        // GEOMETRIC FEATURED FINDS SECTION HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategory == "All") "Featured Finds" else selectedCategory,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${products.size} found",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("featured_finds_refresh")
                )
            }
        }

        // PRODUCTS GRID LIST (Render 2 items per row with Geometric Balance layout card spacing)
        val productChunks = products.chunked(2)
        if (productChunks.isEmpty()) {
            item {
                EmptyShopState(searchQuery)
            }
        } else {
            items(productChunks) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductCard(
                        product = pair[0],
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_card_${pair[0].id}"),
                        onProductClick = { viewModel.selectProduct(pair[0]) },
                        onQuickAdd = { viewModel.addToCart(pair[0]) }
                    )
                    if (pair.size > 1) {
                        ProductCard(
                            product = pair[1],
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_card_${pair[1].id}"),
                            onProductClick = { viewModel.selectProduct(pair[1]) },
                            onQuickAdd = { viewModel.addToCart(pair[1]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Extra spacing at the bottom
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun JungleHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Elegant Geometric Square Logo Box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.jungle_logo_1779687761624),
                    contentDescription = "Jungle Mall Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Jungle Mall",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = "TANZANIA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // Action Buttons on Right
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(onClick = { /* notification action */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBarAndFilters(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("search_bar_input"),
        placeholder = { 
            Text(
                "Search products, brands...", 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 14.sp
            ) 
        },
        leadingIcon = { 
            Icon(
                Icons.Default.Search, 
                contentDescription = "Search Icon", 
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            ) 
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear inquiry", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        maxLines = 1,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun CategoryChipsSelector(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Top Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "See all",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { /* action */ }
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(Inventory.categories) { (cat, emoji) ->
                val isSelected = selectedCategory == cat
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelectCategory(cat) }
                        .testTag("category_chip_$cat")
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 26.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PromoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            // Top Right Decorative Circle
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 32.dp, y = (-32).dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), CircleShape)
            )

            // Bottom Right Decorative Circle
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 16.dp, y = 20.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            )

            // Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "WILD WEEKEND DEALS 🐆",
                    color = Color(0xFFC3EFC3),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "50% Off\nSafari Gear",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 26.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onProductClick: () -> Unit,
    onQuickAdd: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onProductClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat geometric look
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Elegant 1:1 Aspect Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val emoji = when (product.iconName) {
                    "shuka" -> "🧣"
                    "coffee" -> "☕"
                    "boots" -> "🥾"
                    "phone" -> "📱"
                    "dress" -> "👗"
                    "art" -> "🐘"
                    "spice" -> "🌿"
                    "honey" -> "🍯"
                    "sandals" -> "👡"
                    else -> "📦"
                }
                Text(text = emoji, fontSize = 42.sp)

                // Branch Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Text(
                        text = if (product.branch.contains("Arusha")) "Arusha" else if (product.branch.contains("Zanzibar")) "Zanzibar" else "Dar",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info details below image
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = product.category,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "★ ${product.rating}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${product.reviewsCount})",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTzs(product.price),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Quick Add Rounded-Square Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onQuickAdd() }
                            .testTag("quick_add_button_${product.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Item",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyShopState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🍃", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Hakuna bidhaa zilizopatikana (No items found)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Kupeleka ombi kwa \"$query\", tafadhali hariri maelezo (Try checking your spelling or selecting another category.)",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

// PRODUCT DETAILS SCREEN (STUNNING BOTTOM SHEET LAYOUT SIMULATION)
@Composable
fun ProductDetailOverlay(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onDismiss() } // Back out clicking outside
            .testTag("detail_overlay_scrim")
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) {} // block click propagation
                .testTag("detail_card_${product.id}"),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .background(Color.Gray.copy(alpha = 0.4f), CircleShape)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Title + Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.85f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_detail_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close description")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful Presentation Image/Background Box
                val detailBg = remember(product.iconName) {
                    when (product.iconName) {
                        "shuka" -> Brush.verticalGradient(listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2)))
                        "coffee" -> Brush.verticalGradient(listOf(Color(0xFFEFEBE9), Color(0xFFD7CCC8)))
                        "boots" -> Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))
                        "phone" -> Brush.verticalGradient(listOf(Color(0xFFE0F2F1), Color(0xFFB2DFDB)))
                        "dress" -> Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)))
                        "art" -> Brush.verticalGradient(listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC)))
                        "spice" -> Brush.verticalGradient(listOf(Color(0xFFFFFDE7), Color(0xFFFFF9C4)))
                        "honey" -> Brush.verticalGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)))
                        else -> Brush.verticalGradient(listOf(Color(0xFFF1F8E9), Color(0xFFDCEDC8)))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(detailBg),
                    contentAlignment = Alignment.Center
                ) {
                    val emoji = when (product.iconName) {
                        "shuka" -> "🧣"
                        "coffee" -> "☕"
                        "boots" -> "🥾"
                        "phone" -> "📱"
                        "dress" -> "👗"
                        "art" -> "🐘"
                        "spice" -> "🌿"
                        "honey" -> "🍯"
                        "sandals" -> "👡"
                        else -> "📦"
                    }
                    Text(text = emoji, fontSize = 84.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rating & Branch Location Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⭐ ${product.rating}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(text = " (${product.reviewsCount} genuine customer reviews)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "In Stock",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Warehouse Location Informative Tip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Pin Map",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Branch: ${product.branch} (Stock Dispatch Ready)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Details Text
                Text(
                    text = "Bidhaa Maelezo (Product Description)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Quantity & Add CTA Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // QTY SELECTOR
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(36.dp).testTag("detail_qty_minus")
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            text = quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .testTag("detail_qty_text"),
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(36.dp).testTag("detail_qty_plus")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase count", modifier = Modifier.size(16.dp))
                        }
                    }

                    // ADD PRICE SUMMARY + CTA
                    Button(
                        onClick = { onAddToCart(quantity) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                            .height(48.dp)
                            .testTag("detail_add_to_cart_cta"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Add to Cart • ${formatTzs(product.price * quantity)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// CART AND PAYMENT SCREEN
@Composable
fun CartScreen(viewModel: JungleMallViewModel, cartItems: List<CartItem>) {
    val checkoutState by viewModel.checkoutState.collectAsStateWithLifecycle()
    val cName by viewModel.customerName.collectAsStateWithLifecycle()
    val cPhone by viewModel.customerPhone.collectAsStateWithLifecycle()
    val payMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val shipping = if (subtotal > 0) 5000.0 else 0.0 // 5,000 TZS flat express shipping
    val totalAmount = subtotal + shipping

    if (checkoutState is CheckoutState.Success) {
        CheckoutSuccessScreen(
            order = (checkoutState as CheckoutState.Success).order,
            onClose = {
                viewModel.resetCheckoutState()
                viewModel.setTab("orders")
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Kikapu Chako (Your Cart)",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (cartItems.isEmpty()) {
            item {
                EmptyCartState(onReturnToShop = { viewModel.setTab("shop") })
            }
        } else {
            // Cart items list
            items(cartItems) { item ->
                CartListItemRow(
                    item = item,
                    onIncrement = { viewModel.updateCartQuantity(item.productId, item.quantity + 1) },
                    onDecrement = { viewModel.updateCartQuantity(item.productId, item.quantity - 1) },
                    onRemove = { viewModel.removeFromCart(item.productId) }
                )
            }

            // Price analysis
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PriceSummarySection(subtotal = subtotal, shipping = shipping, total = totalAmount)
            }

            // Billing configuration
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BillingSection(
                    customerName = cName,
                    customerPhone = cPhone,
                    selectedPaymentMethod = payMethod,
                    checkoutState = checkoutState,
                    onNameChange = { viewModel.customerName.value = it },
                    onPhoneChange = { viewModel.customerPhone.value = it },
                    onPaymentMethodChange = { viewModel.selectPaymentMethod(it) },
                    onCheckoutClick = { viewModel.performCheckout() }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun CartListItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("cart_item_${item.productId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom Emoji representation corresponding to background gradient
            val detailBg = remember(item.iconName) {
                when (item.iconName) {
                    "shuka" -> Color(0xFFFFCDD2)
                    "coffee" -> Color(0xFFD7CCC8)
                    "boots" -> Color(0xFFC8E6C9)
                    "phone" -> Color(0xFFB2DFDB)
                    "dress" -> Color(0xFFFFE0B2)
                    "art" -> Color(0xFFCFD8DC)
                    "spice" -> Color(0xFFFFF9C4)
                    "honey" -> Color(0xFFFFECB3)
                    else -> Color(0xFFDCEDC8)
                }
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(detailBg),
                contentAlignment = Alignment.Center
            ) {
                val emoji = when (item.iconName) {
                    "shuka" -> "🧣"
                    "coffee" -> "☕"
                    "boots" -> "🥾"
                    "phone" -> "📱"
                    "dress" -> "👗"
                    "art" -> "🐘"
                    "spice" -> "🌿"
                    "honey" -> "🍯"
                    "sandals" -> "👡"
                    else -> "📦"
                }
                Text(text = emoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatTzs(item.price)} each",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Total: ${formatTzs(item.price * item.quantity)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Qty adjustments & Delete
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("cart_remove_${item.productId}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove product",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, Color.Gray.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("cart_qty_minus_${item.productId}")
                    ) {
                        Text("-", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = item.quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .testTag("cart_qty_text_${item.productId}")
                    )
                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("cart_qty_plus_${item.productId}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add another", modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PriceSummarySection(subtotal: Double, shipping: Double, total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Hali ya Malipo (Payment Summary)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Subtotal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Text(text = formatTzs(subtotal), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Courier Fee (Express Hub)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Text(text = formatTzs(shipping), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.Gray.copy(alpha = 0.15f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Total Payable", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = formatTzs(total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("payable_total_text")
                )
            }
        }
    }
}

@Composable
fun BillingSection(
    customerName: String,
    customerPhone: String,
    selectedPaymentMethod: String,
    checkoutState: CheckoutState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onCheckoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Maelezo ya Agizo (Billing Details)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Name Input
            OutlinedTextField(
                value = customerName,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("checkout_name_input"),
                label = { Text("Jina kamili (Full Name)") },
                placeholder = { Text("Mfano: Juma Kibali") },
                maxLines = 1,
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            // Phone Input
            OutlinedTextField(
                value = customerPhone,
                onValueChange = onPhoneChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("checkout_phone_input"),
                label = { Text("Namba ya Simu (Phone)") },
                placeholder = { Text("Mfano: +255 754 XXXXXX") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                maxLines = 1,
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Text(
                text = "Njia ya Malipo (Payment Method)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // TANZANIAN PAYMENT CHOICES PILLS
            val paymentsList = listOf(
                "Vodacom M-Pesa" to MpesaRed,
                "Tigo Pesa" to TigoBlue,
                "Airtel Money" to AirtelYellow,
                "Halopesa" to HalopesaOrange
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                paymentsList.forEach { (name, nativeColor) ->
                    val isChosen = selectedPaymentMethod == name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isChosen) 2.dp else 1.dp,
                                color = if (isChosen) nativeColor else Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                color = if (isChosen) nativeColor.copy(alpha = 0.08f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onPaymentMethodChange(name) }
                            .padding(10.dp)
                            .testTag("payment_option_$name")
                    ) {
                        RadioButton(
                            selected = isChosen,
                            onClick = { onPaymentMethodChange(name) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = nativeColor
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(nativeColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = name,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (name.contains("M-Pesa")) "MOMoney" else "Token Pay",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Errors handling
            if (checkoutState is CheckoutState.Error) {
                Text(
                    text = (checkoutState as CheckoutState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("checkout_error_text")
                )
            }

            // Checkout CTA
            Button(
                onClick = onCheckoutClick,
                enabled = checkoutState !is CheckoutState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("place_order_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (checkoutState is CheckoutState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.0.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Order Sheet")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agiza Bidhaa Sasa (Place Order)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartState(onReturnToShop: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🛒", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Kikapu chako kina tupu (Your cart is empty)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Anza sasa kuongeza safari bidhaa na matoleo hapa chini.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        Button(
            onClick = onReturnToShop,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("return_to_shop_button")
        ) {
            Text("Gundua Bidhaa (Explore Marketplace)", fontWeight = FontWeight.SemiBold)
        }
    }
}

// SUCCESS SCREEN DISPLAY
@Composable
fun CheckoutSuccessScreen(order: Order, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎉", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Agizo Limepokelewa!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Order successfully verified & placed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray.copy(alpha = 0.15f))

                // Invoice analysis
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuccessRow(label = "Agizo Ref (Order ID)", value = "#TZ-992${order.id}A")
                    SuccessRow(label = "Mteja (Customer Name)", value = order.customerName)
                    SuccessRow(label = "Simu (Phone Number)", value = order.customerPhone)
                    SuccessRow(label = "Njia Malipo (Payment)", value = order.paymentMethod)
                    SuccessRow(label = "Jumla (Total Paid)", value = formatTzs(order.totalAmount), isTotal = true)
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray.copy(alpha = 0.15f))

                // Process indicator card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Syncing",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Awaiting verification SMS on ${order.customerPhone}. Dispatch processing within 1–3 hours.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("checkout_success_done_button"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Tazama Maagizo ya Nyuma (View Orders)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SuccessRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = if (isTotal) Color.Black else Color.Gray, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 13.sp, fontWeight = if (isTotal) FontWeight.Black else FontWeight.Medium, color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

// ORDERS LIST HISTORY SCREEN
@Composable
fun OrdersScreen(viewModel: JungleMallViewModel, orders: List<Order>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Maagizo Yaliyopita (Orders History)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (orders.isEmpty()) {
            EmptyOrdersState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orders) { order ->
                    OrderHistoryItemCard(order)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun OrderHistoryItemCard(order: Order) {
    val dateString = remember(order.orderTimestamp) {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        sdf.format(java.util.Date(order.orderTimestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_history_card_${order.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #TZ-992${order.id}A",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = dateString,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Processing",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.Gray.copy(alpha = 0.1f))

            Text(
                text = "Vitu (Purchased items):",
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = order.itemsSummary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.Gray.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Billing Name: ${order.customerName}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Payment: ${order.paymentMethod}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = formatTzs(order.totalAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyOrdersState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📦", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bado hujaagiza bidhaa yoyote (No orders yet)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Historia ya maagizo yako itaonekana hapa pale utakaponunua bidhaa kutoka kikapuni chako.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, start = 20.dp, end = 20.dp)
        )
    }
}

// PROFILE AND HUBS DETAILS SCREEN
@Composable
fun ProfileScreen(viewModel: JungleMallViewModel, orders: List<Order>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Kuhusu Jungle Mall (Hubs & Info)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Customer Profile short summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "JM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Jungle Explorer Profile",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Total Orders: ${orders.size} dispatched",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tanzanian Hub locations details Card
        Text(
            text = "Matawi ya Kuchukulia (Pickup Hubs Locations)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        HubLocationRow(
            title = "DSM Main Hub • Slipway & Mlimani City",
            details = "Dar es Salaam, Tanzania. Open Everyday 8AM - 10PM. Tele: +255 22 232 4001\nFeatures wide collection of local artifacts, clothing, and electronics.",
            emoji = "🏙️"
        )
        Spacer(modifier = Modifier.height(8.dp))
        HubLocationRow(
            title = "Arusha Northern Circuit Hub",
            details = "Clock Tower Plaza, Arusha, Tanzania. Open 7AM - 9PM. Tele: +255 27 254 8000\nSpecializes in handcarved ebony craftwork, authentic Maasai beaded products, and rugged boots.",
            emoji = "🌋"
        )
        Spacer(modifier = Modifier.height(8.dp))
        HubLocationRow(
            title = "Zanzibar Darajani Market Depot",
            details = "Stone Town, Zanzibar. Open Everyday 8AM - 8PM. Tele: +255 24 223 9010\nHub for traditional Zanzibar prints, aromatic Island cloves, spices, and handmade sandals.",
            emoji = "🏖️"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Terms info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Gharama na Huduma za Usafirishaji (Shipping Scope)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "1. Siku 1 Usafirishaji Dar es Salaam, Arusha & Zanzibar hubs.\n2. Malipo yote yanaidhinishwa kupitia mitandao salama ya simu (M-Pesa, Tigo-Pesa, Airtel-Money, Halopesa).\n3. Rejesha bidhaa ndani ya siku 7 ikiwa haijakidhi ubora uliyoomba.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun HubLocationRow(title: String, details: String, emoji: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(text = emoji, fontSize = 24.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = details,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
