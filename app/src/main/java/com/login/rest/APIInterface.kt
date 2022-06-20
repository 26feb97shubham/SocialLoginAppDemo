package com.login.rest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface APIInterface {
    @GET("{user_id}")
    fun getUserDetails(@QueryMap query : HashMap<String, String>) : Call<ResponseBody?>
}