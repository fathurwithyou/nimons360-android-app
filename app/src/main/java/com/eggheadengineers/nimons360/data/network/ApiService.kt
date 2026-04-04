package com.eggheadengineers.nimons360.data.network

import com.eggheadengineers.nimons360.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginApiResponse>

    @GET("api/me")
    suspend fun getProfile(): Response<ProfileApiResponse>

    @PATCH("api/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): Response<ProfileApiResponse>

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
    suspend fun joinFamily(@Body request: JoinFamilyRequestDto): Response<FamilyDetailApiResponse>

    @POST("api/families/leave")
    suspend fun leaveFamily(@Body request: LeaveFamilyRequestDto): Response<FamilyDetailApiResponse>
}
