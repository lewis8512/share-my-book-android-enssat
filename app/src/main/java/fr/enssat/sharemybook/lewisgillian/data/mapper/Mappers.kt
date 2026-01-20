package fr.enssat.sharemybook.lewisgillian.data.mapper

import fr.enssat.sharemybook.lewisgillian.data.local.entity.BookEntity
import fr.enssat.sharemybook.lewisgillian.data.local.entity.UserEntity
import fr.enssat.sharemybook.lewisgillian.data.remote.dto.BookData
import fr.enssat.sharemybook.lewisgillian.domain.model.Book
import fr.enssat.sharemybook.lewisgillian.domain.model.User

fun BookEntity.toDomain(): Book = Book(
    uid = uid,
    isbn = isbn,
    title = title,
    authors = authors,
    coverUrl = coverUrl,
    ownerUuid = ownerUuid,
    borrowerUuid = borrowerUuid,
    createdAt = createdAt
)

fun Book.toEntity(): BookEntity = BookEntity(
    uid = uid,
    isbn = isbn,
    title = title,
    authors = authors,
    coverUrl = coverUrl,
    ownerUuid = ownerUuid,
    borrowerUuid = borrowerUuid,
    createdAt = createdAt
)

fun UserEntity.toDomain(): User = User(
    uid = uid,
    fullName = fullName,
    tel = tel,
    email = email,
    isCurrentUser = isCurrentUser
)

fun User.toEntity(): UserEntity = UserEntity(
    uid = uid,
    fullName = fullName,
    tel = tel,
    email = email,
    isCurrentUser = isCurrentUser
)

fun BookData.toBook(isbn: String, ownerUuid: String): Book {
    val authorsString = authors?.joinToString(", ") { it.name } ?: "Unknown"
    val coverUrl = cover?.large ?: cover?.medium ?: cover?.small

    return Book(
        uid = java.util.UUID.randomUUID().toString(),
        isbn = isbn,
        title = title,
        authors = authorsString,
        coverUrl = coverUrl,
        ownerUuid = ownerUuid,
        borrowerUuid = null,
        createdAt = System.currentTimeMillis()
    )
}
