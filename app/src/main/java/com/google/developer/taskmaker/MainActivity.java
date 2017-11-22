package com.google.developer.taskmaker;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.developer.taskmaker.data.DatabaseContract;
import com.google.developer.taskmaker.data.TaskAdapter;
import com.google.developer.taskmaker.data.TaskUpdateService;

public class MainActivity extends AppCompatActivity implements
        TaskAdapter.OnItemClickListener,
        View.OnClickListener,LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private TaskAdapter mAdapter;

    private SharedPreferences mPrefs;
    private Cursor mCursor ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAdapter = new TaskAdapter(null);
        mAdapter.setOnItemClickListener(this);

         findViewById(R.id.fab).setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//Loads Shared preferences
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        getSupportLoaderManager().initLoader(1, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Click events in Floating Action Button */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivity(intent);
    }

    /* Click events in RecyclerView items */
    @Override
    public void onItemClick(View v, int position) {

        mCursor.moveToPosition(position);
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.setData(Uri.withAppendedPath(DatabaseContract.CONTENT_URI,mCursor.getString(mCursor.getColumnIndex(DatabaseContract.TaskColumns._ID))));
        startActivity(intent);

    }

    /* Click events on RecyclerView item checkboxes */
    @Override
    public void onItemToggled(boolean active, int position) {
        //TODO: Handle task item checkbox event
        ContentValues values = new ContentValues(2);
        mCursor.moveToPosition(position);
        if (active)
        {
            values.put(DatabaseContract.TaskColumns.IS_COMPLETE, 1);
        }
        else {
            values.put(DatabaseContract.TaskColumns.IS_COMPLETE, 0);
        }

        values.put(DatabaseContract.TaskColumns._ID,mCursor.getString(mCursor.getColumnIndex(DatabaseContract.TaskColumns._ID)));
        TaskUpdateService.updateTask(MainActivity.this,Uri.withAppendedPath(DatabaseContract.CONTENT_URI,mCursor.getString(mCursor.getColumnIndex(DatabaseContract.TaskColumns._ID))),values);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Uri CONTACT_URI = DatabaseContract.CONTENT_URI;
        CursorLoader cursorLoader = new CursorLoader(this, CONTACT_URI, null,
                null, null, loadPref());
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

        mCursor = cursor;
        cursor.moveToFirst();
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }


    private String loadPref() {


        String myListPreference = mPrefs.getString(getString(R.string.pref_sortBy_key),
                getString(R.string.pref_sortBy_default_label));

        if (myListPreference.equalsIgnoreCase(getString(R.string.pref_sortBy_default_label))) {
            return DatabaseContract.DEFAULT_SORT;
        } else {
            return DatabaseContract.DATE_SORT;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sortBy_key))) {
            getSupportLoaderManager().restartLoader(1, null, this);
        }
    }
}
