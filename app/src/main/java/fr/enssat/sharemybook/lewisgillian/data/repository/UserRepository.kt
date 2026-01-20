package fr.enssat.sharemybook.lewisgillian.data.repository

import fr.enssat.sharemybook.lewisgillian.data.local.dao.UserDao
import fr.enssat.sharemybook.lewisgillian.data.mapper.toDomain
import fr.enssat.sharemybook.lewisgillian.data.mapper.toEntity
import fr.enssat.sharemybook.lewisgillian.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class UserRepository(
    private val userDao: UserDao
) {

    fun observeCurrentUser(): Flow<User?> {
        return userDao.observeCurrentUser()
            .map { entity -> entity?.toDomain() }
    }

    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        userDao.getCurrentUser()?.toDomain()
    }

    suspend fun getUserByUid(uid: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByUid(uid)?.toDomain()
    }

    suspend fun saveCurrentUser(user: User) = withContext(Dispatchers.IO) {
        val userWithUid = if (user.uid.isBlank()) {
            user.copy(uid = generateUserUuid())
        } else {
            user
        }

        val entity = userWithUid.copy(isCurrentUser = true).toEntity()
        userDao.insertUser(entity)
    }

    suspend fun saveContact(user: User) = withContext(Dispatchers.IO) {
        val entity = user.copy(isCurrentUser = false).toEntity()
        userDao.insertUser(entity)
    }

    fun generateUserUuid(): String {
        return UUID.randomUUID().toString()
    }
}
