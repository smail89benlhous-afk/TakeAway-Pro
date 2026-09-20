package com.takeawaypro.app

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
    val id: Int = 0,
    val role: String = "restaurant",
    val businessType: BusinessType = BusinessType.CAFE,
    val businessName: String = "",
    val managerName: String = "",
    val phone: String = "",
    val email: String = "",
    val city: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
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
    val hidden: Boolean = false,
    // Content URI (as a string) of a real photo picked by the admin, if any.
    // Falls back to `emoji` in the UI when this is null. Local-only for now:
    // the backend doesn't yet accept photo uploads, so this never leaves the device.
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
data class OrderLine(
    val productId: String,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int
)

@Serializable
data class OrderAccountInfo(
    val businessName: String = "",
    val city: String = "",
    val phone: String = "",
    val address: String = ""
)

@Serializable
data class Order(
    val id: Int,
    val account: OrderAccountInfo,
    val lines: List<OrderLine>,
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
    val id: Int,
    val businessName: String,
    val businessType: BusinessType,
    val city: String,
    val phone: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val orderCount: Int
)

val sampleCategories = listOf("Gobelets", "Boîtes", "Sacs", "Couverts", "Pailles", "Serviettes", "Autres")

private val modelsJson = Json { ignoreUnknownKeys = true }

private fun parseErrorMessage(raw: String?): String {
    if (raw.isNullOrBlank()) return "Une erreur est survenue. Réessayez."
    return try {
        modelsJson.decodeFromString(ApiError.serializer(), raw).error
    } catch (e: Exception) {
        "Une erreur est survenue. Réessayez."
    }
}

private fun businessTypeFromApi(raw: String?): BusinessType =
    raw?.let { runCatching { BusinessType.valueOf(it) }.getOrNull() } ?: BusinessType.AUTRE

private fun ApiAccount.toAccount(): Account = Account(
    id = id,
    role = role,
    businessType = businessTypeFromApi(businessType),
    businessName = businessName ?: "",
    managerName = managerName ?: "",
    phone = phone,
    email = email ?: "",
    city = city ?: "",
    address = address ?: "",
    latitude = latitude,
    longitude = longitude
)

private fun ApiProduct.toProduct(): Product = Product(
    id = id,
    name = name,
    category = category,
    price = price,
    emoji = emoji,
    popular = popular,
    stock = stock,
    hidden = hidden
)

private fun ApiOrderItem.toOrderLine(): OrderLine = OrderLine(productId, productName, unitPrice, quantity)

private fun ApiOrder.toOrder(): Order = Order(
    id = id,
    account = OrderAccountInfo(
        businessName = account?.businessName ?: "",
        city = account?.city ?: "",
        phone = account?.phone ?: "",
        address = account?.address ?: ""
    ),
    lines = items.map { it.toOrderLine() },
    subtotal = subtotal,
    deliveryFee = deliveryFee,
    total = total,
    paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethod) }.getOrDefault(PaymentMethod.CASH_ON_DELIVERY),
    status = runCatching { OrderStatus.valueOf(status) }.getOrDefault(OrderStatus.RECEIVED)
)

private fun ApiRestaurant.toSummary(): RestaurantSummary = RestaurantSummary(
    id = id,
    businessName = businessName,
    businessType = businessTypeFromApi(businessType),
    city = city ?: "",
    phone = phone,
    address = address ?: "",
    latitude = latitude,
    longitude = longitude,
    orderCount = orderCount
)

private fun ApiNotification.toNotification(): Notification = Notification(
    id = id, orderId = orderId, title = title, message = message, read = read
)

/**
 * What gets persisted locally on the device: only the session (JWT + account),
 * so a restaurant/café or the admin doesn't have to log in again every time the
 * app is closed. Everything else (catalog, orders, notifications, restaurants)
 * now lives on the real backend and is fetched fresh over the network.
 */
@Serializable
data class SessionSnapshot(
    val token: String? = null,
    val account: Account? = null
)

/**
 * Shared app state, held once at the NavHost root and passed down to screens.
 * Talks to the real backend (see ApiClient/ApiService) for the catalog, orders,
 * restaurants and notifications — data now syncs across every device. Only the
 * in-progress cart and the current session stay local to this app instance.
 */
class AppData(private val store: LocalStore? = null) {
    private val initialSession = store?.load()

    var account = mutableStateOf(initialSession?.account)
    val cart = mutableStateListOf<CartLine>()
    val orders = mutableStateListOf<Order>()
    val notifications = mutableStateListOf<Notification>()
    val adminProducts = mutableStateListOf<Product>()
    val restaurants = mutableStateListOf<RestaurantSummary>()
    var adminStats = mutableStateOf<AdminStats?>(null)

    val deliveryFee = 20.0

    init {
        ApiClient.authToken = initialSession?.token
    }

    val isLoggedIn: Boolean get() = account.value != null

    private fun persistSession() {
        store?.save(SessionSnapshot(token = ApiClient.authToken, account = account.value))
    }

    fun logout() {
        ApiClient.authToken = null
        account.value = null
        cart.clear()
        orders.clear()
        notifications.clear()
        adminProducts.clear()
        restaurants.clear()
        adminStats.value = null
        store?.clear()
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Erreur réseau. Vérifiez votre connexion."))
    }

