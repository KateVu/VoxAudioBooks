package com.katevu.voxaudiobooks.databases

/**
 * Author: Kate Vu
 */
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.katevu.voxaudiobooks.api.NetworkService
import com.katevu.voxaudiobooks.api.NetworkServiceAudio
import com.katevu.voxaudiobooks.api.NetworkServiceSearch
import com.katevu.voxaudiobooks.api.URL_SEARCH_FORMAT
import com.katevu.voxaudiobooks.models.Audio
import com.katevu.voxaudiobooks.models.BookParcel
import com.katevu.voxaudiobooks.utils.AudioState
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

    /**
     * Check if the book book is in Favourite list
     */
    private suspend fun isFavourite(identifier: String): Boolean {
        val book = getBookDB(identifier)
        return (book != null)
    }

    /**
     * Get Librovox collection book in BookRepository class
     */
    suspend fun getAllBooks(): List<BookParcel>? {
        val fetchResult = NetworkService().voxBooksService.getAllBooks()
//                Log.d(TAG, "Raw data received: $fetchResult")
        val result = fetchResult.channel?.items?.filterNot {
            it.link.isBlank()
            it.guid.isBlank()
        }
        //                Log.d(TAG, "Data from internet: $resultParcel}")
        return result?.map { v ->
            BookParcel(
                v.guid,
                v.title,
                v.description,
                v.link,
                v.pubDate,
                v.creator,
                v.identifier,
                v.runtime,
                v.totalTracks,
                isFavourite(v.identifier)
            )
        }
    }

    /**
     * Get book from search query
     */
    suspend fun getQueryBooks(query: String): List<BookParcel>? {
        val queryValue = String.format(URL_SEARCH_FORMAT, query)
        val fetchResult = NetworkServiceSearch().searchBooksService.getQueryBooks(queryValue)
        Log.d(TAG, "Raw data received: $fetchResult")
        val result = fetchResult.channel?.items?.filterNot {
            it.link.isBlank()
            it.guid.isBlank()
        }

        return result?.map { v ->
            BookParcel(
                v.guid,
                v.title,
                v.description,
                v.link,
                v.pubDate,
                v.creator,
                v.identifier,
                v.runtime,
                v.totalTracks,
                isFavourite(v.identifier)
            )
        }
    }

    suspend fun getAudioBook(identifier: String): Audio? {
                val result = NetworkServiceAudio().audioService.getAudioBook(identifier)
//                Log.d(TAG, ".getBook call result: $result")
        return run {
            val resutValue = result.apply {
                mediaFiles = mediaFiles.filter {
                    it.format == "64Kbps MP3"
                }.toMutableList()
                mediaFiles.map{ it.playbackState = AudioState().IDLE}
            }
            resutValue
        }
    }

    companion object {
        private var INSTANCE: BookRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = BookRepository(context)
            }
        }

        fun get(): BookRepository {
//            Log.d(TAG, ".get called $INSTANCE")
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be init")
        }
    }

}

