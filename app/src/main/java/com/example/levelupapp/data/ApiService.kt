package com.example.levelupapp.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
}