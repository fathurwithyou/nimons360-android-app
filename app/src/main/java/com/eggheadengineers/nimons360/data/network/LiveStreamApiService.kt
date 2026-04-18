package com.eggheadengineers.nimons360.data.network

import com.eggheadengineers.nimons360.data.dto.EndStreamRequestDto
import com.eggheadengineers.nimons360.data.dto.LiveStreamApiResponse
import com.eggheadengineers.nimons360.data.dto.LiveStreamListApiResponse
import com.eggheadengineers.nimons360.data.dto.StartStreamRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface LiveStreamApiService {

    @POST("api/streams/start")
    suspend fun startStream(@Body request: StartStreamRequestDto): Response<LiveStreamApiResponse>

    @HTTP(method = "DELETE", path = "api/streams/{streamId}", hasBody = true)
    suspend fun endStream(
        @Path("streamId") streamId: String,
        @Body request: EndStreamRequestDto,
    ): Response<Unit>

    @GET("api/families/{familyId}/streams")
    suspend fun listStreams(@Path("familyId") familyId: String): Response<LiveStreamListApiResponse>
}
