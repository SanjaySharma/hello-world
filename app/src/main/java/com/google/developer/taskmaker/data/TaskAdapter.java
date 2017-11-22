package com.google.developer.taskmaker.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.developer.taskmaker.R;
import com.google.developer.taskmaker.views.TaskTitleView;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    /* Callback for list item click events */
    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        void onItemToggled(boolean active, int position);
    }

    /* ViewHolder for each task item */
    public class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TaskTitleView nameView;
        public TextView dateView;
        public ImageView priorityView;
        public CheckBox checkBox;

        public TaskHolder(View itemView) {
            super(itemView);

            nameView = (TaskTitleView) itemView.findViewById(R.id.text_description);
            dateView = (TextView) itemView.findViewById(R.id.text_date);
            priorityView = (ImageView) itemView.findViewById(R.id.priority);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(this);
            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == checkBox) {
                completionToggled(this);
            } else {
                postItemClick(this);
            }
        }
    }

    private Cursor mCursor;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    public TaskAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void completionToggled(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemToggled(holder.checkBox.isChecked(), holder.getAdapterPosition());
        }
    }

    private void postItemClick(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
        }
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_item_task, parent, false);

        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {

        mCursor.moveToPosition(position);



        int priority = mCursor.getInt(mCursor.getColumnIndex(DatabaseContract.TaskColumns.IS_PRIORITY));

        holder.priorityView.setImageResource((priority == 0 ? R.drawable.ic_not_priority : R.drawable.ic_priority));

        int isComplete = mCursor.getInt(mCursor.getColumnIndex(DatabaseContract.TaskColumns.IS_COMPLETE));

        holder.checkBox.setChecked((isComplete == 1 ? true : false));

        holder.nameView.setText(mCursor.getString(mCursor.getColumnIndex(DatabaseContract.TaskColumns.DESCRIPTION)));

        long dueDate = mCursor.getLong(mCursor.getColumnIndex(DatabaseContract.TaskColumns.DUE_DATE));
        holder.dateView.setText((dueDate != Long.MAX_VALUE ? DateUtils.getRelativeTimeSpanString(dueDate) : ""));

                if(dueDate != Long.MAX_VALUE && dueDate < System.currentTimeMillis()){
                    holder.nameView.setState(TaskTitleView.OVERDUE);

                    Log.e("Name "+mCursor.getString(mCursor.getColumnIndex(DatabaseContract.TaskColumns.DESCRIPTION))
                            ," OVERDUE ");
                }else{
                    holder.nameView.setState((isComplete == 1 ? TaskTitleView.DONE : TaskTitleView.NORMAL));
                }

    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    /**
     * Retrieve a {@link Task} for the data at the given position.
     *
     * @param position Adapter item position.
     *
     * @return A new {@link Task} filled with the position's attributes.
     */
    public Task getItem(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid item position requested");
        }

        return new Task(mCursor);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
