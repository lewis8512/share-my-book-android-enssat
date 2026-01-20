package fr.enssat.sharemybook.lewisgillian.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.enssat.sharemybook.lewisgillian.data.local.dao.BookDao
import fr.enssat.sharemybook.lewisgillian.data.local.dao.UserDao
import fr.enssat.sharemybook.lewisgillian.data.local.entity.BookEntity
import fr.enssat.sharemybook.lewisgillian.data.local.entity.UserEntity

@Database(
    entities = [BookEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun bookDao(): BookDao
    abstract fun userDao(): UserDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "sharemybook_database"
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
