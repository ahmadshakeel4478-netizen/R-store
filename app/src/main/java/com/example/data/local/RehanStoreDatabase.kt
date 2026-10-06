package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.WishlistDao
import com.example.data.local.entity.CartEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.WishlistEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CartEntity::class,
        WishlistEntity::class,
        OrderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RehanStoreDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: RehanStoreDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): RehanStoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RehanStoreDatabase::class.java,
                    "rehan_store_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialProducts(database.productDao())
                    }
                }
            }
        }

        suspend fun populateInitialProducts(productDao: ProductDao) {
            val initial = listOf(
                ProductEntity(
                    id = "PROD_MOB_01",
                    title = "Rehan Pro Max 5G (8GB RAM, 128GB)",
                    description = "Ultra fast 5G flagship phone with 120Hz Super AMOLED display, 108MP Sony camera sensor, 5000mAh all-day battery with 67W Turbo Flash Charge.",
                    category = "Mobiles",
                    price = 14999.0,
                    originalPrice = 21999.0,
                    rating = 4.8,
                    reviewCount = 1420,
                    imageUrl = "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600&auto=format&fit=crop&q=80",
                    badge = "Rehan Assured",
                    sellerName = "Rehan Official Store",
                    inStock = true,
                    stockCount = 45,
                    isSellerItem = true,
                    brand = "RehanTech",
                    specs = "8GB RAM | 128GB ROM | 108MP + 16MP Camera | 5000 mAh Battery | 5G Ready"
                ),
                ProductEntity(
                    id = "PROD_EAR_02",
                    title = "Rehan BassPods Wireless Earbuds ANC",
                    description = "Active Noise Cancellation up to 32dB, 40 hours total playtime, dual mic AI clear voice call clarity, instant fast pair.",
                    category = "Electronics",
                    price = 1499.0,
                    originalPrice = 3999.0,
                    rating = 4.7,
                    reviewCount = 890,
                    imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600&auto=format&fit=crop&q=80",
                    badge = "Deal of the Day",
                    sellerName = "Rehan Official Store",
                    inStock = true,
                    stockCount = 120,
                    isSellerItem = true,
                    brand = "Rehan Audio",
                    specs = "Active Noise Cancellation | 40H Playtime | Bluetooth 5.3 | IPX5 Sweatproof"
                ),
                ProductEntity(
                    id = "PROD_WAT_03",
                    title = "Apex AMOLED Smartwatch with BT Calling",
                    description = "1.96-inch HD AMOLED Always-On Display, Bluetooth calling, 120+ sports modes, 24/7 heart rate & SpO2 health tracking.",
                    category = "Electronics",
                    price = 1999.0,
                    originalPrice = 5499.0,
                    rating = 4.6,
                    reviewCount = 632,
                    imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80",
                    badge = "Trending",
                    sellerName = "Rehan Official Store",
                    inStock = true,
                    stockCount = 80,
                    isSellerItem = true,
                    brand = "Apex Pro",
                    specs = "1.96\" AMOLED Display | BT Calling | 7 Days Battery | IP68 Water Resistant"
                ),
                ProductEntity(
                    id = "PROD_FAS_04",
                    title = "Men's Premium Slim Fit Denim Jacket",
                    description = "Handcrafted premium washed denim jacket with reinforced seams, stylish brass buttons, and comfortable stretch denim fabric.",
                    category = "Fashion",
                    price = 1199.0,
                    originalPrice = 2999.0,
                    rating = 4.5,
                    reviewCount = 412,
                    imageUrl = "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=600&auto=format&fit=crop&q=80",
                    badge = "Special Offer",
                    sellerName = "Rehan Fashion Hub",
                    inStock = true,
                    stockCount = 35,
                    isSellerItem = true,
                    brand = "Urban Rehan",
                    specs = "100% Cotton Denim | Machine Wash | Button Closure | All Season Wear"
                ),
                ProductEntity(
                    id = "PROD_FAS_05",
                    title = "Women's Embroidered Anarkali Kurta Set",
                    description = "Gorgeous festive designer Anarkali kurta with pant and chiffon dupatta set featuring gold zari embroidery work.",
                    category = "Fashion",
                    price = 1399.0,
                    originalPrice = 3499.0,
                    rating = 4.9,
                    reviewCount = 760,
                    imageUrl = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&auto=format&fit=crop&q=80",
                    badge = "Top Rated",
                    sellerName = "Rehan Ethnic Collection",
                    inStock = true,
                    stockCount = 50,
                    isSellerItem = true,
                    brand = "Rehan Heritage",
                    specs = "Chanderi Silk Fabric | Pure Zari Embroidery | Set of 3 (Kurta, Pant, Dupatta)"
                ),
                ProductEntity(
                    id = "PROD_SHOE_06",
                    title = "Men's Ultra Air Cushion Running Shoes",
                    description = "Breathable mesh lightweight running sneakers with responsive air-cushioned bounce sole for daily walking & athletic workouts.",
                    category = "Footwear",
                    price = 999.0,
                    originalPrice = 2499.0,
                    rating = 4.4,
                    reviewCount = 523,
                    imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80",
                    badge = "Under ₹999",
                    sellerName = "Rehan Footwear Co.",
                    inStock = true,
                    stockCount = 60,
                    isSellerItem = true,
                    brand = "StrideMax",
                    specs = "Air Cushion Sole | Breathable Mesh Upper | Non-slip Grip | Lightweight 280g"
                ),
                ProductEntity(
                    id = "PROD_KIT_07",
                    title = "Prestige 3-Piece Non-Stick Cookware Set",
                    description = "Durable hard anodized non-stick frying pan, omni tawa, and kadai with tempered glass lid. Induction & gas stove compatible.",
                    category = "Home & Kitchen",
                    price = 1799.0,
                    originalPrice = 3899.0,
                    rating = 4.6,
                    reviewCount = 380,
                    imageUrl = "https://images.unsplash.com/photo-1584992236310-6edddc08acff?w=600&auto=format&fit=crop&q=80",
                    badge = "Kitchen Must-Have",
                    sellerName = "Rehan Home Store",
                    inStock = true,
                    stockCount = 28,
                    isSellerItem = true,
                    brand = "ChefMaster",
                    specs = "5-Layer Granite Coating | Induction Base | PFOA Free | Dishwasher Safe"
                ),
                ProductEntity(
                    id = "PROD_GROC_08",
                    title = "Royal Premium California Almonds (1kg Pack)",
                    description = "100% natural, whole crunchy California Badam. Vacuum packed to preserve fresh nutrients, proteins, and healthy dietary fats.",
                    category = "Grocery",
                    price = 699.0,
                    originalPrice = 1200.0,
                    rating = 4.8,
                    reviewCount = 940,
                    imageUrl = "https://images.unsplash.com/photo-1508061253366-f7da158b6d46?w=600&auto=format&fit=crop&q=80",
                    badge = "Super Saver",
                    sellerName = "Rehan Organic Farm",
                    inStock = true,
                    stockCount = 150,
                    isSellerItem = true,
                    brand = "PureRehan",
                    specs = "100% Whole California Almonds | Net Weight: 1000g | Zero Preservatives"
                )
            )
            productDao.insertProducts(initial)
        }
    }
}
