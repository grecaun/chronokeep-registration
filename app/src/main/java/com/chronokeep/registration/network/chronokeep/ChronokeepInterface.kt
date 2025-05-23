package com.chronokeep.registration.network.chronokeep

import android.util.Log
import com.chronokeep.registration.network.chronokeep.objects.ChronokeepParticipant
import com.chronokeep.registration.network.chronokeep.requests.AddParticipantsRequest
import com.chronokeep.registration.network.chronokeep.requests.GetParticipantsRequest
import com.chronokeep.registration.network.chronokeep.requests.LoginRequest
import com.chronokeep.registration.network.chronokeep.requests.RefreshTokenRequest
import com.chronokeep.registration.network.chronokeep.requests.UpdateParticipantsRequest
import com.chronokeep.registration.network.chronokeep.responses.AddParticipantsResponse
import com.chronokeep.registration.network.chronokeep.responses.GetAllEventYearsResponse
import com.chronokeep.registration.network.chronokeep.responses.GetParticipantsResponse
import com.chronokeep.registration.network.chronokeep.responses.LoginResponse
import com.chronokeep.registration.network.chronokeep.responses.UpdateParticipantsResponse
import com.chronokeep.registration.objects.database.DatabaseParticipant
import com.chronokeep.registration.objects.database.DatabaseSetting
import com.chronokeep.registration.util.Constants
import com.chronokeep.registration.util.Globals
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

