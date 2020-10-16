package com.katevu.voxaudiobooks.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.BookParcel

class MainActivity : AppCompatActivity(), BookListFragment.Callbacks {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty) {
            Log.d(TAG, "add PhotoGalleryFragment")
            val fragment = BookListFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }

    }

    override fun onBookSelected(bookParcel: BookParcel) {
        val fragment = BookDetailsFragment.newInstance(bookParcel)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}