package com.ashmakesstuff.bunky

import android.content.Context
import android.database.Cursor
import android.database.DataSetObserver
import android.support.v7.widget.RecyclerView

/**
 * Created by skyfishjy on 10/31/14.
 */

abstract class AbstractCursorRecyclerViewAdapter<VH : RecyclerView.ViewHolder>(private val mContext: Context, cursor: Cursor?) : RecyclerView.Adapter<VH>() {

    private var mDataValid: Boolean = false

    private var mRowIdColumn: Int = 0

    private var mCursor: Cursor? = null

    private val mDataSetObserver: DataSetObserver?

    init {
        mCursor = cursor
        mDataValid = mCursor != null
        mRowIdColumn = if (mDataValid) this.mCursor!!.getColumnIndex("_id") else -1
        mDataSetObserver = NotifyingDataSetObserver()
        if (this.mCursor != null) {
            this.mCursor!!.registerDataSetObserver(mDataSetObserver)
        }
    }

    override fun getItemCount(): Int {
        return if (mDataValid && mCursor != null) {
            mCursor!!.count
        } else 0
    }

    override fun getItemId(position: Int): Long {
        return if (mDataValid && mCursor != null && mCursor!!.moveToPosition(position)) {
            mCursor!!.getLong(mRowIdColumn)
        } else 0
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    // Modified cursor to cursor!!
    abstract fun onBindViewHolder(viewHolder: VH, cursor: Cursor, position: Int)

    override fun onBindViewHolder(viewHolder: VH, position: Int) {
        if (!mDataValid) {
            throw IllegalStateException("this should only be called when the cursor is valid")
        }
        if (!mCursor!!.moveToPosition(position)) {
            throw IllegalStateException("couldn't move cursor to position $position")
        }
        onBindViewHolder(viewHolder, mCursor!! , position)
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    fun changeCursor(cursor: Cursor) {
        val old = swapCursor(cursor)
        old?.close()
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * [.changeCursor], the returned old Cursor is *not*
     * closed.
     */
    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === mCursor) {
            return null
        }
        val oldCursor = mCursor
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver)
        }
        mCursor = newCursor
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor!!.registerDataSetObserver(mDataSetObserver)
            }
            mRowIdColumn = newCursor!!.getColumnIndexOrThrow("_id")
            mDataValid = true
            notifyDataSetChanged()
        } else {
            mRowIdColumn = -1
            mDataValid = false
            notifyDataSetChanged()
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return oldCursor
    }

    private inner class NotifyingDataSetObserver : DataSetObserver() {
        override fun onChanged() {
            super.onChanged()
            mDataValid = true
            notifyDataSetChanged()
        }

        override fun onInvalidated() {
            super.onInvalidated()
            mDataValid = false
            notifyDataSetChanged()
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}