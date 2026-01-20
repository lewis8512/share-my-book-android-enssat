package fr.enssat.sharemybook.lewisgillian.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TransactionInitRequest(
    @SerializedName("action")
    val action: String, // LOAN ou RETURN
    
    @SerializedName("book")
    val book: TransactionBook,
    
    @SerializedName("owner")
    val owner: TransactionUser
)

data class TransactionInitResponse(
    @SerializedName("shareId")
    val shareId: String
)

data class TransactionAcceptRequest(
    @SerializedName("borrower")
    val borrower: TransactionUser
)

data class TransactionResponse(
    @SerializedName("action")
    val action: String,
    
    @SerializedName("book")
    val book: TransactionBook,
    
    @SerializedName("owner")
    val owner: TransactionUser,
    
    @SerializedName("borrower")
    val borrower: TransactionUser? = null
)

data class TransactionBook(
    @SerializedName("uid")
    val uid: String,
    
    @SerializedName("isbn")
    val isbn: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("authors")
    val authors: String,
    
    @SerializedName("covers")
    val covers: String? = null
)

data class TransactionUser(
    @SerializedName("uid")
    val uid: String,
    
    @SerializedName("fullName")
    val fullName: String,
    
    @SerializedName("tel")
    val tel: String,
    
    @SerializedName("email")
    val email: String
)
