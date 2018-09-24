package com.ashmakesstuff.bunky.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log


class CourseProvider : ContentProvider() {

    // Database helper object
    private lateinit var mDbHelper: CourseDbHelper

    companion object {

        // Tag for the log message
        val LOG_TAG = CourseProvider::class.java.simpleName!!

        // Initialize the provider and the database helper object.

        private const val COURSES = 100
        private const val COURSE_ID = 101

        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            sUriMatcher.addURI(CourseContract.CONTENT_AUTHORITY, CourseContract.PATH_COURSES, COURSES)

            sUriMatcher.addURI(CourseContract.CONTENT_AUTHORITY, CourseContract.PATH_COURSES + "/#", COURSE_ID)
        }
    }

    override fun onCreate(): Boolean {
        mDbHelper = CourseDbHelper(context)
        return true
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var selection = selection
        var selectionArgs = selectionArgs
        // Get readable database
        val database = mDbHelper.readableDatabase

        val cursor: Cursor
        val match = sUriMatcher.match(uri)

        when (match) {
            COURSES -> cursor = database.query(CourseContract.CourseEntry.TABLE_NAME, projection, selection,
                    selectionArgs, null, null, sortOrder)

            COURSE_ID -> {
                selection = CourseContract.CourseEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of he table.
                cursor = database.query(CourseContract.CourseEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            }

            else -> throw IllegalArgumentException(" Cannot query unknown URI $uri")
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor
        cursor.setNotificationUri(context!!.contentResolver, uri)

        return cursor
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    override fun getType(uri: Uri): String? {
        val match = sUriMatcher.match(uri)
        when (match) {
            COURSES -> return CourseContract.CourseEntry.CONTENT_LIST_TYPE
            COURSE_ID -> return CourseContract.CourseEntry.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown URI " + " with " +
                    "match" + match)
        }
    }


    /**
     * Insert new data into the provider with the given ContentValues.
     */
    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val match = sUriMatcher.match(uri)
        when (match) {
            COURSES -> return insertCourse(uri, contentValues!!)
            else -> throw IllegalArgumentException("Insertion is not supported for $uri")
        }
    }

    // Insert pet helper method
    private fun insertCourse(uri: Uri, values: ContentValues): Uri? {
        // Check that the name is not null
        val name = values.getAsString(CourseContract.CourseEntry.COLUMN_COURSE_NAME)
                ?: throw IllegalArgumentException("Course requires a name")

        // Check if the conducted is not null
        val conducted = values.getAsInteger(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED)
                ?: throw IllegalArgumentException("Number of classes CONDUCTED cannot be null")

        if (conducted < 1)
            throw IllegalArgumentException("Classes CONDUCTED must be more than 0")

        val bunked = values.getAsInteger(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED)
                ?: throw IllegalArgumentException("Number of classes BUNKED cannot be null")

        if (bunked < 0)
            throw IllegalArgumentException("Classes BUNKED must be positive")


        // No need to check for classroom, credits, instructor any value is valid (including null)

        // Get writable database
        val database = mDbHelper.writableDatabase
        // Insert a new pet with the given values
        val id = database.insert(CourseContract.CourseEntry.TABLE_NAME, null, values)
        // if the id is -1 , insertion failed
        if (id.equals(-1)) {
            Log.e(LOG_TAG, "Failed to insert row for $uri")
            return null
        }

        // Notify all listeners that the data has changed for the pet content URI
        context!!.contentResolver.notifyChange(uri, null)

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id)
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var selection = selection
        var selectionArgs = selectionArgs
        val database = mDbHelper.writableDatabase

        // Track the number of rows deleted
        val rowsDeleted: Int

        val match = sUriMatcher.match(uri)
        when (match) {
            COURSES -> rowsDeleted = database.delete(CourseContract.CourseEntry.TABLE_NAME, selection, selectionArgs)
            COURSE_ID -> {
                // Deleting a single pet
                selection = CourseContract.CourseEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                rowsDeleted = database.delete(CourseContract.CourseEntry.TABLE_NAME, selection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Deletion is not supported for $uri")
        }

        if (rowsDeleted != 0)
            context!!.contentResolver.notifyChange(uri, null)

        return rowsDeleted
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    override fun update(uri: Uri, contentValues: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        var selection = selection
        var selectionArgs = selectionArgs
        val match = sUriMatcher.match(uri)
        when (match) {
            COURSES -> return updateCourse(uri, contentValues!!, selection, selectionArgs)
            COURSE_ID -> {
                // For the COURSE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = CourseContract.CourseEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                return updateCourse(uri, contentValues!!, selection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Update is not supported for $uri")
        }
    }

    private fun updateCourse(uri: Uri, values: ContentValues, selection: String?, selectionArgs: Array<String>?): Int {
        // If the {@link CourseEntry#COLUMN_COURSE_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(CourseContract.CourseEntry.COLUMN_COURSE_NAME)) {
            val name = values.getAsString(CourseContract.CourseEntry.COLUMN_COURSE_NAME)
                    ?: throw IllegalArgumentException("Course requires a name")
        }

        // If the {@link CourseEntry#COLUMN_CLASSES_CONDUCTED} key is present,
        // check that the number of classes conducted is valid
        if (values.containsKey(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED)) {
            val conducted = values.getAsInteger(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED)
                    ?: throw IllegalArgumentException("Number of classes CONDUCTED cannot be null")
            if (conducted < 1)
                throw IllegalArgumentException("Number of classes CONDUCTED must be more than 0")
        }

        // If the {@link CourseEntry#COLUMN_CLASSES_BUNKED} key is present,
        // check that number of bunked value is valid
        if (values.containsKey(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED)) {
            // Check that the weight is greater than or equal to 0 kg
            val bunked = values.getAsInteger(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED)
                    ?: throw IllegalArgumentException("Number of classes BUNKED cannot be null")
            if (bunked < 0)
                throw IllegalArgumentException("Number of classes BUNKED must be positive or zero")

        }

        // No need to check for classroom, credits, instructor any value is valid (including null)
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0
        }

        // Otherwise, get writable database to update the data
        val database = mDbHelper.writableDatabase

        // Returns the number of database rows affected by the update statement
        val rowsUpdated = database.update(CourseContract.CourseEntry.TABLE_NAME, values, selection, selectionArgs)

        // If more than 0 rows are updated, then notify all the listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }

        return rowsUpdated
    }
}
