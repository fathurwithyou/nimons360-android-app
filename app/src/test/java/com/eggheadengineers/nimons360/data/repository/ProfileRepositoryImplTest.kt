package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.data.dto.CreateFamilyRequestDto
import com.eggheadengineers.nimons360.data.dto.FamilyDetailApiResponse
import com.eggheadengineers.nimons360.data.dto.FamilyListApiResponse
import com.eggheadengineers.nimons360.data.dto.JoinFamilyRequestDto
import com.eggheadengineers.nimons360.data.dto.LeaveFamilyRequestDto
import com.eggheadengineers.nimons360.data.dto.LoginApiResponse
import com.eggheadengineers.nimons360.data.dto.LoginRequestDto
import com.eggheadengineers.nimons360.data.dto.ProfileApiResponse
import com.eggheadengineers.nimons360.data.dto.ProfileDto
import com.eggheadengineers.nimons360.data.dto.UpdateProfileRequestDto
import com.eggheadengineers.nimons360.data.network.ApiService
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class ProfileRepositoryImplTest {

    private val fakeApi = FakeApiService()
    private val repo = ProfileRepositoryImpl(fakeApi)

    // ── getProfile ──

    @Test
    fun `getProfile success maps to correct domain model`() = runTest {
        fakeApi.getProfileResponse = Response.success(
            ProfileApiResponse(
                data = ProfileDto(
                    id = 42, nim = "13521099",
                    email = "alice@stei.itb.ac.id", fullName = "Alice",
                    createdAt = "2025-01-01T00:00:00Z", updatedAt = null,
                )
            )
        )

        val result = repo.getProfile()

        assertTrue(result.isSuccess)
        val profile = result.getOrThrow()
        assertEquals("42", profile.id)
        assertEquals("Alice", profile.name)
        assertEquals("alice@stei.itb.ac.id", profile.email)
    }

    @Test
    fun `getProfile with null data fails with descriptive message`() = runTest {
        fakeApi.getProfileResponse = Response.success(ProfileApiResponse(data = null))

        val result = repo.getProfile()

        assertTrue(result.isFailure)
        assertEquals("Empty profile response", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile 401 fails with server error message`() = runTest {
        fakeApi.getProfileResponse = Response.error(
            401,
            """{"error":{"code":"UNAUTHORIZED","message":"Token expired"}}""".toResponseBody(JSON),
        )

        val result = repo.getProfile()

        assertTrue(result.isFailure)
        assertEquals("Token expired", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfile 500 with blank body fails with default message`() = runTest {
        fakeApi.getProfileResponse = Response.error(500, "".toResponseBody(JSON))

        val result = repo.getProfile()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.startsWith("Failed to get profile") == true)
    }

    // ── updateName ──

    @Test
    fun `updateName success returns updated profile`() = runTest {
        fakeApi.updateProfileResponse = Response.success(
            ProfileApiResponse(
                data = ProfileDto(
                    id = 42, nim = null,
                    email = "alice@stei.itb.ac.id", fullName = "Alice Updated",
                    createdAt = null, updatedAt = "2026-04-05T00:00:00Z",
                )
            )
        )

        val result = repo.updateName("Alice Updated")

        assertTrue(result.isSuccess)
        assertEquals("Alice Updated", result.getOrThrow().name)
    }

    @Test
    fun `updateName with null data fails with descriptive message`() = runTest {
        fakeApi.updateProfileResponse = Response.success(ProfileApiResponse(data = null))

        val result = repo.updateName("Alice")

        assertTrue(result.isFailure)
        assertEquals("Empty update response", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateName 422 fails with server validation message`() = runTest {
        fakeApi.updateProfileResponse = Response.error(
            422,
            """{"error":{"code":"VALIDATION","message":"Name must be at least 2 characters"}}"""
                .toResponseBody(JSON),
        )

        val result = repo.updateName("A")

        assertTrue(result.isFailure)
        assertEquals("Name must be at least 2 characters", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateName sends correct request dto`() = runTest {
        fakeApi.updateProfileResponse = Response.success(
            ProfileApiResponse(
                data = ProfileDto(id = 1, nim = null, email = "a@b.com", fullName = "New Name",
                    createdAt = null, updatedAt = null)
            )
        )

        repo.updateName("New Name")

        assertEquals("New Name", fakeApi.lastUpdateProfileRequest?.fullName)
    }

    companion object {
        private val JSON = "application/json".toMediaType()
    }
}

// ── Fake ApiService ──

class FakeApiService : ApiService {
    var loginResponse: Response<LoginApiResponse> = stub()
    var getProfileResponse: Response<ProfileApiResponse> = stub()
    var updateProfileResponse: Response<ProfileApiResponse> = stub()
    var getMyFamiliesResponse: Response<FamilyListApiResponse> = stub()
    var getDiscoverFamiliesResponse: Response<FamilyListApiResponse> = stub()
    var getAllFamiliesResponse: Response<FamilyListApiResponse> = stub()
    var getFamilyDetailResponse: Response<FamilyDetailApiResponse> = stub()
    var createFamilyResponse: Response<FamilyDetailApiResponse> = stub()
    var joinFamilyResponse: Response<FamilyDetailApiResponse> = stub()
    var leaveFamilyResponse: Response<FamilyDetailApiResponse> = stub()

    var lastUpdateProfileRequest: UpdateProfileRequestDto? = null
    var lastLoginRequest: LoginRequestDto? = null

    override suspend fun login(request: LoginRequestDto): Response<LoginApiResponse> {
        lastLoginRequest = request
        return loginResponse
    }

    override suspend fun getProfile() = getProfileResponse

    override suspend fun updateProfile(request: UpdateProfileRequestDto): Response<ProfileApiResponse> {
        lastUpdateProfileRequest = request
        return updateProfileResponse
    }

    override suspend fun getMyFamilies() = getMyFamiliesResponse
    override suspend fun getDiscoverFamilies() = getDiscoverFamiliesResponse
    override suspend fun getAllFamilies() = getAllFamiliesResponse
    override suspend fun getFamilyDetail(id: String) = getFamilyDetailResponse
    override suspend fun createFamily(request: CreateFamilyRequestDto) = createFamilyResponse
    override suspend fun joinFamily(request: JoinFamilyRequestDto) = joinFamilyResponse
    override suspend fun leaveFamily(request: LeaveFamilyRequestDto) = leaveFamilyResponse

    private fun <T> stub(): Response<T> =
        Response.error(500, "stub not configured".toResponseBody("text/plain".toMediaType()))
}
