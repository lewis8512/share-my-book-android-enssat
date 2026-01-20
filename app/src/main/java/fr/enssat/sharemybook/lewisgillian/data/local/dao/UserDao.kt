package fr.enssat.sharemybook.lewisgillian.data.local.dao

import androidx.room.*
import fr.enssat.sharemybook.lewisgillian.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE isCurrentUser = 1")
    suspend fun getCurrentUser(): UserEntity?
    
    @Query("SELECT * FROM users WHERE isCurrentUser = 1")
    fun observeCurrentUser(): Flow<UserEntity?>
    
    @Query("SELECT * FROM users WHERE uid = :userUid")
    suspend fun getUserByUid(userUid: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE isCurrentUser = 0 ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE isCurrentUser = 0")
    suspend fun deleteAllContacts()
}
