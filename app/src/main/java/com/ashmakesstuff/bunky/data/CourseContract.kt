package com.ashmakesstuff.bunky.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns


object CourseContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    const val CONTENT_AUTHORITY = "com.ashmakesstuff.bunky"

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    val BASE_CONTENT_URI = Uri.parse("content://$CONTENT_AUTHORITY")!!

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.ashmakesstuff.testbunky/courses/ is a valid path for
     * looking at course data. content://com.ashmakesstuff.testbunky/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    const val PATH_COURSES = "courses"

    /**
     * Inner class that defines constant values for the courses database table.
     * Each entry in the table represents a single course.
     */
    class CourseEntry : BaseColumns {
        companion object {

            // Complete Content Uri
            val CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_COURSES)!!

            /**
             * The MIME type of the [.CONTENT_URI] for a list of courses.
             */
            val CONTENT_LIST_TYPE =
                    ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSES


            //The MIME type of the {@link #CONTENT_URI} for a single course.

            val CONTENT_ITEM_TYPE =
                    ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSES


            // Name of the database table for courses
            const val TABLE_NAME = "courses"

            // Unique Id for the courses : INTEGER
            const val _ID = BaseColumns._ID

            // Name of the course : TEXT
            const val COLUMN_COURSE_NAME = "name"

            //  The classroom the course is conducted in : TEXT
            const val COLUMN_CLASSROOM = "classroom"

            //  Name of the course instructor : TEXT
            const val COLUMN_INSTRUCTOR = "instructor"

            // Credits for the course : REAL
            const val COLUMN_COURSE_CREDITS = "credits"

            // Number of Classes attended : INTEGER
            const val COLUMN_CLASSES_BUNKED = "bunked"

            // Number of Classes conducted : INTEGER
            const val COLUMN_CLASSES_CONDUCTED = "conducted"

            // ID for Bunk Button
            const val BUNK_BUTTON =0

            // ID for Attend Button
            const val  ATTEND_BUTTON=1

        }
    }
}
