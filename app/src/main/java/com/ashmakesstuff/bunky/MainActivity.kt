package com.ashmakesstuff.bunky

import android.app.AlertDialog
import android.app.LoaderManager
import android.content.ContentValues
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.ashmakesstuff.bunky.data.CourseContract
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private val courseLoaderID = 0
    // If the app crashes, make this var ? (nullable)
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mNoItemsImage: ImageView
    private lateinit var mNoItemsText: TextView
    private lateinit var mAdView: AdView
    private lateinit var mDrawerLayout: DrawerLayout

    private val mAdapter = CursorRecyclerViewAdapter(this@MainActivity, null)
    private val mDrawerCloseDelay: Long = 200
    private val mPrefsName = "MyPrefsFile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDrawerLayout = findViewById(R.id.drawer_layout)

        // Setup the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)
        }


        // Setup FAB to open EditorActivity
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, EditorActivity::class.java)
            startActivity(intent)
        }

        // Setting up the navigation drawer
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener {
            mDrawerLayout.closeDrawers()

            Handler().postDelayed({
                when (it.itemId) {
                    R.id.nav_about -> {
                        val intent = Intent(this@MainActivity, AboutActivity::class.java)
                        startActivity(intent)
                    }

                    R.id.nav_insert_dummy_data -> {
                        insertCourse()
                    }

                    R.id.nav_settings -> {
                        showMinimumAttendanceSettings()
                    }
                }
            }, mDrawerCloseDelay)
            true
        }

        // Check if the app is running for the first time
        val settings = this.getSharedPreferences(mPrefsName, Context.MODE_PRIVATE)

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Toast.makeText(this@MainActivity, "First time", Toast.LENGTH_SHORT).show()
            settings.getFloat("minimumAttendance", 75.0f)
            showMinimumAttendanceSettings()
            // first time task
            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).apply()
        }

        val mLayoutManager = LinearLayoutManager(this)
        mRecyclerView = findViewById(R.id.RecyclerView)
        mNoItemsImage = findViewById(R.id.emptyImageView)
        mNoItemsText = findViewById(R.id.no_course_text_view)

        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.adapter = mAdapter

        // Start the loader
        loaderManager.initLoader(courseLoaderID, null, this)

        // AdView initialize
        MobileAds.initialize(this, this.getString(R.string.ad_app_id))

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun showMinimumAttendanceSettings() {

        val settings = this.getSharedPreferences(mPrefsName, Context.MODE_PRIVATE)
        val dialogBuilder = AlertDialog.Builder(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert)

        dialogBuilder.setTitle("Minimum attendance")
        dialogBuilder.setMessage("Set your minimum attendance percentage")
        val view = layoutInflater.inflate(R.layout.dialog_box_min_attendance, null)
        dialogBuilder.setView(view)

        val minimumEditText: EditText = view.findViewById(R.id.min_attendance_edit)

        minimumEditText.setText(settings.getFloat("minimumAttendance", 75.0f).toInt().toString())

        dialogBuilder.setPositiveButton("Save") { dialog, id ->
            val min = minimumEditText.text.toString().trim()
            if (!TextUtils.isEmpty(min)) {
                if (min.toInt() in 1..99) {
                    settings.edit().putFloat("minimumAttendance", min.toFloat()).apply()
                    mAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@MainActivity, "Invalid attendance threshold", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, id ->
            dialog?.dismiss()
        }
        // Create and show the AlertDialog
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun insertCourse() {
        // Create a ContentValue object where column names are the keys.
        // and Example data are the values
        val values = ContentValues()
        values.put(CourseContract.CourseEntry.COLUMN_COURSE_NAME, "Example Course")
        values.put(CourseContract.CourseEntry.COLUMN_CLASSROOM, "Example Location")
        values.put(CourseContract.CourseEntry.COLUMN_CLASSES_BUNKED, 1)
        values.put(CourseContract.CourseEntry.COLUMN_CLASSES_CONDUCTED, 3)

        // Insert a new row for e.g. data in the database, returning the ID of that new row.
        contentResolver.insert(CourseContract.CourseEntry.CONTENT_URI, values)
    }

    private fun deleteAllCourses() {
        contentResolver.delete(CourseContract.CourseEntry.CONTENT_URI, null, null)
        Toast.makeText(this, "Deleted all courses", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setMessage(R.string.delete_all_dialog_msg)
        builder.setPositiveButton(R.string.delete) { dialog, id ->
            // User clicked the "Delete" button, so delete the course
            deleteAllCourses()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        return when (item?.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }

            // Respond to a click on the "Delete all entries" menu option
            R.id.action_delete_all_entries -> {
                showDeleteAllConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): android.content.Loader<Cursor> {
        // Define a projection that specifies the columns from the table we care about.
        val projection = arrayOf(CourseContract.CourseEntry._ID,
                CourseContract.CourseEntry.COLUMN_COURSE_NAME,
                CourseContract.CourseEntry.COLUMN_LAST_UPDATED,
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
        if (mAdapter.itemCount == 0) {
            mRecyclerView.visibility = View.GONE
            mNoItemsText.visibility = View.VISIBLE
            mNoItemsImage.visibility = View.VISIBLE
        } else {
            mNoItemsText.visibility = View.GONE
            mNoItemsImage.visibility = View.GONE
            mRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onLoaderReset(loader: android.content.Loader<Cursor>?) {
        mAdapter.swapCursor(null)
    }
}
