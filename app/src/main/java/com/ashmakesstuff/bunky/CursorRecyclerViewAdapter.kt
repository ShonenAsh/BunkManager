package com.ashmakesstuff.bunky

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.ashmakesstuff.bunky.data.CourseContract
import kotlinx.android.synthetic.main.list_item.view.*
import java.math.RoundingMode
import java.text.DecimalFormat

class CursorRecyclerViewAdapter(context: Context, cursor: Cursor?) : AbstractCursorRecyclerViewAdapter<CursorRecyclerViewAdapter.ViewHolder>(context, cursor) {

    private val mContext: Context = context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var mCourseNameTextView: TextView = view.course_name_text_view
        var mBunksTextView: TextView = view.number_of_bunk_text_view
        var mSafeBunks: TextView = view.safe_bunks_text_view
        var mPercentTextView: TextView = view.percent_text_view
        var mCardView: CardView = view.list_item_card_view
        var mButtonAttend: Button = view.button_attend
        var mButtonBunk: Button = view.button_bunk

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, cursor: Cursor, position: Int) {
        // Calculate the percentage
        fun calculatePercentage(attended: Int, conducted: Int): String {
            val df = DecimalFormat("#")
            df.roundingMode = RoundingMode.CEILING
            return df.format((attended * 100) / conducted).toString()
        }

        fun getBunkStatus(attended: Int, conducted: Int): String {
            return if (((attended * 100).toDouble() / conducted) <= 75.0) {
                viewHolder.mSafeBunks.text = mContext.getString(R.string.required_classes)
                val required: Int = ((0.75 * conducted - attended) / 0.25).toInt()
                required.toString()     // return statement lifted out of if block
            } else {
                viewHolder.mSafeBunks.text = mContext.getString(R.string.safe_bunks)
                val bunks: Int = ((attended - 0.75 * conducted) / 0.75).toInt()
                bunks.toString()        // return statement lifted out of if block
            }
        }

        fun incrementButtons(uri: Uri, buttonType: Int) {
            val contentResolver: ContentResolver? = mContext.contentResolver

            val projection = arrayOf(CourseContract.CourseEntry._ID,
                    CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED,
                    CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED)

            val buttonCursor: Cursor? = contentResolver!!.query(uri, projection, null, null, null)

            buttonCursor.use {
                if (buttonCursor!!.moveToFirst()) {
                    val values = ContentValues()
                    val conducted = buttonCursor.getInt(buttonCursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED))

                    when (buttonType) {
                        CourseContract.CourseEntry.BUNK_BUTTON -> {
                            val bunked = buttonCursor.getInt(buttonCursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED))
                            values.put(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED, bunked + 1)
                            values.put(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED, conducted + 1)
                            contentResolver.update(uri, values, null, null)
                        }

                        CourseContract.CourseEntry.ATTEND_BUTTON -> {
                            values.put(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED, conducted + 1)
                            contentResolver.update(uri, values, null, null)
                        }
                        else -> throw IllegalArgumentException("Accessing wrong button type")
                    }
                }
            }
        }


        val name = cursor.getString(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_NAME))
        val bunks = cursor.getInt(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED))
        val conducted = cursor.getInt(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED))
        viewHolder.mCourseNameTextView.text = name
        viewHolder.mBunksTextView.text = getBunkStatus(conducted - bunks, conducted)
        viewHolder.mPercentTextView.text = calculatePercentage(conducted - bunks, conducted)

        val itemId: Long = getItemId(position)

        // Form the content URI that represents the specific course that was clicked on,
        // by appending the "id" (passed as input to this method) onto the
        // {@link CourseEntry#CONTENT_URI}.
        // for example, the URI would be "content://com.ashmakesstuff.bunky/pets/2
        // for the pet with ID 2 when clicked.
        val currentCourseUri = ContentUris.withAppendedId(CourseContract.CourseEntry.CONTENT_URI, itemId)

        viewHolder.mCardView.setOnClickListener {

            Toast.makeText(mContext, itemId.toString(), Toast.LENGTH_SHORT).show()

            val intent = Intent(mContext, EditorActivity::class.java)

            // Set the URI on the data field of the intent
            intent.data = currentCourseUri

            // Launch the {@link EditorActivity} to display the data for the current course.
            mContext.startActivity(intent)
        }

        viewHolder.mButtonAttend.setOnClickListener {
            incrementButtons(currentCourseUri, CourseContract.CourseEntry.ATTEND_BUTTON)
        }

        viewHolder.mButtonBunk.setOnClickListener {
            incrementButtons(currentCourseUri, CourseContract.CourseEntry.BUNK_BUTTON)
        }

    }
}