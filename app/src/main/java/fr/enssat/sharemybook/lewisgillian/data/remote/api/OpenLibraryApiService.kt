package fr.enssat.sharemybook.lewisgillian.data.remote.api

import fr.enssat.sharemybook.lewisgillian.data.remote.dto.BookData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApiService {
    
    @GET("api/books")
    suspend fun getBookByIsbn(
        @Query("bibkeys") bibkeys: String,
        @Query("jscmd") jscmd: String = "data",
        @Query("format") format: String = "json"
    ): Response<Map<String, BookData>>
}
