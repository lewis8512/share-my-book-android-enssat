package fr.enssat.sharemybook.lewisgillian.domain.model

data class Book(
    val uid: String,
    val isbn: String,
    val title: String,
    val authors: String,
    val coverUrl: String?,
    val ownerUuid: String,
    val borrowerUuid: String?,
    val createdAt: Long
) {
    fun isLent(): Boolean = borrowerUuid != null
    
    fun isOwnedBy(userUuid: String): Boolean = ownerUuid == userUuid
    
    fun isBorrowedBy(userUuid: String): Boolean = borrowerUuid == userUuid
}
