package com.chronokeep.registration.network.chronokeep

import com.chronokeep.registration.network.chronokeep.requests.AddParticipantsRequest
import com.chronokeep.registration.network.chronokeep.requests.GetParticipantsRequest
import com.chronokeep.registration.network.chronokeep.requests.LoginRequest
import com.chronokeep.registration.network.chronokeep.requests.RefreshTokenRequest
import com.chronokeep.registration.network.chronokeep.requests.UpdateParticipantsRequest
import com.chronokeep.registration.network.chronokeep.responses.AddParticipantsResponse
import com.chronokeep.registration.network.chronokeep.responses.GetAccountResponse
import com.chronokeep.registration.network.chronokeep.responses.GetParticipantsResponse
import com.chronokeep.registration.network.chronokeep.responses.LoginResponse
import com.chronokeep.registration.network.chronokeep.responses.UpdateParticipantsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChronokeepService {
    @Headers("Accept: application/json")
    @POST("account")
    fun getEvents(@Header("Authorization") auth: String): Call<GetAccountResponse>
    @Headers("Accept: application/json")
    @POST("account/login")
    fun login(@Body dataModel: LoginRequest): Call<LoginResponse>
    @Headers("Accept: application/json")
    @POST("account/refresh")
    fun refresh(@Body dataModel: RefreshTokenRequest): Call<LoginResponse>
    @Headers("Accept: application/json")
    @POST("r/participants")
    fun getParticipants(@Header("Authorization") auth: String, @Body dataModel: GetParticipantsRequest): Call<GetParticipantsResponse>
    @Headers("Accept: application/json")
    @POST("r/participants/add-many")
    fun addParticipants(@Header("Authorization") auth: String, @Body dataModel: AddParticipantsRequest): Call<AddParticipantsResponse>
    @Headers("Accept: application/json")
    @POST("r/participants/update-many")
    fun updateParticipants(@Header("Authorization") auth: String, @Body dataModel: UpdateParticipantsRequest): Call<UpdateParticipantsResponse>
}