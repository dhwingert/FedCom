package com.dhwingert.fedcom.util;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.dhwingert.fedcom.database.BaseInfo;

import java.util.List;

/**
 * Overrides ArrayAdapter's hasStableIds to return true.
 *
 * This means that a given item in the list will have the same id even after the underlying data
 * has been updated.  In ArrayAdapter, that isn't guaranteed.
 */
public class StableArrayAdapter extends ArrayAdapter {

    public static final int INVALID_ID = -1;

    boolean mDuringUpdate;

    public StableArrayAdapter(Context context, int textViewResourceId, List<? extends BaseInfo> objects) {
        super(context, textViewResourceId, objects);
        mDuringUpdate = false;
    }

    @Override
    public long getItemId(int position) {
        int itemId = INVALID_ID;

        if (position >= 0 && position < getCount()) {
            BaseInfo item = (BaseInfo) getItem(position);
            if (item == null) {
                return INVALID_ID;
            } else {
                return item.getId();
            }
        }

        return itemId;
    }

    @Override
    public void notifyDataSetChanged () {
        mDuringUpdate = false;
        super.notifyDataSetChanged();
    }

    // Indicate if this lists data is being updated
    //      If it is, isEmpty() will return False for the time being
    //      to prevent the "Empty" list indicator from flashing on the screen.
    public void doingUpdate(boolean doingUpdate) {
        mDuringUpdate = doingUpdate;
    }

    @Override
    public boolean isEmpty() {
        // During an update always return False for isEmpty()
        //      This prevents the "Empty" indicator from flashing on screen
        //      while the contents of this list are being updated.
        if (mDuringUpdate) {
            return false;
        } else {
            return super.isEmpty();
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
