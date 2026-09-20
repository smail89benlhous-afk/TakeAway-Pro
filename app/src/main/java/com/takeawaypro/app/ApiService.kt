package com.takeawaypro.app

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("api/products")
    suspend fun getProducts(@Query("all") all: Int? = null): Response<ProductsResponse>

    @POST("api/products")
    suspend fun createProduct(@Body body: ProductRequest): Response<ProductResponse>

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body body: ProductRequest): Response<ProductResponse>

    @PATCH("api/products/{id}/visibility")
    suspend fun setProductVisibility(@Path("id") id: String, @Body body: VisibilityRequest): Response<OkResponse>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<OkResponse>

    @POST("api/orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): Response<OrderResponse>

    @GET("api/orders/mine")
    suspend fun getMyOrders(): Response<OrdersResponse>

    @GET("api/orders/{id}")
    suspend fun getOrder(@Path("id") id: Int): Response<OrderResponse>

    @GET("api/orders")
    suspend fun getAllOrders(): Response<OrdersResponse>

    @PATCH("api/orders/{id}/status")
    suspend fun updateOrderStatus(@Path("id") id: Int, @Body body: StatusRequest): Response<OrderResponse>

    @GET("api/restaurants")
    suspend fun getRestaurants(): Response<RestaurantsResponse>

    @GET("api/restaurants/{id}")
    suspend fun getRestaurant(@Path("id") id: Int): Response<RestaurantDetailResponse>

    @GET("api/notifications")
    suspend fun getNotifications(): Response<NotificationsResponse>

    @PATCH("api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Int): Response<OkResponse>

    @GET("api/admin/stats")
    suspend fun getAdminStats(): Response<AdminStats>
}
