package com.takeawaypro.app

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable

@Serializable
enum class BusinessType(val label: String, val emoji: String) {
    RESTAURANT("Restaurant", "🍽️"),
    CAFE("Café", "☕"),
    SNACK("Snack", "🥡"),
    PATISSERIE("Pâtisserie", "🧁"),
    AUTRE("Autre", "🏬")
}

@Serializable
data class Account(
    var businessType: BusinessType = BusinessType.CAFE,
    var businessName: String = "",
    var managerName: String = "",
    var phone: String = "",
    var email: String = "",
    var password: String = "",
    var city: String = "",
    var address: String = ""
)

@Serializable
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val emoji: String,
    val popular: Boolean = false,
    val stock: Int = 0,
    // Content URI (as a string) of a real photo picked by the admin, if any.
    // Falls back to `emoji` in the UI when this is null.
    val imageUri: String? = null
)

@Serializable
data class CartLine(
    val product: Product,
    val quantity: Int
)

@Serializable
enum class PaymentMethod(val label: String) {
    CASH_ON_DELIVERY("Paiement à la livraison"),
    ELECTRONIC("Paiement électronique (bientôt disponible)")
}

@Serializable
enum class OrderStatus(val label: String) {
    RECEIVED("Commande reçue"),
    CONFIRMED("Commande confirmée"),
    PREPARING("En préparation"),
    DELIVERING("En livraison"),
    DELIVERED("Livrée")
}

@Serializable
data class Order(
    val id: Int,
    val account: Account,
    val lines: List<CartLine>,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val status: OrderStatus = OrderStatus.RECEIVED
)

@Serializable
data class Notification(
    val id: Int,
    val orderId: Int,
    val title: String,
    val message: String,
    val read: Boolean = false
)

@Serializable
data class RestaurantSummary(
    val businessName: String,
    val businessType: BusinessType,
    val city: String,
    val phone: String,
    val address: String,
    val baselineOrderCount: Int
)

val sampleCategories = listOf("Gobelets", "Boîtes", "Sacs", "Couverts", "Pailles", "Serviettes", "Autres")

val sampleProducts = listOf(
    Product("p1", "Gobelet plastique 30cl", "Gobelets", 0.50, "🥤", popular = true, stock = 5000),
    Product("p2", "Gobelet café 25cl", "Gobelets", 0.45, "☕", popular = true, stock = 4000),
    Product("p3", "Couvercle pour gobelet", "Gobelets", 0.25, "🔘", stock = 6000),
    Product("p4", "Boîte repas Take Away", "Boîtes", 1.20, "📦", popular = true, stock = 2000),
    Product("p5", "Boîte burger", "Boîtes", 0.90, "🍔", stock = 1500),
    Product("p6", "Sac papier kraft", "Sacs", 0.80, "🛍️", stock = 1000),
    Product("p7", "Sac plastique", "Sacs", 0.30, "👜", stock = 2000),
    Product("p8", "Pailles", "Pailles", 0.10, "🥢", stock = 8000),
    Product("p9", "Cuillère plastique", "Couverts", 0.15, "🥄", stock = 3000),
    Product("p10", "Fourchette plastique", "Couverts", 0.15, "🍴", stock = 3000),
    Product("p11", "Serviette", "Serviettes", 0.08, "🧻", stock = 10000),
    Product("p12", "Film alimentaire", "Autres", 15.0, "🎞️", stock = 200)
)

val sampleRestaurants = listOf(
    RestaurantSummary("Café Atlas", BusinessType.CAFE, "Marrakech", "0600000001", "Rue Mohammed V, Marrakech", 24),
    RestaurantSummary("Snack Al Madina", BusinessType.SNACK, "Casablanca", "0600000002", "Bd Zerktouni, Casablanca", 41),
    RestaurantSummary("Pâtisserie Douceur", BusinessType.PATISSERIE, "Rabat", "0600000003", "Avenue Hassan II, Rabat", 12),
    RestaurantSummary("Restaurant Chez Amine", BusinessType.RESTAURANT, "Fès", "0600000004", "Route de Sefrou, Fès", 10)
)

/**
 * Full snapshot of everything that gets persisted locally on the device
 * (see LocalStore). Anything not listed here (like the in-progress cart)
 * is intentionally session-only in v1.
 */
@Serializable
data class AppSnapshot(
    val account: Account? = null,
    val products: List<Product> = sampleProducts,
    val orders: List<Order> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val nextOrderId: Int = 1025,
    val nextNotificationId: Int = 1,
    val nextProductIdCounter: Int = sampleProducts.size + 1
)

/**
 * Shared app state, held once at the NavHost root and passed down to screens.
 *
 * v1 persistence model: the whole snapshot is serialized to JSON and saved in
 * SharedPreferences on this one device (see LocalStore). There is still no
 * real server, so nothing syncs between devices yet — that needs an actual
 * backend (accounts, products, orders in a real database reachable from
 * every phone), which is the next big step beyond this prototype.
 */
