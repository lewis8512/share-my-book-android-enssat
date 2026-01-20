package fr.enssat.sharemybook.lewisgillian.data.remote.api

import fr.enssat.sharemybook.lewisgillian.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ShareMyBookApiService {
    
    @POST("init")
    suspend fun initTransaction(
        @Body request: TransactionInitRequest
    ): Response<TransactionInitResponse>
    
    @POST("accept/{shareId}")
    suspend fun acceptTransaction(
        @Path("shareId") shareId: String,
        @Body request: TransactionAcceptRequest
    ): Response<TransactionResponse>
    
    @GET("result/{shareId}")
    suspend fun getTransactionResult(
        @Path("shareId") shareId: String
    ): Response<TransactionResponse>
}
