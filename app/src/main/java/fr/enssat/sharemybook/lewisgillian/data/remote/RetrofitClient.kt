package fr.enssat.sharemybook.lewisgillian.data.remote

import fr.enssat.sharemybook.lewisgillian.data.remote.api.OpenLibraryApiService
import fr.enssat.sharemybook.lewisgillian.data.remote.api.ShareMyBookApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val openLibraryRetrofit = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val shareMyBookRetrofit = Retrofit.Builder()
        .baseUrl("https://europe-west9-mythic-cocoa-442917-i7.cloudfunctions.net/shareMyBook/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val openLibraryApi: OpenLibraryApiService by lazy {
        openLibraryRetrofit.create(OpenLibraryApiService::class.java)
    }
    
    val shareMyBookApi: ShareMyBookApiService by lazy {
        shareMyBookRetrofit.create(ShareMyBookApiService::class.java)
    }
}
