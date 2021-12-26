package network

import retrofit2.http.GET
import retrofit2.http.Query

interface API {

    @GET("api/v1.0/random")
    suspend fun getRandom(
        @Query("min") fromNumber: Int,
        @Query("max") toNumber: Int,
        @Query("count") numbersOfResults: Int,
    ): List<Int>

    @GET("api/v1.0/stoCazzo")
    suspend fun brokenAPI(): List<Int>

    @GET("api/v1.0/stoCazzo")
    suspend fun brokenAPIResult(): Result<List<Int>>

}