package com.katevu.voxaudiobooks.ui

/**
 * Author: Kate Vu
 */
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemClickListener(
    context: Context?,
    recyclerView: RecyclerView,
    private val listener: OnRecyclerClickListener
) : RecyclerView.SimpleOnItemTouchListener() {
    private val TAG = "RecyclerItemClickLis"

    interface OnRecyclerClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    private val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
//            Log.d(TAG, "onSingleTapUp called")
                val childView = recyclerView.findChildViewUnder(e.x, e.y)
//            Log.d(TAG,"onSingleTapUp calling lister onItemClick")
                if (childView != null) {
                    listener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView))
                }
                return true
            }
        })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//        Log.d(TAG, "onInterceptTouchEvent called start: $e")
        var result = gestureDetector.onTouchEvent(e)
//        Log.d(TAG, "onInterceptTouchEvent returning: $result")
//        return super.onInterceptTouchEvent(rv, e)
        return result
    }
}