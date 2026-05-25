package com.example.data

data class Product(
    val id: String,
    val name: String,
    val price: Double, // TZS
    val category: String,
    val iconName: String, // "shuka", "coffee", "boots", "phone", "dress", "art", "spice", "honey"
    val rating: Float,
    val reviewsCount: Int,
    val description: String,
    val isAvailable: Boolean = true,
    val branch: String = "Dar es Salaam Hub"
)

object Inventory {
    val categories = listOf(
        "All" to "🏪",
        "Local Crafts" to "🎨",
        "Fashion" to "👕",
        "Outdoors" to "🐆",
        "Groceries" to "🍍",
        "Electronics" to "📱"
    )

    val products = listOf(
        Product(
            id = "p1",
            name = "Maasai Shúkà Blanket",
            price = 35000.0,
            category = "Local Crafts",
            iconName = "shuka",
            rating = 4.9f,
            reviewsCount = 142,
            description = "Authentic hand-woven Maasai Shúkà from Arusha. Extremely durable, multi-functional fabric with a striking red and black check pattern. Perfect as a throw, outdoor blanket, or fashion accessory.",
            branch = "Arusha Curio Market"
        ),
        Product(
            id = "p2",
            name = "Mt. Kilimanjaro Coffee AA",
            price = 28000.0,
            category = "Groceries",
            iconName = "coffee",
            rating = 4.8f,
            reviewsCount = 310,
            description = "Single-origin, medium-roasted Arabica beans harvested from the fertile volcanic soils of Mount Kilimanjaro. Rich body with delightful notes of dark chocolate and blackcurrant.",
            branch = "Moshi Gate Depot"
        ),
        Product(
            id = "p3",
            name = "Serengeti Extreme Trail Boots",
            price = 145000.0,
            category = "Outdoors",
            iconName = "boots",
            rating = 4.7f,
            reviewsCount = 89,
            description = "Rugged, water-resistant hiking boots engineered for safari expeditions and rough trails. Features breathable mesh lining and dual-density slip-resistant vulcanized rubber outsoles.",
            branch = "Dar es Salaam Hub"
        ),
        Product(
            id = "p4",
            name = "Safari Smart Pro Phone",
            price = 560000.0,
            category = "Electronics",
            iconName = "phone",
            rating = 4.6f,
            reviewsCount = 67,
            description = "Highly capable smartphone optimized for remote areas. Features a massive 6000mAh battery for 3-day exploration use, advanced GPS satellite connection, and a crisp triple-lens camera.",
            branch = "Dar - Mlimani City"
        ),
        Product(
            id = "p5",
            name = "Zanzibar Hand-Printed Kanga",
            price = 18000.0,
            category = "Fashion",
            iconName = "dress",
            rating = 4.8f,
            reviewsCount = 115,
            description = "Beautifully dyed cotton Kanga wrap direct from Stone Town, Zanzibar. Adorned with highly decorative borders and a traditional Swahili proverb: 'Amani na Upendo' (Peace and Love).",
            branch = "Zanzibar Darajani"
        ),
        Product(
            id = "p6",
            name = "Ebony Handcarved Elephant",
            price = 85000.0,
            category = "Local Crafts",
            iconName = "art",
            rating = 4.9f,
            reviewsCount = 54,
            description = "Exquisite masterwork handcarved from pitch-black solid Tanzanian Ebony wood by Makonde artisans. Polished to a subtle satin sheen, showcasing stunning muscle definition and natural ivory-mimicking tusks.",
            branch = "Dar es Salaam Slipway"
        ),
        Product(
            id = "p7",
            name = "Pemba Island Clove Pack",
            price = 12000.0,
            category = "Groceries",
            iconName = "spice",
            rating = 4.9f,
            reviewsCount = 76,
            description = "Aromatic grade-A whole dried cloves hand-harvested on Pemba Island, Zanzibar Archipelago. Features intense spicy sweetness, ideal for culinary creations, spiced teas, or home fragrance.",
            branch = "Zanzibar Port Depot"
        ),
        Product(
            id = "p8",
            name = "Miombo Forest Wild Honey",
            price = 15000.0,
            category = "Groceries",
            iconName = "honey",
            rating = 4.7f,
            reviewsCount = 42,
            description = "Pure, unpasteurized, organic raw honey sourced from wild Miombo woodland beehives in Tabora. Dark amber color with a complex smoky undertone and robust, unrefined sweetness.",
            branch = "Tabora Junction"
        ),
        Product(
            id = "p9",
            name = "Tribal Beaded Leather Sandals",
            price = 32000.0,
            category = "Fashion",
            iconName = "sandals",
            rating = 4.8f,
            reviewsCount = 63,
            description = "Premium handcrafted leather sandals beautifully decorated with hand-sewn glass beadwork in traditional tribal patterns. Supple cushioned insoles provide comfortable all-day beach and market walking.",
            branch = "Arusha Curio Market"
        )
    )
}
