package com.ashmakesstuff.bunky.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CourseDbHelper
(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        // Name of the DataBase file
        private const val DATABASE_NAME = "bunky.db"
        // DataBase Version number
        private const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create a String containing the SQL statement to create the courses table
        val SQL_CREATE_COURSES_TABLE = ("CREATE TABLE " + CourseContract.CourseEntry.TABLE_NAME + " ("
                + CourseContract.CourseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CourseContract.CourseEntry.COLUMN_COURSE_NAME + " TEXT NOT NULL, "
                + CourseContract.CourseEntry.COLUMN_LAST_UPDATED + " INTEGER NOT NULL DEFAULT 0, "
                + CourseContract.CourseEntry.COLUMN_CLASSROOM + " TEXT, "
                + CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED + " INTEGER NOT NULL DEFAULT 0, "
                + CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED + " INTEGER NOT NULL DEFAULT 1);")

        // Execute the SQL statements
        db.execSQL(SQL_CREATE_COURSES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Nothing here yet
    }
}

