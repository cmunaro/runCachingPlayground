package network

import com.google.gson.GsonBuilder
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

object Network {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://www.randomnumberapi.com/")
        .addCallAdapterFactory(ResultAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    val service: API = retrofit.create(API::class.java)
}