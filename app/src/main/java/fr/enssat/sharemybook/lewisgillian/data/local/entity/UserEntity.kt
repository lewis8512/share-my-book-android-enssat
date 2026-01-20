package fr.enssat.sharemybook.lewisgillian.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    
    val fullName: String,
    val tel: String,
    val email: String,
    
    val isCurrentUser: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis()
)
