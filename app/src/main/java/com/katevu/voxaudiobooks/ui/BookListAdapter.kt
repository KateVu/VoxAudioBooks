package com.katevu.voxaudiobooks.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.Book

class BookListAdapter(var books: List<Book>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_book, parent, false)
        return BookListHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val book = books[position]
        holder as BookListHolder
        holder.bind(book)
    }

    override fun getItemCount(): Int {
        return books.size
    }


    /**
     * BookListHolder
     */
    private inner class BookListHolder(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var book: Book
        private val bookTitle = itemView.findViewById<TextView>(R.id.title)

        /**
         * Binding data
         */
        fun bind(book: Book) {
            this.book = book
            bookTitle.text = this.book.title
        }
    }

}
