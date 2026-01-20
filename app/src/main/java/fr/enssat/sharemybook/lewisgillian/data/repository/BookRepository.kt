package fr.enssat.sharemybook.lewisgillian.data.repository

import android.util.Log
import fr.enssat.sharemybook.lewisgillian.data.local.dao.BookDao
import fr.enssat.sharemybook.lewisgillian.data.mapper.toBook
import fr.enssat.sharemybook.lewisgillian.data.mapper.toDomain
import fr.enssat.sharemybook.lewisgillian.data.mapper.toEntity
import fr.enssat.sharemybook.lewisgillian.data.remote.api.OpenLibraryApiService
import fr.enssat.sharemybook.lewisgillian.domain.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BookRepository(
    private val bookDao: BookDao,
    private val openLibraryApi: OpenLibraryApiService
) {

    fun getAllUserBooks(userUuid: String): Flow<List<Book>> {
        return bookDao.getAllUserBooks(userUuid)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getLentBooks(userUuid: String): Flow<List<Book>> {
        return bookDao.getLentBooks(userUuid)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getBorrowedBooks(userUuid: String): Flow<List<Book>> {
        return bookDao.getBorrowedBooks(userUuid)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun getBookByUid(uid: String): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookByUid(uid)?.toDomain()
    }

    suspend fun bookExistsForIsbn(isbn: String, userUuid: String): Boolean = withContext(Dispatchers.IO) {
        try {
            bookDao.getBookByIsbn(isbn, userUuid) != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchBookByIsbn(isbn: String, currentUserUuid: String): Book? =
        withContext(Dispatchers.IO) {
            val bibkey = "ISBN:$isbn"
            val response = openLibraryApi.getBookByIsbn(bibkey)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isNotEmpty()) {
                    val bookData = body[bibkey]
                    if (bookData != null) {
                        bookData.toBook(isbn, currentUserUuid)
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else {
                throw Exception("Erreur API: ${response.code()}")
            }
        }

    suspend fun addBook(book: Book): Book = withContext(Dispatchers.IO) {
        val entity = book.toEntity()
        bookDao.insertBook(entity)
        Log.d("BookRepository", "Livre ajoute: ${book.title}")
        book
    }

    suspend fun deleteBook(uid: String) = withContext(Dispatchers.IO) {
        val bookEntity = bookDao.getBookByUid(uid)
        if (bookEntity != null) {
            bookDao.deleteBook(bookEntity)
            Log.d("BookRepository", "Livre supprime: $uid")
        }
    }

    suspend fun updateBook(book: Book) = withContext(Dispatchers.IO) {
        val entity = book.toEntity()
        bookDao.updateBook(entity)
    }

    suspend fun markAsLent(bookUid: String, borrowerUuid: String) =
        withContext(Dispatchers.IO) {
            bookDao.markAsLent(bookUid, borrowerUuid)
            Log.d("BookRepository", "Livre prete: $bookUid -> $borrowerUuid")
        }

    suspend fun markAsReturned(bookUid: String) = withContext(Dispatchers.IO) {
        bookDao.markAsReturned(bookUid)
        Log.d("BookRepository", "Livre retourne: $bookUid")
    }

    suspend fun addBorrowedBook(
        uid: String,
        isbn: String,
        title: String,
        authors: String,
        coverUrl: String?,
        ownerUid: String,
        borrowerUid: String
    ) = withContext(Dispatchers.IO) {
        val book = Book(
            uid = uid,
            isbn = isbn,
            title = title,
            authors = authors,
            coverUrl = coverUrl,
            ownerUuid = ownerUid,
            borrowerUuid = borrowerUid,
            createdAt = System.currentTimeMillis()
        )
        val entity = book.toEntity()
        bookDao.insertBook(entity)
        Log.d("BookRepository", "Livre emprunte ajoute: $title")
    }
}
