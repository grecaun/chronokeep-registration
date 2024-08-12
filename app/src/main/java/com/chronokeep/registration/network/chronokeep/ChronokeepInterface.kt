package com.chronokeep.registration.network.chronokeep

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.chronokeep.registration.network.chronokeep.objects.ChronokeepEvent
import com.chronokeep.registration.network.chronokeep.objects.ChronokeepParticipant
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
import com.chronokeep.registration.objects.database.DatabaseParticipant
import com.chronokeep.registration.objects.database.DatabaseSetting
import com.chronokeep.registration.util.Constants
import com.chronokeep.registration.util.Globals
import kotlinx.serialization.json.Json
import okhttp3.internal.http.HttpMethod
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class ChronokeepInterface {
    companion object {
        @Volatile
        private var INSTANCE: ChronokeepInterface? = null
        fun getInstance() =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChronokeepInterface().also {
                    INSTANCE = it
                }
            }

        const val HTTP_STATUS_UNAUTHORIZED = 401
        const val BASE_URL: String = "https://api.chronokeep.com/"
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
    }
    private val service: ChronokeepService by lazy { retrofit.create(ChronokeepService::class.java) }

    private val tag = "WebInterface"

    data class LoginInfo(val error: String?, val token: String?, val refresh: String?)
    data class EventInfo(val error: String?, val events: List<ChronokeepEvent>?)
    data class ParticipantInfo(val error: String?, val participants: List<ChronokeepParticipant>?, val slug: String?, val year: String?)

    fun login(
        email: String,
        password: String,
        context: Context,
        success: (LoginResponse?) -> Unit,
        failure: () -> Unit,
        ) {
        Log.d(tag, "Logging in.")
        val call: Call<LoginResponse> = service.login(LoginRequest(email=email,password=password))
        call.enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val info: LoginResponse = response.body() as LoginResponse
                    success(info)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(context, "Login failed.", Toast.LENGTH_SHORT).show()
                failure()
            }
        })
    }

    fun getEvents(
        token: String,
        refresh: String,
        context: Context,
        success: (GetAccountResponse?) -> Unit,
        failure: () -> Unit,
    ) {
        Log.d(tag, "Getting events.")
        val call: Call<GetAccountResponse> = service.getEvents("Bearer $token")
        call.enqueue(object: Callback<GetAccountResponse> {
            override fun onResponse(
                call: Call<GetAccountResponse>,
                response: Response<GetAccountResponse>
            ) {
                if (response.isSuccessful) {
                    val info: GetAccountResponse = response.body() as GetAccountResponse
                    success(info)
                } else if (response.code() == HTTP_STATUS_UNAUTHORIZED) {
                    val refreshCall: Call<LoginResponse> = service.refresh(RefreshTokenRequest(refresh))
                    refreshCall.enqueue(object: Callback<LoginResponse> {
                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            if (response.isSuccessful) {
                                val loginInfo: LoginResponse = response.body() as LoginResponse
                                try {
                                    val settingsDao = Globals.getDatabase()!!.settingDao()
                                    settingsDao.addSetting(DatabaseSetting(Constants.setting_auth_token, loginInfo.access_token))
                                    settingsDao.addSetting(DatabaseSetting(Constants.setting_refresh_token, loginInfo.refresh_token))
                                    val repeat: Call<GetAccountResponse> = service.getEvents("Bearer ${loginInfo.access_token}")
                                    repeat.enqueue(object: Callback<GetAccountResponse> {
                                        override fun onResponse(
                                            call: Call<GetAccountResponse>,
                                            response: Response<GetAccountResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                val info: GetAccountResponse =
                                                    response.body() as GetAccountResponse
                                                success(info)
                                            } else {
                                                Toast.makeText(context, "Unable to get events.", Toast.LENGTH_SHORT).show()
                                                failure()
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<GetAccountResponse>,
                                            t: Throwable
                                        ) {
                                            Toast.makeText(context, "Unable to get events.", Toast.LENGTH_SHORT).show()
                                            failure()
                                        }

                                    })
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error when trying to fetch after refresh.", Toast.LENGTH_SHORT).show()
                                    failure()
                                }
                            } else {
                                Toast.makeText(context, "Unable to refresh authorization data.", Toast.LENGTH_SHORT).show()
                                failure()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Toast.makeText(context, "Unable to refresh authentication data.", Toast.LENGTH_SHORT).show()
                            failure()
                        }

                    })
                }
            }

            override fun onFailure(call: Call<GetAccountResponse>, t: Throwable) {
                Toast.makeText(context, "Unable to get events.", Toast.LENGTH_SHORT).show()
                failure()
            }

        })
    }

    fun getParticipants(
        token: String,
        refresh: String,
        slug: String,
        context: Context,
        success: (GetParticipantsResponse?) -> Unit,
        failure: () -> Unit,
    ) {
        Log.d(tag, "Getting participants.")
        val call: Call<GetParticipantsResponse> = service.getParticipants("Bearer $token", GetParticipantsRequest(slug, null))
        call.enqueue(object: Callback<GetParticipantsResponse> {
            override fun onResponse(
                call: Call<GetParticipantsResponse>,
                response: Response<GetParticipantsResponse>
            ) {
                if (response.isSuccessful) {
                    val info: GetParticipantsResponse = response.body() as GetParticipantsResponse
                    success(info)
                } else if (response.code() == HTTP_STATUS_UNAUTHORIZED) {
                    val refreshCall: Call<LoginResponse> = service.refresh(RefreshTokenRequest(refresh))
                    refreshCall.enqueue(object: Callback<LoginResponse> {
                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            if (response.isSuccessful) {
                                val loginInfo: LoginResponse = response.body() as LoginResponse
                                try {
                                    val settingsDao = Globals.getDatabase()!!.settingDao()
                                    settingsDao.addSetting(DatabaseSetting(Constants.setting_auth_token, loginInfo.access_token))
                                    settingsDao.addSetting(DatabaseSetting(Constants.setting_refresh_token, loginInfo.refresh_token))
                                    val repeat: Call<GetParticipantsResponse> = service.getParticipants("Bearer ${loginInfo.access_token}", GetParticipantsRequest(slug, null))
                                    repeat.enqueue(object: Callback<GetParticipantsResponse> {
                                        override fun onResponse(
                                            call: Call<GetParticipantsResponse>,
                                            response: Response<GetParticipantsResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                val info: GetParticipantsResponse =
                                                    response.body() as GetParticipantsResponse
                                                success(info)
                                            } else {
                                                Toast.makeText(context, "Unable to get participants.", Toast.LENGTH_SHORT).show()
                                                failure()
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<GetParticipantsResponse>,
                                            t: Throwable
                                        ) {
                                            Toast.makeText(context, "Unable to get participants.", Toast.LENGTH_SHORT).show()
                                            failure()
                                        }

                                    })
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error when trying to fetch after refresh.", Toast.LENGTH_SHORT).show()
                                    failure()
                                }
                            } else {
                                Toast.makeText(context, "Unable to refresh authentication data.", Toast.LENGTH_SHORT).show()
                                failure()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Toast.makeText(context, "Unable to refresh authentication data.", Toast.LENGTH_SHORT).show()
                            failure()
                        }

                    })
                } else {
                    Toast.makeText(context, "Unable to get participants.", Toast.LENGTH_SHORT).show()
                    failure()
                }
            }

            override fun onFailure(call: Call<GetParticipantsResponse>, t: Throwable) {
                Toast.makeText(context, "Unable to get participants.", Toast.LENGTH_SHORT).show()
                failure()
            }

        })
    }

    fun updateParticipant(
        token: String,
        refresh: String,
        slug: String,
        year: String,
        participants: List<DatabaseParticipant>,
        context: Context,
        success: (UpdateParticipantsResponse?) -> Unit,
        failure: () -> Unit,
    ) {
        Log.d(tag, "Updating participants.")
        val converted = ArrayList<ChronokeepParticipant>()
        for (part in participants) {
            converted.add(part.toChronokeepParticipant())
        }
        val call: Call<UpdateParticipantsResponse> = service.updateParticipants("Bearer $token", UpdateParticipantsRequest(slug, year, converted))
        call.enqueue(object: Callback<UpdateParticipantsResponse> {
            override fun onResponse(
                call: Call<UpdateParticipantsResponse>,
                response: Response<UpdateParticipantsResponse>
            ) {
                if (response.isSuccessful) {
                    val info: UpdateParticipantsResponse = response.body() as UpdateParticipantsResponse
                    success(info)
                } else if (response.code() == HTTP_STATUS_UNAUTHORIZED) {
                    val refreshCall: Call<LoginResponse> = service.refresh(RefreshTokenRequest(refresh))
                    refreshCall.enqueue(object: Callback<LoginResponse> {
                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            if (response.isSuccessful) {
                                val loginInfo: LoginResponse = response.body() as LoginResponse
                                try {
                                    val settingsDao = Globals.getDatabase()!!.settingDao()
                                    settingsDao.addSetting(DatabaseSetting(Constants.setting_auth_token, loginInfo.access_token))
                                    settingsDao.addSetting(DatabaseSetting(Constants.setting_refresh_token, loginInfo.refresh_token))
                                    val repeat: Call<UpdateParticipantsResponse> = service.updateParticipants("Bearer $token", UpdateParticipantsRequest(slug, year, converted))
                                    repeat.enqueue(object: Callback<UpdateParticipantsResponse> {
                                        override fun onResponse(
                                            call: Call<UpdateParticipantsResponse>,
                                            response: Response<UpdateParticipantsResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                val info: UpdateParticipantsResponse = response.body() as UpdateParticipantsResponse
                                                success(info)
                                            } else {
                                                Toast.makeText(context, "Unable to update participants.", Toast.LENGTH_SHORT).show()
                                                failure()
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<UpdateParticipantsResponse>,
                                            t: Throwable
                                        ) {
                                            Toast.makeText(context, "Unable to update participants.", Toast.LENGTH_SHORT).show()
                                            failure()
                                        }

                                    })
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to update participants.", Toast.LENGTH_SHORT).show()
                                    failure()
                                }
                            } else {
                                Toast.makeText(context, "Unable to refresh authorization data.", Toast.LENGTH_SHORT).show()
                                failure()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Toast.makeText(context, "Unable to refresh authorization data.", Toast.LENGTH_SHORT).show()
                            failure()
                        }

                    })
                } else {
                    Toast.makeText(context, "Unable to update participants.", Toast.LENGTH_SHORT).show()
                    failure()
                }
            }

            override fun onFailure(call: Call<UpdateParticipantsResponse>, t: Throwable) {
                Toast.makeText(context, "Unable to update participants.", Toast.LENGTH_SHORT).show()
                failure()
            }
        })
    }

    fun addParticipant(
        token: String,
        refresh: String,
        slug: String,
        year: String,
        participants: List<DatabaseParticipant>,
        context: Context,
        success: (AddParticipantsResponse?) -> Unit,
        failure: () -> Unit
    ) {
        val converted = ArrayList<ChronokeepParticipant>()
        for (part in participants) {
            converted.add(part.toChronokeepParticipant())
        }
        val call: Call<AddParticipantsResponse> = service.addParticipants("Bearer $token", AddParticipantsRequest(slug, year, converted))
        call.enqueue(object: Callback<AddParticipantsResponse> {
            override fun onResponse(
                call: Call<AddParticipantsResponse>,
                response: Response<AddParticipantsResponse>
            ) {
                if (response.isSuccessful) {
                    val info: AddParticipantsResponse = response.body() as AddParticipantsResponse
                    success(info)
                } else if (response.code() == HTTP_STATUS_UNAUTHORIZED) {
                    val refreshCall: Call<LoginResponse> = service.refresh(RefreshTokenRequest(refresh))
                    refreshCall.enqueue(object: Callback<LoginResponse> {
                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            if (response.isSuccessful) {
                                val loginInfo: LoginResponse = response.body() as LoginResponse
                                try {
                                    val settingsDao = Globals.getDatabase()!!.settingDao()
                                    settingsDao.addSetting(DatabaseSetting(Constants.setting_auth_token, loginInfo.access_token))
                                    settingsDao.addSetting(DatabaseSetting(Constants.setting_refresh_token, loginInfo.refresh_token))
                                    val repeat: Call<AddParticipantsResponse> = service.addParticipants("Bearer $token", AddParticipantsRequest(slug, year, converted))
                                    repeat.enqueue(object: Callback<AddParticipantsResponse> {
                                        override fun onResponse(
                                            call: Call<AddParticipantsResponse>,
                                            response: Response<AddParticipantsResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                val info: AddParticipantsResponse = response.body() as AddParticipantsResponse
                                                success(info)
                                            } else {
                                                Toast.makeText(context, "Unable to add participants.", Toast.LENGTH_SHORT).show()
                                                failure()
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<AddParticipantsResponse>,
                                            t: Throwable
                                        ) {
                                            Toast.makeText(context, "Unable to add participants.", Toast.LENGTH_SHORT).show()
                                            failure()
                                        }

                                    })
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to add participants.", Toast.LENGTH_SHORT).show()
                                    failure()
                                }
                            } else {
                                Toast.makeText(context, "Unable to refresh authorization data.", Toast.LENGTH_SHORT).show()
                                failure()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Toast.makeText(context, "Unable to refresh authorization data.", Toast.LENGTH_SHORT).show()
                            failure()
                        }

                    })
                } else {
                    Toast.makeText(context, "Unable to add participants.", Toast.LENGTH_SHORT).show()
                    failure()
                }
            }

            override fun onFailure(call: Call<AddParticipantsResponse>, t: Throwable) {
                Toast.makeText(context, "Unable to add participants.", Toast.LENGTH_SHORT).show()
                failure()
            }
        })
    }
}