class AppData(private val store: LocalStore? = null) {
    private val initialSnapshot = store?.load() ?: AppSnapshot()

    var account = mutableStateOf(initialSnapshot.account)
    val cart = mutableStateListOf<CartLine>()
    val orders = mutableStateListOf(*initialSnapshot.orders.toTypedArray())
    val notifications = mutableStateListOf(*initialSnapshot.notifications.toTypedArray())
    val adminProducts = mutableStateListOf(*initialSnapshot.products.toTypedArray())
    val restaurants = mutableStateListOf(*sampleRestaurants.toTypedArray())
    private var nextOrderId = initialSnapshot.nextOrderId
    private var nextNotificationId = initialSnapshot.nextNotificationId
    private var nextProductIdCounter = initialSnapshot.nextProductIdCounter

    val deliveryFee = 20.0

    // Baseline figures so the dashboard isn't empty before real orders come in (mock data, v1).
    private val baselineOrders = 24
    private val baselineClients = 87
    private val baselineRevenue = 3400.0

    val todayOrderCount: Int get() = baselineOrders + orders.size
    val todayClientCount: Int get() = baselineClients
    val todayProductCount: Int get() = adminProducts.size
    val todayRevenue: Double get() = baselineRevenue + orders.sumOf { it.total }

    private fun persist() {
        store?.save(
            AppSnapshot(
                account = account.value,
                products = adminProducts.toList(),
                orders = orders.toList(),
                notifications = notifications.toList(),
                nextOrderId = nextOrderId,
                nextNotificationId = nextNotificationId,
                nextProductIdCounter = nextProductIdCounter
            )
        )
    }

    fun nextProductId(): String = "p${nextProductIdCounter++}"

    fun registerAccount(newAccount: Account) {
        account.value = newAccount
        val index = restaurants.indexOfFirst { it.businessName == newAccount.businessName }
        val summary = RestaurantSummary(
            businessName = newAccount.businessName,
            businessType = newAccount.businessType,
            city = newAccount.city,
            phone = newAccount.phone,
            address = newAccount.address,
            baselineOrderCount = 0
        )
        if (index >= 0) restaurants[index] = summary else restaurants.add(summary)
        persist()
    }

    fun setAccount(newAccount: Account) {
        account.value = newAccount
        persist()
    }

    fun addToCart(product: Product, quantity: Int) {
        val index = cart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val newQty = cart[index].quantity + quantity
            if (newQty <= 0) cart.removeAt(index)
            else cart[index] = cart[index].copy(quantity = newQty)
        } else if (quantity > 0) {
            cart.add(CartLine(product, quantity))
        }
    }

    fun setQuantity(product: Product, quantity: Int) {
        val index = cart.indexOfFirst { it.product.id == product.id }
        if (quantity <= 0) {
            if (index >= 0) cart.removeAt(index)
        } else if (index >= 0) {
            cart[index] = cart[index].copy(quantity = quantity)
        } else {
            cart.add(CartLine(product, quantity))
        }
    }

    fun quantityOf(product: Product): Int =
        cart.firstOrNull { it.product.id == product.id }?.quantity ?: 0

    fun subtotal(): Double = cart.sumOf { it.product.price * it.quantity }

    fun placeOrder(paymentMethod: PaymentMethod): Order {
        val sub = subtotal()
        val order = Order(
            id = nextOrderId++,
            account = account.value ?: Account(),
            lines = cart.toList(),
            subtotal = sub,
            deliveryFee = deliveryFee,
            total = sub + deliveryFee,
            paymentMethod = paymentMethod
        )
        orders.add(0, order)
        val totalItems = order.lines.sumOf { it.quantity }
        val businessName = order.account.businessName.ifBlank { "Un client" }
        notifications.add(
            0,
            Notification(
                id = nextNotificationId++,
                orderId = order.id,
                title = "Nouvelle commande #${order.id}",
                message = "$businessName vient de passer une commande de $totalItems produits."
            )
        )
        cart.clear()
        persist()
        return order
    }

    fun orderById(orderId: Int): Order? = orders.firstOrNull { it.id == orderId }

    fun updateOrderStatus(orderId: Int, status: OrderStatus) {
        val index = orders.indexOfFirst { it.id == orderId }
        if (index >= 0) {
            orders[index] = orders[index].copy(status = status)
            persist()
        }
    }

    fun markNotificationRead(notificationId: Int) {
        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index >= 0) {
            notifications[index] = notifications[index].copy(read = true)
            persist()
        }
    }

    fun addProduct(product: Product) {
        adminProducts.add(product)
        persist()
    }

    fun updateProduct(product: Product) {
        val index = adminProducts.indexOfFirst { it.id == product.id }
        if (index >= 0) {
            adminProducts[index] = product
            persist()
        }
    }

    fun deleteProduct(productId: String) {
        adminProducts.removeAll { it.id == productId }
        persist()
    }
}
