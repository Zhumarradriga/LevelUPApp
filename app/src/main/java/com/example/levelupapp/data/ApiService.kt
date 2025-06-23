package com.example.levelupapp.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/register/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/resend-confirmation/")
    suspend fun resendConfirmation(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/request-password-reset/")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): Response<GenericResponse>

    @POST("api/auth/confirm-password-reset/")
    suspend fun confirmPasswordReset(
        @Body request: ResetPasswordRequest
    ): Response<GenericResponse>

    @GET("api/auth/confirm-email/{token}/")
    suspend fun confirmEmail(@Path("token") token: String): Response<GenericResponse>

    @POST("api/auth/token/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("api/auth/user/")
    suspend fun getUser(): Response<UserResponse>

    @GET("api/tasks/tasks/")
    suspend fun getTasks(): Response<List<Task>>

    @POST("api/tasks/tasks/")
    suspend fun createTask(@Body request: TaskRequest): Response<Task>

    @GET("api/tasks/tasks/{id}/")
    suspend fun getTaskById(@Path("id") id: Int): Response<Task>

    @PATCH("api/tasks/tasks/{id}/")
    suspend fun updateTask(@Path("id") id: Int, @Body request: TaskRequest): Response<Task>

    @DELETE("api/tasks/tasks/{id}/")
    suspend fun deleteTask(@Path("id") id: Int): Response<GenericResponse>

    @GET("api/tasks/categories/")
    suspend fun getCategories(): Response<List<Category>>

    @POST("api/tasks/categories/")
    suspend fun createCategory(@Body request: CategoryRequest): Response<Category>

    @PUT("api/tasks/categories/{id}/")
    suspend fun updateCategory(@Path("id") id: Int, @Body request: CategoryRequest): Response<Category>

    @DELETE("api/tasks/categories/{id}/")
    suspend fun deleteCategory(@Path("id") id: Int): Response<GenericResponse>

    @GET("api/stats/stats/")
    suspend fun getStats(): Response<List<Stat>>

    @POST("api/stats/stats/")
    suspend fun createStat(@Body request: StatRequest): Response<Stat>

    @GET("api/stats/stats/{id}/")
    suspend fun getStatById(@Path("id") id: Int): Response<Stat>

    @PUT("api/stats/stats/{id}/")
    suspend fun updateStat(@Path("id") id: Int, @Body request: StatRequest): Response<Stat>

    @DELETE("api/stats/stats/{id}/")
    suspend fun deleteStat(@Path("id") id: Int): Response<GenericResponse>

    @GET("api/stats/avatar/")
    suspend fun getAvatar(): Response<List<AvatarResponse>>
}