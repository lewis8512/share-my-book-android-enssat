package fr.enssat.sharemybook.lewisgillian.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val uid: String = UUID.randomUUID().toString(),
    
    val isbn: String,
    val title: String,
    val authors: String,
    val coverUrl: String? = null,
    
    val ownerUuid: String,
    
    val borrowerUuid: String? = null,
    
    val createdAt: Long = System.currentTimeMillis()
)
