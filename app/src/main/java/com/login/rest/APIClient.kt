package com.login.rest

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class APIClient {
    private var retrofit: Retrofit? = null
    private val baseUrl: String = "https://graph.instagram.com/v14.0/"
    var baseClient : OkHttpClient?=OkHttpClient().newBuilder().connectTimeout(80, TimeUnit.SECONDS)
        .readTimeout(80, TimeUnit.SECONDS).writeTimeout(80, TimeUnit.SECONDS)
        .addInterceptor(LoginInterceptor()).build()
    fun getClient(): Retrofit? {
        if (retrofit == null) {
            val okHttpClient = OkHttpClient().newBuilder().connectTimeout(80, TimeUnit.SECONDS)
                .readTimeout(80, TimeUnit.SECONDS).writeTimeout(80, TimeUnit.SECONDS)
                .addInterceptor(LoginInterceptor()).build()

            retrofit = Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient).addConverterFactory(
                GsonConverterFactory.create()).build()
        }
        return retrofit
    }
    class LoginInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val t1 = System.nanoTime()
            Log.e("OkHttp", String.format("--> Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()))
            try {
                val requestBuffer = Buffer()
                Log.e("OkHttp", requestBuffer.readUtf8().replace("=", ":").replace("&", "\n"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val response = chain.proceed(request)
            val t2 = System.nanoTime()
            Log.e("OkHttp", String.format("<-- Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6, response.headers()))
            val contentType = response.body()!!.contentType()
            val content = response.body()!!.string()
            Log.e("OkHttp", content)
            val wrappedBody = ResponseBody.create(contentType, content)
            return response.newBuilder().body(wrappedBody).build()
        }
    }
}