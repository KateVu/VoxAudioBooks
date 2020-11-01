package com.katevu.voxaudiobooks.databases

/**
 * Author: Kate Vu
 */
import androidx.lifecycle.LiveData
import androidx.room.*
import com.katevu.voxaudiobooks.models.BookParcel

/**
 * The Data Access Object for the Plant class.
 */
@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY identifier")
    fun getBooksDB(): LiveData<List<BookParcel>>

    @Query("SELECT * FROM books WHERE identifier = :identifier")
    suspend fun getBookDB(identifier: String): BookParcel?

    @Query("SELECT * FROM books ORDER BY identifier")
    suspend fun getBooksFavourite(): List<BookParcel>


    @Query("SELECT * FROM books WHERE identifier = :identifier")
    fun getBookDB2(identifier: String): LiveData<BookParcel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(books: List<BookParcel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addBook(book: BookParcel)

    @Delete
    fun deleteBook(book: BookParcel)

}