package com.ashmakesstuff.bunky

import android.app.LoaderManager
import android.content.ContentValues
import android.content.CursorLoader
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.ashmakesstuff.bunky.data.CourseContract


class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private val COURSE_LOADER = 0
    // If the app crashes, make this var ? (nullable)
    private lateinit var mRecyclerView: RecyclerView
    private val mAdapter = CursorRecyclerViewAdapter(this, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup FAB to open EditorActivity
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, EditorActivity::class.java)
            startActivity(intent)
        }

        val mLayoutManager = LinearLayoutManager(this)
        mRecyclerView = findViewById(R.id.RecyclerView)
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.adapter = mAdapter


        // Start the loader
        loaderManager.initLoader(COURSE_LOADER, null, this)
    }

    private fun insertCourse() {
        // Create a ContentValue object where column names are the keys.
        // and DSA's per attributes are the values.
        val values = ContentValues()
        values.put(CourseContract.CourseEntry.COLUMN_COURSE_NAME, "Data Structures and Algorithms")
        values.put(CourseContract.CourseEntry.COLUMN_COURSE_CREDITS, 5.0)
        values.put(CourseContract.CourseEntry.COLUMN_CLASSROOM, "LHC103")
        values.put(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED, 1)
        values.put(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED, 3)
        values.put(CourseContract.CourseEntry.COLUMN_INSTRUCTOR, "ABC")


        // Insert a new row for Toto in the database, returning the ID of thar new row.

        val newUri = contentResolver.insert(CourseContract.CourseEntry.CONTENT_URI, values)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        when (item?.itemId) {
            // Respond to a click on the "Insert dummy data" menu option
            R.id.action_insert_dummy_data -> {
                insertCourse()
                return true
            }
            // Respond to a click on the "Delete all entries" menu option
            R.id.action_delete_all_entries ->
                // Do nothing for now
                return true
        }
        return true
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): android.content.Loader<Cursor> {
        // Define a projection that specifies the columns from the table we care about.
        val projection = arrayOf(CourseContract.CourseEntry._ID,
                CourseContract.CourseEntry.COLUMN_COURSE_NAME,
                CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED,
                CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED)

        // This loader will execute the ContentProvider's query method on a background thread
        return CursorLoader(this,
                CourseContract.CourseEntry.CONTENT_URI,
                projection,
                null, null, null)
    }

    override fun onLoadFinished(loader: android.content.Loader<Cursor>, data: Cursor) {
        // Update {@link CursorRecyclerViewAdapter} with this new cursor containing updated course data
        mAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: android.content.Loader<Cursor>?) {
        mAdapter.swapCursor(null)
    }
}
