package com.ashmakesstuff.bunky

import android.app.AlertDialog
import android.app.LoaderManager
import android.content.ContentValues
import android.content.CursorLoader
import android.content.DialogInterface
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.ashmakesstuff.bunky.data.CourseContract
import kotlinx.android.synthetic.main.activity_editor.*

class EditorActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */

    // Content URI for the existing course (null if its a new Course)
    private var mCurrentCourseUri: Uri? = null

    private val EXISTING_COURSE_LOADER = 0

    /**
     *  EditText fields for the editor
     */

    private lateinit var mNameEditText: EditText
    private lateinit var mCreditEditText: EditText
    private lateinit var mClassroomEditText: EditText
    private lateinit var mBunkedEditText: EditText
    private lateinit var mConductedEditText: EditText
    private lateinit var mInstructorEditText: EditText

    // Check fo changes made by the user while editing
    private var mCourseHasChanged: Boolean = false

    private val mTouchListener = View.OnTouchListener { view, motionEvent ->
        mCourseHasChanged = true
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Examine the intent that was used to launch this activity,
        // in order to determine whether we are creating a new course or editing an existing one
        val intent = intent
        mCurrentCourseUri = intent.data
        if (mCurrentCourseUri == null) {
            // This is a new Pet
            title = "Add a Course" // Change the app bar title

            // Invalidate the options menu, so that the 'Delete' option can be hidden
            // It doesn't make sense to delete a course that hasn't been created yet
            invalidateOptionsMenu()
        } else {
            title = "Edit Course" // Change the app bar title to Edit Course

            // Initialize a loader to read the course data from the database
            // and display the current values in the editor
            loaderManager.initLoader(EXISTING_COURSE_LOADER, null, this)
        }
        // Find all the relevant views which are required to read data from the user
        mNameEditText = edit_course_name
        mCreditEditText = edit_credits
        mClassroomEditText = edit_classroom
        mBunkedEditText = edit_bunked
        mConductedEditText = edit_conducted
        mInstructorEditText = edit_instructor

        // Setup the OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // when the user tries to exit the editor without saving
        mNameEditText.setOnTouchListener(mTouchListener)
        mCreditEditText.setOnTouchListener(mTouchListener)
        mClassroomEditText.setOnTouchListener(mTouchListener)
        mBunkedEditText.setOnTouchListener(mTouchListener)
        mConductedEditText.setOnTouchListener(mTouchListener)
        mInstructorEditText.setOnTouchListener(mTouchListener)

    }

    private fun saveCourse() {
        // Read the data from the input fields
        val nameString = mNameEditText.text.toString().trim()
        val creditString = mCreditEditText.text.toString().trim()
        var classroomString = mClassroomEditText.text.toString().trim()
        val bunkedString = mBunkedEditText.text.toString().trim()
        val conductedString = mConductedEditText.text.toString().trim()
        val instructorString = mInstructorEditText.text.toString().trim()

        var bunked = 0
        var conducted = 1

        if (mCurrentCourseUri == null &&
                TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, "Please enter the course name", Toast.LENGTH_SHORT).show()
            return
        }

        // If the number of classes bunked isn't specified or is invalid, we set it to 0.
        if (!TextUtils.isEmpty(bunkedString) && Integer.parseInt(bunkedString) >= 0)
            bunked = Integer.parseInt(bunkedString)

        // If the number of classes conducted isn't specified or is invalid, we set it to 1.
        if (!TextUtils.isEmpty(conductedString) && Integer.parseInt(conductedString) > 0)
            conducted = Integer.parseInt(conductedString)

        // If the classroom isn't specified we set it to "Not Specified"
        if (TextUtils.isEmpty(classroomString))
            classroomString = "Not Specified"

        // Create a ContentValue object where column names are the keys.
        // and add the newly accepted data.
        val values = ContentValues()

        values.put(CourseContract.CourseEntry.COLUMN_COURSE_NAME, nameString)
        values.put(CourseContract.CourseEntry.COLUMN_COURSE_CREDITS, creditString)
        values.put(CourseContract.CourseEntry.COLUMN_CLASSROOM, classroomString)
        values.put(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED, bunked)
        values.put(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED, conducted)
        values.put(CourseContract.CourseEntry.COLUMN_INSTRUCTOR, instructorString)

        // An if..else block for inserting a new course or updating an existing course.
        if (mCurrentCourseUri == null) {
            // Insert a new row for the course in the database, returning the ID of that new row.

            val newUri = contentResolver.insert(CourseContract.CourseEntry.CONTENT_URI, values)

            if (newUri == null)
            // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_course_failed), Toast.LENGTH_SHORT).show()
            else
            // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_course_successful), Toast.LENGTH_SHORT).show()
        } else {
            // Otherwise this is an EXISTING course, so update the course with content URI: mCurrentCourseUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentCourseUri will already identify the correct row in the database that
            // we want to modify.
            val rowsAffected = contentResolver.update(mCurrentCourseUri, values, null, null)

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0)
            // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_course_failed), Toast.LENGTH_SHORT).show()
            else
            // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_course_successful), Toast.LENGTH_SHORT).show()

        }
    }

    private fun showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_dialog_msg)
        builder.setPositiveButton(R.string.delete) { dialog, id ->
            // User clicked the "Delete" button, so delete the course
            deleteCourse()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, id ->
            // User clicked the "Cancel" button, so dismiss the dialog
            // and continue editing the pet.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }

    // Perform the deletion of course
    private fun deleteCourse() {
        // Only perform delete if this an existing course
        if (mCurrentCourseUri != null) {
            // Call the ContentResolver to delete the course at the given content URI
            // Pass in the null selection for the selection and selection args because
            // the mCurrentCourseUri content URI already identifies the course that we want.

            val rowsDeleted = contentResolver.delete(mCurrentCourseUri, null, null)

            // Show a toast depending on whether or not the delete was successful
            if (rowsDeleted == 0)
                Toast.makeText(this, "Error deleting the course", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "Course Deleted", Toast.LENGTH_SHORT).show()
        }
        // Close the activity
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu options from the menu_editor_activity.xml file
        // This will add the menu items to the app bar
        menuInflater.inflate(R.menu.menu_editor_activity, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        // If this a new course, hide the "Delete" menu Item
        if (mCurrentCourseUri == null) {
            val menuItem = menu?.findItem(R.id.action_delete)
            menuItem?.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            // When the Save button is clicked in the menu options
            R.id.action_save -> {
                // Save the pet to the database
                saveCourse()
                // Exit activity
                finish()
                return true
            }
            // When the Delete button is clicked in the menu options
            R.id.action_delete -> {
                // Pop up the confirmation dialog for deleting the course
                showDeleteConfirmationDialog()
                return true
            }
            // When the "Up" arrow button is pressed in the app bar
            android.R.id.home -> {
                if (!mCourseHasChanged) {
                    // Navigate back to parent activity (MainActivity)
                    NavUtils.navigateUpFromSameTask(this)
                    return true
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                val discardButtonClickListener = DialogInterface.OnClickListener { dialogInterface, i ->
                    // User clicked "Discard" button, navigate to parent activity.
                    NavUtils.navigateUpFromSameTask(this@EditorActivity)
                }
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     * the user confirms they want to discard their changes
     */
    private fun showUnsavedChangesDialog(
            discardButtonClickListener: DialogInterface.OnClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.unsaved_changes_dialog_msg)
        builder.setPositiveButton(R.string.discard, discardButtonClickListener)
        builder.setNegativeButton(R.string.keep_editing) { dialog, id ->
            // User clicked the "Keep editing" button, so dismiss the dialog
            // and continue editing the course.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()

    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        // Define a projection that specifies the columns from the table we care about.
        val projection = arrayOf(CourseContract.CourseEntry._ID,
                CourseContract.CourseEntry.COLUMN_COURSE_NAME,
                CourseContract.CourseEntry.COLUMN_COURSE_CREDITS,
                CourseContract.CourseEntry.COLUMN_CLASSROOM,
                CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED,
                CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED,
                CourseContract.CourseEntry.COLUMN_INSTRUCTOR)

        // This loader will execute the ContentProvider's query method on a background thread
        return CursorLoader(this,
                mCurrentCourseUri,
                projection,
                null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, cursor: Cursor?) {
        // Exit early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.count < 1)
            return

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor) since we editing only one course
        if (cursor.moveToFirst()) {
            // Find the columns of the course attributes that we're interested in
            val nameColumnIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_NAME)
            val creditsColumnIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_CREDITS)
            val classroomColumnIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CLASSROOM)
            val bunkedColumnIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED)
            val conductedColumnIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED)
            val instructorColumnIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_INSTRUCTOR)

            // Update the views on the screen with the values from the database
            mNameEditText.setText(cursor.getString(nameColumnIndex))
            mCreditEditText.setText(cursor.getString(creditsColumnIndex))
            mClassroomEditText.setText(cursor.getString(classroomColumnIndex))
            mInstructorEditText.setText(cursor.getString(instructorColumnIndex))

            // getInt() returns an Int which is converted to String
            mBunkedEditText.setText(cursor.getInt(bunkedColumnIndex).toString())
            mConductedEditText.setText(cursor.getInt(conductedColumnIndex).toString())
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        // If the loader is invalidated, clear out all the data from the input fields
        mNameEditText.setText("")
        mCreditEditText.setText("")
        mClassroomEditText.setText("")
        mBunkedEditText.setText("")
        mConductedEditText.setText("")
        mInstructorEditText.setText("")
    }

}
