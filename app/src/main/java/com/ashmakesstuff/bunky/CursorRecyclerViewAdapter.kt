package com.ashmakesstuff.bunky

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.ashmakesstuff.bunky.data.CourseContract
import kotlinx.android.synthetic.main.list_item.view.*
import java.math.RoundingMode
import java.text.DecimalFormat

class CursorRecyclerViewAdapter(context: Context, cursor: Cursor?) : AbstractCursorRecyclerViewAdapter<CursorRecyclerViewAdapter.ViewHolder>(context, cursor) {

    private lateinit var mContext: Context
    private val mBunkDrawableWhite = R.drawable.ic_bunk_white_24dp
    private val mTimeDrawableWhite = R.drawable.ic_access_time_white_18dp
    private val mAttendDrawableWhite = R.drawable.ic_attend_white_24dp
    private val mBunkDrawableBlack = R.drawable.ic_bunk_black_24dp
    private val mAttendDrawableBlack = R.drawable.ic_attend_black_24dp
    private val mTimeDrawableBlack = R.drawable.ic_access_time_black_18dp
    private val mPrefsName = "MyPrefsFile"
    private var mMinimumAttendance = 75.0f

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var mCourseNameTextView: TextView = view.course_name_text_view
        var mBunksTextView: TextView = view.number_of_bunk_text_view
        var mSafeBunks: TextView = view.safe_bunks_text_view
        var mPercentTextView: TextView = view.percent_text_view
        var mCardView: CardView = view.list_item_card_view
        var mButtonAttend: Button = view.button_attend
        var mButtonBunk: Button = view.button_bunk
        var mPercentSymbol: TextView = view.percent_symbol_text_view
        var mLastUpdated: TextView = view.update_text_view
        var mTimeIcon: ImageView = view.ic_time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        mContext = parent.context
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, cursor: Cursor, position: Int) {
        val settings = mContext.getSharedPreferences(mPrefsName, Context.MODE_PRIVATE)
        mMinimumAttendance = settings.getFloat("minimumAttendance", 75.0f)

        val mPrimaryColor = mContext.resources.getColor(R.color.primaryTextColor)
        val mSecondaryColor = mContext.resources.getColor(R.color.secondaryTextColor)
        val mAlertColor = mContext.resources.getColor(R.color.alertColor)
        val mSafeBunksColor = mContext.resources.getColor(R.color.safeBunksColor)

        // Calculate the percentage
        fun calculatePercentage(attended: Long, conducted: Long): String {
            val df = DecimalFormat("#")
            df.roundingMode = RoundingMode.CEILING
            return df.format((attended * 100) / conducted)
        }

        // Set Card theme
        fun setCardColor(text: Int, cardColor: Int, bunkIcon: Int, attendIcon: Int, timeIcon: Int, buttonText: Int = mAlertColor, safeBunks: Int = mSafeBunksColor) {
            viewHolder.mCourseNameTextView.setTextColor(text)
            viewHolder.mPercentTextView.setTextColor(text)
            viewHolder.mPercentSymbol.setTextColor(text)
            viewHolder.mLastUpdated.setTextColor(safeBunks)

            viewHolder.mCardView.setCardBackgroundColor(cardColor)
            viewHolder.mBunksTextView.setTextColor(safeBunks); viewHolder.mSafeBunks.setTextColor(safeBunks)
            viewHolder.mButtonAttend.setTextColor(buttonText); viewHolder.mButtonBunk.setTextColor(buttonText)
            viewHolder.mButtonBunk.setCompoundDrawablesRelativeWithIntrinsicBounds(bunkIcon, 0, 0, 0)
            viewHolder.mButtonAttend.setCompoundDrawablesRelativeWithIntrinsicBounds(attendIcon, 0, 0, 0)
            viewHolder.mTimeIcon.setImageResource(timeIcon)
        }

        // Get the Bunk-ability
        fun getBunkStatus(attended: Long, conducted: Long): String {
            return if (((attended * 100).toDouble() / conducted) < mMinimumAttendance) {
                viewHolder.mSafeBunks.text = mContext.getString(R.string.required_classes)
                val required: Int = Math.ceil(((mMinimumAttendance / 100) * conducted - attended).toDouble() / (1 - (mMinimumAttendance / 100))).toInt()
                // Changing the card theme
                setCardColor(mPrimaryColor, mAlertColor, mBunkDrawableWhite, mAttendDrawableWhite, mTimeDrawableWhite, mPrimaryColor, mPrimaryColor)
                required.toString()     // return statement lifted out of if block
            } else {
                viewHolder.mSafeBunks.text = mContext.getString(R.string.safe_bunks)
                val bunks: Int = ((attended - (mMinimumAttendance / 100) * conducted) / (mMinimumAttendance / 100)).toInt()
                // Changing the card theme
                setCardColor(mSecondaryColor, mPrimaryColor, mBunkDrawableBlack, mAttendDrawableBlack, mTimeDrawableBlack)
                bunks.toString()        // return statement lifted out of if block
            }
        }

        // This function is called when the buttons are clicked
        fun incrementButtons(uri: Uri, buttonType: Int, conducted: Long, bunked: Long) {
            val contentResolver: ContentResolver? = mContext.contentResolver

            val values = ContentValues()
            values.put(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED, conducted + 1)
            when (buttonType) {
                CourseContract.CourseEntry.BUNK_BUTTON -> {
                    values.put(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED, bunked + 1)
                    contentResolver!!.update(uri, values, null, null)
                }

                CourseContract.CourseEntry.ATTEND_BUTTON -> {
                    contentResolver!!.update(uri, values, null, null)
                }
                else -> throw IllegalArgumentException("Accessing wrong button type")
            }
        }

        val name = cursor.getString(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_NAME))
        val bunked = cursor.getLong(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED))
        val conducted = cursor.getLong(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED))
        val updateTime = cursor.getLong(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_LAST_UPDATED))
        if (updateTime == 0L)
            viewHolder.mLastUpdated.text = mContext.getString(R.string.never_updated)
        else {
            var relativeTime = DateUtils.getRelativeDateTimeString(mContext, updateTime, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)

            if (relativeTime.toString()[0].equals('0'))
                viewHolder.mLastUpdated.text = mContext.getString(R.string.updated_now)
            else {
                relativeTime = "Updated $relativeTime"
                viewHolder.mLastUpdated.text = relativeTime
            }
        }

        viewHolder.mCourseNameTextView.text = name
        viewHolder.mBunksTextView.text = getBunkStatus(conducted - bunked, conducted)
        viewHolder.mPercentTextView.text = calculatePercentage(conducted - bunked, conducted)

        // Form the content URI that represents the specific course that was clicked on,
        // by appending the "id" (passed as input to this method) onto the
        // {@link CourseEntry#CONTENT_URI}.
        // for example, the URI would be "content://com.ashmakesstuff.bunky/course/2
        // for the course with ID 2 when clicked.
        val currentCourseUri = ContentUris.withAppendedId(CourseContract.CourseEntry.CONTENT_URI, getItemId(position))

        viewHolder.mCardView.setOnClickListener {
            val intent = Intent(mContext, EditorActivity::class.java)
            // Set the URI on the data field of the intent
            intent.data = currentCourseUri

            // Launch the {@link EditorActivity} to display the data for the current course.
            mContext.startActivity(intent)
        }

        //When the buttons are clicked
        viewHolder.mButtonAttend.setOnClickListener {
            incrementButtons(currentCourseUri, CourseContract.CourseEntry.ATTEND_BUTTON, conducted, bunked)
        }
        viewHolder.mButtonBunk.setOnClickListener {
            incrementButtons(currentCourseUri, CourseContract.CourseEntry.BUNK_BUTTON, conducted, bunked)
        }
    }
}