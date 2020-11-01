package com.katevu.voxaudiobooks.ui

/**
 * Author: Kate Vu
 */
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.BookParcel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BookListFragment.Callbacks,
    BookFavouriteFragment.Callbacks {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val isFragmentContainerEmpty = savedInstanceState == null
        val firstFragment = BookListFragment.newInstance()
        val secondFragment = BookFavouriteFragment.newInstance()

        if (isFragmentContainerEmpty) {
//            Log.d(TAG, "add PhotoGalleryFragment")
            //val fragment = BookListFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, firstFragment)
                .commit()
            //setCurrentFragment(fragment)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.books -> setCurrentFragment(firstFragment)
                R.id.favourite -> setCurrentFragment(secondFragment)
            }
            true
        }

    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            //addToBackStack(null)
            commit()
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