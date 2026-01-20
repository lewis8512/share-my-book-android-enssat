package fr.enssat.sharemybook.lewisgillian.data.local.dao

import androidx.room.*
import fr.enssat.sharemybook.lewisgillian.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface BookDao {

    @Query("SELECT * FROM books WHERE ownerUuid = :userUuid OR borrowerUuid = :userUuid ORDER BY createdAt DESC")
    fun getAllUserBooks(userUuid: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE ownerUuid = :userUuid AND borrowerUuid IS NOT NULL")
    fun getLentBooks(userUuid: String): Flow<List<BookEntity>>
    
    @Query("SELECT * FROM books WHERE borrowerUuid = :userUuid")
    fun getBorrowedBooks(userUuid: String): Flow<List<BookEntity>>
    
    @Query("SELECT * FROM books WHERE uid = :bookUid")
    suspend fun getBookByUid(bookUid: String): BookEntity?
    
    @Query("SELECT * FROM books WHERE isbn = :isbn AND ownerUuid = :userUuid")
    suspend fun getBookByIsbn(isbn: String, userUuid: String): BookEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Update
    suspend fun updateBook(book: BookEntity)
    
    @Delete
    suspend fun deleteBook(book: BookEntity)
    
    @Query("UPDATE books SET borrowerUuid = :borrowerUuid WHERE uid = :bookUid")
    suspend fun markAsLent(bookUid: String, borrowerUuid: String)
    
    @Query("UPDATE books SET borrowerUuid = NULL WHERE uid = :bookUid")
    suspend fun markAsReturned(bookUid: String)
    
}
