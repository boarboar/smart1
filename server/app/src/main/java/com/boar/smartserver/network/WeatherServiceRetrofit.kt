package com.boar.smartserver.network

import com.boar.smartserver.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object WeatherServiceRetrofit {
    fun obtain(
            readTimeoutInSeconds: Long = 1,
            connectTimeoutInSeconds: Long = 1
    ): Retrofit {
        //val loggingInterceptor = HttpLoggingInterceptor()
        //loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                        OkHttpClient
                                .Builder()
                                //.addInterceptor(loggingInterceptor)
                                .addInterceptor(makeLoggingInterceptor())
                                .addInterceptor(makeHeadersInterceptor())
                                .addInterceptor(makeAddQuerySecurityInterceptor())
                                .readTimeout(readTimeoutInSeconds, TimeUnit.SECONDS)
                                .connectTimeout(connectTimeoutInSeconds, TimeUnit.SECONDS)
                                .build()
                )
                .build()
    }
}


fun makeHeadersInterceptor() = Interceptor { chain ->
    chain.proceed(chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Accept-Language", "en")
            .addHeader("Content-Type", "application/json")
            .build())
}

fun makeLoggingInterceptor() = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
    else HttpLoggingInterceptor.Level.NONE
}

fun makeAddQuerySecurityInterceptor() = Interceptor { chain ->
    val originalRequest = chain.request()
// Url customization: add query parameters
    val url = originalRequest.url().newBuilder()
            .addQueryParameter("appid", "24da3cce6dac6c5bba10603f295ac1bc")
            .build()
// Request customization: set custom url
    val request = originalRequest
            .newBuilder()
            .url(url)
            .build()

    chain.proceed(request)
}