package com.takeawaypro.app

import kotlinx.serialization.Serializable

@Serializable
data class ApiAccount(
    val id: Int,
    val role: String,
    val businessType: String? = null,
    val businessName: String? = null,
    val managerName: String? = null,
    val phone: String,
    val email: String? = null,
    val city: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class AuthResponse(val token: String, val account: ApiAccount)

@Serializable
data class RegisterRequest(
    val businessType: String,
    val businessName: String,
    val managerName: String,
    val phone: String,
    val email: String? = null,
    val password: String,
    val city: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class LoginRequest(val phone: String, val password: String)

@Serializable
data class ApiProduct(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val emoji: String = "📦",
    val photoUrl: String? = null,
    val description: String? = null,
    val popular: Boolean = false,
    val stock: Int = 0,
    val hidden: Boolean = false
)

@Serializable
data class ProductsResponse(val products: List<ApiProduct>)

@Serializable
data class ProductResponse(val product: ApiProduct)

@Serializable
data class ProductRequest(
    val name: String,
    val category: String,
    val price: Double,
    val emoji: String,
    val description: String? = null,
    val stock: Int
)

@Serializable
data class VisibilityRequest(val hidden: Boolean)

@Serializable
data class OrderItemDto(val productId: String, val quantity: Int)

@Serializable
data class CreateOrderRequest(val items: List<OrderItemDto>, val paymentMethod: String)

@Serializable
data class ApiOrderItem(
    val productId: String,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int
)

@Serializable
data class ApiOrderAccount(
    val businessName: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val address: String? = null
)

@Serializable
data class ApiOrder(
    val id: Int,
    val status: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val paymentMethod: String,
    val createdAt: String,
    val account: ApiOrderAccount? = null,
    val items: List<ApiOrderItem> = emptyList()
)

@Serializable
data class OrderResponse(val order: ApiOrder)

@Serializable
data class OrdersResponse(val orders: List<ApiOrder>)

@Serializable
data class StatusRequest(val status: String)

@Serializable
data class ApiRestaurant(
    val id: Int,
    val businessType: String? = null,
    val businessName: String,
    val managerName: String? = null,
    val phone: String,
    val email: String? = null,
    val city: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val orderCount: Int = 0
)

@Serializable
data class RestaurantsResponse(val restaurants: List<ApiRestaurant>)

@Serializable
data class ApiRestaurantOrderSummary(val id: Int, val status: String, val total: Double, val createdAt: String)

@Serializable
data class RestaurantDetailResponse(
    val restaurant: ApiRestaurant,
    val orders: List<ApiRestaurantOrderSummary>,
    val totalPurchases: Double
)

@Serializable
data class ApiNotification(
    val id: Int,
    val orderId: Int,
    val title: String,
    val message: String,
    val read: Boolean,
    val createdAt: String
)

@Serializable
data class NotificationsResponse(val notifications: List<ApiNotification>)

@Serializable
data class AdminStats(
    val orderCount: Int,
    val clientCount: Int,
    val productCount: Int,
    val revenue: Double
)

@Serializable
data class OkResponse(val ok: Boolean = true)

@Serializable
data class ApiError(val error: String)
