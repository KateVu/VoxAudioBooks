package com.katevu.voxaudiobooks.databases

/**
 * Author: Kate Vu
 */
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.katevu.voxaudiobooks.models.BookParcel

private const val DATABASE_NAME = "books-database"

@Database(entities = [BookParcel::class], version = 1, exportSchema = false)
abstract class BooksDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE books ADD COLUMN isFavourite INTEGER NOT NULL DEFAULT 0"
        )
    }
}
