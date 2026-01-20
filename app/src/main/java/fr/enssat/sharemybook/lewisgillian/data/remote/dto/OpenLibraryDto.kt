package fr.enssat.sharemybook.lewisgillian.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenLibraryResponse(
    @SerializedName("ISBN:0596156715")
    val bookData: Map<String, BookData>? = null
)

data class BookData(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("authors")
    val authors: List<Author>? = null,
    
    @SerializedName("cover")
    val cover: Cover? = null,
    
    @SerializedName("publishers")
    val publishers: List<Publisher>? = null,
    
    @SerializedName("publish_date")
    val publishDate: String? = null,
    
    @SerializedName("number_of_pages")
    val numberOfPages: Int? = null
)

data class Author(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("url")
    val url: String? = null
)

data class Cover(
    @SerializedName("small")
    val small: String? = null,
    
    @SerializedName("medium")
    val medium: String? = null,
    
    @SerializedName("large")
    val large: String? = null
)

data class Publisher(
    @SerializedName("name")
    val name: String
)
