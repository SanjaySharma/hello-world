package com.google.developer.taskmaker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.developer.taskmaker.data.DatabaseContract;
import com.google.developer.taskmaker.data.TaskUpdateService;
import com.google.developer.taskmaker.reminders.AlarmScheduler;
import com.google.developer.taskmaker.views.DatePickerFragment;
import com.google.developer.taskmaker.views.TaskTitleView;

import java.util.Calendar;
import java.util.Date;

public class TaskDetailActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener,LoaderManager.LoaderCallbacks<Cursor> {

    private TaskTitleView mNameView;
    private TextView mDateView;
    private ImageView mPriorityView;
    private Uri mTaskUri ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_details);
        //Task must be passed to this activity as a valid provider Uri
        mTaskUri = getIntent().getData();

        mNameView = (TaskTitleView)findViewById(R.id.text_description);
        mDateView = (TextView) findViewById(R.id.text_date);
        mPriorityView = (ImageView) findViewById(R.id.priority);
        mDateView.setVisibility(View.VISIBLE);


        getSupportLoaderManager().initLoader(2, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_delete:
                TaskUpdateService.deleteTask(TaskDetailActivity.this,mTaskUri);
                 finish();
                break;
            case R.id.action_reminder:
                DatePickerFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "datePicker");
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date d =  cal.getTime();

        AlarmScheduler.scheduleAlarm(TaskDetailActivity.this,d.getTime(),mTaskUri);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cursorLoader = new CursorLoader(this, mTaskUri, null,
                null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        if(null != cursor && cursor.getCount() >0 ) {
            cursor.moveToFirst();

            mNameView.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.TaskColumns.DESCRIPTION)));

            int priority = cursor.getInt(cursor.getColumnIndex(DatabaseContract.TaskColumns.IS_PRIORITY));

            mPriorityView.setImageResource((priority == 0 ? R.drawable.ic_not_priority : R.drawable.ic_priority));


            long dueDate = cursor.getLong(cursor.getColumnIndex(DatabaseContract.TaskColumns.DUE_DATE));
            mDateView.setText(getResources().getString(R.string.task_date) + "   " + (dueDate != Long.MAX_VALUE ? DateUtils.getRelativeTimeSpanString(dueDate) : getResources().getString(R.string.date_empty)));

            int isComplete = cursor.getInt(cursor.getColumnIndex(DatabaseContract.TaskColumns.IS_COMPLETE));

            if(dueDate != Long.MAX_VALUE && dueDate < System.currentTimeMillis()){
                mNameView.setState(TaskTitleView.OVERDUE);
            }else{
                mNameView.setState((isComplete == 1 ? TaskTitleView.DONE : TaskTitleView.NORMAL));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }

}
