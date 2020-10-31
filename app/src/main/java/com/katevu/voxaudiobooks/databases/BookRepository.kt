package com.katevu.voxaudiobooks.databases

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.katevu.voxaudiobooks.models.BookParcel
import java.util.concurrent.Executors

private const val DATABASE_NAME = "booksfavourite-database"
private const val TAG = "BookRepository"

class BookRepository private constructor(context: Context) {

    private val database: BooksDatabase = Room.databaseBuilder(
        context.applicationContext,
        BooksDatabase::class.java,
        DATABASE_NAME
    )//.addMigrations(migration_1_2)
        .build()
    private val bookDao = database.bookDao()

    private val executor = Executors.newSingleThreadExecutor()


    fun getBooksDB(): LiveData<List<BookParcel>> {
        Log.d(TAG, ".getBooksDB called ${bookDao.getBooksDB()}")
        return bookDao.getBooksDB()
    }
//    suspend fun getBooksDB(): List<BookParcel> {
//            Log.d(TAG, ".getBooks called ${bookDao.getBooksDB()}")
//            return bookDao.getBooksDB()
//    }

    fun getBookDB2(identifier: String): LiveData<BookParcel?> {
        return bookDao.getBookDB2(identifier)
    }


    suspend fun getBookDB(identifier: String): BookParcel? = bookDao.getBookDB(identifier)

    fun addBook(book: BookParcel) {
        Log.d(TAG, ".addBook called}")
        executor.execute {
            bookDao.addBook(book)
        }

    }

    fun deleteBook(book: BookParcel) {
        Log.d(TAG, ".deleteBook called}")
        executor.execute {
            bookDao.deleteBook(book)
        }

    }


//    suspend fun insertAll(books: List<BookParcel>) {
//        try {
//            Log.d(TAG, ".insertAll call")
//            bookDao.insertAll(books)
//        } catch (e: Error) {
//            Log.d(TAG, ".insertAll error: ${e.message}")
//        }
//    }

    companion object {
        private var INSTANCE: BookRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = BookRepository(context)
            }
        }

        fun get(): BookRepository {
            Log.d(TAG, ".get called $INSTANCE")
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be init")
        }
    }

}

