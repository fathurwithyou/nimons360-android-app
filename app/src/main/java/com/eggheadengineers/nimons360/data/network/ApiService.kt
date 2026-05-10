package com.eggheadengineers.nimons360.data.network

import com.eggheadengineers.nimons360.data.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginApiResponse>

    @GET("api/me")
    suspend fun getProfile(): Response<ProfileApiResponse>

    @PATCH("api/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): Response<ProfileApiResponse>

    @Multipart
    @POST("api/me/photo")
    suspend fun uploadProfilePhoto(@Part photo: MultipartBody.Part): Response<ProfileApiResponse>

    @GET("api/me/families")
    suspend fun getMyFamilies(): Response<FamilyListApiResponse>

    @GET("api/families/discover")
    suspend fun getDiscoverFamilies(): Response<FamilyListApiResponse>

    @GET("api/families")
    suspend fun getAllFamilies(): Response<FamilyListApiResponse>

    @GET("api/families/{familyId}")
    suspend fun getFamilyDetail(@Path("familyId") id: String): Response<FamilyDetailApiResponse>

    @POST("api/families")
    suspend fun createFamily(@Body request: CreateFamilyRequestDto): Response<FamilyDetailApiResponse>

    @POST("api/families/join")
    suspend fun joinFamily(@Body request: JoinFamilyRequestDto): Response<SimpleApiResponse>

    @POST("api/families/leave")
    suspend fun leaveFamily(@Body request: LeaveFamilyRequestDto): Response<SimpleApiResponse>

    @POST("api/notifications/subscribe")
    suspend fun subscribeDeviceToken(
        @Body request: SubscribeDeviceTokenRequestDto,
    ): Response<SubscribeDeviceTokenApiResponse>

    @POST("api/notifications/unsubscribe")
    suspend fun unsubscribeDeviceToken(): Response<UnsubscribeDeviceTokenApiResponse>

    @POST("api/notifications/send")
    suspend fun sendFamilyNotification(
        @Body request: SendFamilyNotificationRequestDto,
    ): Response<SendFamilyNotificationApiResponse>

    @POST("api/notifications/greeting")
    suspend fun sendGreetingNotification(
        @Body request: SendGreetingNotificationRequestDto,
    ): Response<SendGreetingNotificationApiResponse>
}