@Suppress("unused")
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

    fun login(
        email: String,
        password: String,
        success: (response: LoginResponse?) -> Unit,
        failure: (message: String) -> Unit,
        ) {
        Log.d(tag, "Logging in.")
        val call: Call<LoginResponse> = service.login(LoginRequest(email=email,password=password))
        call.enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val info: LoginResponse = response.body() as LoginResponse
                    success(info)
                } else {
                    failure("Unable to login. (0x02)")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d(tag, "error thrown: ${t.message}")
                failure("Unable to login. (0x01)")
            }
        })
    }

    fun getEvents(
        token: String,
        refresh: String,
        success: (response: GetAllEventYearsResponse?) -> Unit,
        failure: (message: String) -> Unit,
    ) {
        Log.d(tag, "Getting events.")
        val call: Call<GetAllEventYearsResponse> = service.getEvents("Bearer $token")
        call.enqueue(object: Callback<GetAllEventYearsResponse> {
            override fun onResponse(
                call: Call<GetAllEventYearsResponse>,
                response: Response<GetAllEventYearsResponse>
            ) {
                if (response.isSuccessful) {
                    val info: GetAllEventYearsResponse = response.body() as GetAllEventYearsResponse
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
                                    val repeat: Call<GetAllEventYearsResponse> = service.getEvents("Bearer ${loginInfo.access_token}")
                                    repeat.enqueue(object: Callback<GetAllEventYearsResponse> {
                                        override fun onResponse(
                                            call: Call<GetAllEventYearsResponse>,
                                            response: Response<GetAllEventYearsResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                val info: GetAllEventYearsResponse =
                                                    response.body() as GetAllEventYearsResponse
                                                success(info)
                                            } else {
                                                failure("Unable to get events. (0x22)")
                                            }
                                        }

                                        override fun onFailure(call: Call<GetAllEventYearsResponse>, t: Throwable) {
                                            Log.d(tag, "error thrown: ${t.message}")
                                            failure("Unable to get events. (0x21)")
                                        }

                                    })
                                } catch (e: Exception) {
                                    failure("Error with login info. (0x13)")
                                }
                            } else {
                                failure("Login information expired or not valid.")
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Log.d(tag, "error thrown: ${t.message}")
                            failure("Error with login info. (0x11)")
                        }

                    })
                } else {
                    failure("Invalid response from server. (0x02)")
                }
            }

            override fun onFailure(call: Call<GetAllEventYearsResponse>, t: Throwable) {
                Log.d(tag, "error thrown: ${t.message}")
                failure("Unable to get events. (0x01)")
            }

        })
    }

    fun getParticipants(
        token: String,
        refresh: String,
        slug: String,
        year: String,
        limit: Int,
        page: Int,
        success: (response: GetParticipantsResponse?) -> Unit,
        failure: (message: String) -> Unit,
    ) {
        Log.d(tag, "Getting participants.")
        val call: Call<GetParticipantsResponse> = service.getParticipants("Bearer $token", GetParticipantsRequest(slug, year, limit, page, null))
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
                                    val repeat: Call<GetParticipantsResponse> = service.getParticipants("Bearer ${loginInfo.access_token}", GetParticipantsRequest(slug, year, limit, page, null))
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
                                                failure("Unable to get participants. (0x22)")
                                            }
                                        }

                                        override fun onFailure(call: Call<GetParticipantsResponse>, t: Throwable) {
                                            Log.d(tag, "error thrown: ${t.message}")
                                            failure("Unable to get participants. (0x21)")
                                        }

                                    })
                                } catch (e: Exception) {
                                    failure("Error with login info. (0x13)")
                                }
                            } else {
                                failure("Login information expired or not valid.")
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Log.d(tag, "error thrown: ${t.message}")
                            failure("Error with login info. (0x11)")
                        }

                    })
                } else {
                    failure("Unable to get participants. (0x02)")
                }
            }

            override fun onFailure(call: Call<GetParticipantsResponse>, t: Throwable) {
                Log.d(tag, "error thrown: ${t.message}")
                failure("Unable to get participants. (0x01)")
            }

        })
    }

    fun updateParticipant(
        token: String,
        refresh: String,
        slug: String,
        year: String,
        participants: List<DatabaseParticipant>,
        updated_after: Long?,
        success: (response: UpdateParticipantsResponse?) -> Unit,
        failure: (message: String) -> Unit,
    ) {
        Log.d(tag, "Updating participants.")
        val converted = ArrayList<ChronokeepParticipant>()
        for (part in participants) {
            converted.add(part.toChronokeepParticipant())
        }
        val call: Call<UpdateParticipantsResponse> = service.updateParticipants("Bearer $token", UpdateParticipantsRequest(slug, year, converted, updated_after))
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
                                    val repeat: Call<UpdateParticipantsResponse> = service.updateParticipants("Bearer ${loginInfo.access_token}", UpdateParticipantsRequest(slug, year, converted, updated_after))
                                    repeat.enqueue(object: Callback<UpdateParticipantsResponse> {
                                        override fun onResponse(
                                            call: Call<UpdateParticipantsResponse>,
                                            response: Response<UpdateParticipantsResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                val info: UpdateParticipantsResponse = response.body() as UpdateParticipantsResponse
                                                success(info)
                                            } else {
                                                failure("Unable to update participants. (0x22)")
                                            }
                                        }

                                        override fun onFailure(call: Call<UpdateParticipantsResponse>, t: Throwable) {
                                            Log.d(tag, "error thrown: ${t.message}")
                                            failure("Unable to update participants. (0x21)")
                                        }

                                    })
                                } catch (e: Exception) {
                                    failure("Unable to update participants. (0x13)")
                                }
                            } else {
                                failure("Login information expired or not valid.")
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Log.d(tag, "error thrown: ${t.message}")
                            failure("Error with login info. (0x11)")
                        }

                    })
                } else {
                    failure("Unable to update participants. (0x02)")
                }
            }

            override fun onFailure(call: Call<UpdateParticipantsResponse>, t: Throwable) {
                Log.d(tag, "error thrown: ${t.message}")
                failure("Unable to update participants. (0x01)")
            }
        })
    }

    fun addParticipant(
        token: String,
        refresh: String,
        slug: String,
        year: String,
        participants: List<DatabaseParticipant>,
        updated_after: Long?,
        success: (response: AddParticipantsResponse?) -> Unit,
        failure: (message: String) -> Unit
    ) {
        Log.d(tag, "Adding participants.")
        val converted = ArrayList<ChronokeepParticipant>()
        for (part in participants) {
            converted.add(part.toChronokeepParticipant())
        }
        val call: Call<AddParticipantsResponse> = service.addParticipants("Bearer $token", AddParticipantsRequest(slug, year, converted, updated_after))
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
                                    val repeat: Call<AddParticipantsResponse> = service.addParticipants("Bearer ${loginInfo.access_token}", AddParticipantsRequest(slug, year, converted, updated_after))
                                    repeat.enqueue(object: Callback<AddParticipantsResponse> {
                                        override fun onResponse(
                                            call: Call<AddParticipantsResponse>,
                                            response: Response<AddParticipantsResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                val info: AddParticipantsResponse = response.body() as AddParticipantsResponse
                                                success(info)
                                            } else {
                                                failure("Unable to add participants. (0x22)")
                                            }
                                        }

                                        override fun onFailure(call: Call<AddParticipantsResponse>, t: Throwable) {
                                            Log.d(tag, "error thrown: ${t.message}")
                                            failure("Unable to add participants. (0x21)")
                                        }

                                    })
                                } catch (e: Exception) {
                                    failure("Unable to add participants. (0x13)")
                                }
                            } else {
                                failure("Login information expired or not valid.")
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Log.d(tag, "error thrown: ${t.message}")
                            failure("Error with login info. (0x11)")
                        }

                    })
                } else {
                    failure("Unable to add participants. (0x02)")
                }
            }

            override fun onFailure(call: Call<AddParticipantsResponse>, t: Throwable) {
                Log.d(tag, "error thrown: ${t.message}")
                failure("Unable to add participants. (0x01)")
            }
        })
    }
}