    suspend fun register(
        businessType: BusinessType,
        businessName: String,
        managerName: String,
        phone: String,
        email: String,
        password: String,
        city: String,
        address: String,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Unit> = safeCall {
        val response = ApiClient.service.register(
            RegisterRequest(
                businessType = businessType.name,
                businessName = businessName,
                managerName = managerName,
                phone = phone,
                email = email.ifBlank { null },
                password = password,
                city = city,
                address = address,
                latitude = latitude,
                longitude = longitude
            )
        )
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        ApiClient.authToken = body.token
        account.value = body.account.toAccount()
        persistSession()
    }

    suspend fun login(phone: String, password: String): Result<Account> = safeCall {
        val response = ApiClient.service.login(LoginRequest(phone, password))
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        ApiClient.authToken = body.token
        val acc = body.account.toAccount()
        account.value = acc
        persistSession()
        acc
    }

    suspend fun loadProducts(includeHidden: Boolean = false): Result<Unit> = safeCall {
        val response = ApiClient.service.getProducts(if (includeHidden) 1 else null)
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        adminProducts.clear()
        adminProducts.addAll(body.products.map { it.toProduct() })
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

    suspend fun placeOrder(paymentMethod: PaymentMethod): Result<Order> = safeCall {
        val items = cart.map { OrderItemDto(it.product.id, it.quantity) }
        val response = ApiClient.service.createOrder(CreateOrderRequest(items, paymentMethod.name))
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        val order = body.order.toOrder()
        orders.add(0, order)
        cart.clear()
        order
    }

    suspend fun loadMyOrders(): Result<Unit> = safeCall {
        val response = ApiClient.service.getMyOrders()
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        orders.clear()
        orders.addAll(body.orders.map { it.toOrder() })
    }

    suspend fun fetchOrder(orderId: Int): Result<Order> = safeCall {
        val response = ApiClient.service.getOrder(orderId)
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        val order = body.order.toOrder()
        val idx = orders.indexOfFirst { it.id == orderId }
        if (idx >= 0) orders[idx] = order else orders.add(order)
        order
    }

    fun orderById(orderId: Int): Order? = orders.firstOrNull { it.id == orderId }

    suspend fun loadAllOrdersAdmin(): Result<Unit> = safeCall {
        val response = ApiClient.service.getAllOrders()
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        orders.clear()
        orders.addAll(body.orders.map { it.toOrder() })
    }

    suspend fun updateOrderStatus(orderId: Int, status: OrderStatus): Result<Unit> = safeCall {
        val response = ApiClient.service.updateOrderStatus(orderId, StatusRequest(status.name))
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        val order = body.order.toOrder()
        val idx = orders.indexOfFirst { it.id == orderId }
        if (idx >= 0) orders[idx] = order else orders.add(order)
    }

    suspend fun loadRestaurants(): Result<Unit> = safeCall {
        val response = ApiClient.service.getRestaurants()
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        restaurants.clear()
        restaurants.addAll(body.restaurants.map { it.toSummary() })
    }

    suspend fun loadRestaurantDetail(id: Int): Result<RestaurantDetailResponse> = safeCall {
        val response = ApiClient.service.getRestaurant(id)
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        response.body() ?: throw Exception("Réponse invalide du serveur.")
    }

    suspend fun loadNotifications(): Result<Unit> = safeCall {
        val response = ApiClient.service.getNotifications()
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        notifications.clear()
        notifications.addAll(body.notifications.map { it.toNotification() })
    }

    suspend fun markNotificationRead(notificationId: Int): Result<Unit> = safeCall {
        val response = ApiClient.service.markNotificationRead(notificationId)
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val idx = notifications.indexOfFirst { it.id == notificationId }
        if (idx >= 0) notifications[idx] = notifications[idx].copy(read = true)
    }

    suspend fun loadAdminStats(): Result<Unit> = safeCall {
        val response = ApiClient.service.getAdminStats()
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        adminStats.value = response.body() ?: throw Exception("Réponse invalide du serveur.")
    }

    suspend fun addProduct(
        name: String, category: String, price: Double, emoji: String, description: String?, stock: Int
    ): Result<Product> = safeCall {
        val response = ApiClient.service.createProduct(ProductRequest(name, category, price, emoji, description, stock))
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        val product = body.product.toProduct()
        adminProducts.add(product)
        product
    }

    suspend fun updateProduct(
        id: String, name: String, category: String, price: Double, emoji: String, description: String?, stock: Int
    ): Result<Product> = safeCall {
        val response = ApiClient.service.updateProduct(id, ProductRequest(name, category, price, emoji, description, stock))
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Réponse invalide du serveur.")
        val product = body.product.toProduct()
        val idx = adminProducts.indexOfFirst { it.id == id }
        if (idx >= 0) adminProducts[idx] = product else adminProducts.add(product)
        product
    }

    suspend fun setProductVisibility(id: String, hidden: Boolean): Result<Unit> = safeCall {
        val response = ApiClient.service.setProductVisibility(id, VisibilityRequest(hidden))
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        val idx = adminProducts.indexOfFirst { it.id == id }
        if (idx >= 0) adminProducts[idx] = adminProducts[idx].copy(hidden = hidden)
    }

    suspend fun deleteProduct(id: String): Result<Unit> = safeCall {
        val response = ApiClient.service.deleteProduct(id)
        if (!response.isSuccessful) throw Exception(parseErrorMessage(response.errorBody()?.string()))
        adminProducts.removeAll { it.id == id }
    }
}
