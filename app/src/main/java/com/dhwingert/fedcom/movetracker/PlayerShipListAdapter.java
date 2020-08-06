package com.dhwingert.fedcom.movetracker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.dhwingert.fedcom.database.BaseInfo;
import com.dhwingert.fedcom.util.DragNDropAdapter;
import com.dhwingert.fedcom.util.DragNDropListView;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.R;
import com.dhwingert.fedcom.database.ShipInfo;
import com.dhwingert.fedcom.util.StableArrayAdapter;

import java.util.List;

/**
 * Adapter for ship ListView in PlayerDetailsFragment.
 *
 * This supports the custom row views in the ship list.
 *
 * Created by David Wingert on 12/30/2014.
 */
public class PlayerShipListAdapter extends StableArrayAdapter implements DragNDropAdapter {

    // Callback methods implemented by PlayerDetailsFragment
    public interface PlayerShipListAdapterListener {
        public void onShipDeleted();
    }

    private PlayerShipListAdapterListener mListener = null;

    int mPosition[];
    int mHandler;

    private GameInfo mGameInfo;

    public void setGameInfo(GameInfo gameInfo) {
        mGameInfo = gameInfo;
    }

    public void addPlayerShipListAdapterListener(PlayerShipListAdapterListener listener) {
        mListener = listener;
    }

    public void removePlayerShipListAdapterListener() {
        mListener = null;
    }

    public PlayerShipListAdapter(Context context, List<ShipInfo> shipInfoList, int handler) {
        super(context, 0, shipInfoList);

        mHandler = handler;
        setup(shipInfoList.size());
    }

    private void setup(int size) {
        mPosition = new int[size];

        for (int i = 0; i < size; ++i) mPosition[i] = i;
    }

    @Override
    public void notifyDataSetChanged () {
        setup(getCount());
        super.notifyDataSetChanged();
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup group) {
        return super.getDropDownView(mPosition[position], view, group);
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(mPosition[position]);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(mPosition[position]);
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < getCount()) {
            return super.getItemId(mPosition[position]);
        } else {
            return INVALID_ID;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlayerShipRowView playerShipRowView = (PlayerShipRowView)convertView;

        if (null == playerShipRowView) {
            playerShipRowView = PlayerShipRowView.inflate(parent);
        }
        playerShipRowView.setItem( (ShipInfo)getItem(position), mGameInfo );

        ImageButton shipDeleteButton = (ImageButton) playerShipRowView.findViewById(R.id.deleteUnitButton);
        shipDeleteButton.setOnClickListener(mOnDeleteButtonClickListener);
        shipDeleteButton.setTag(position);

        return playerShipRowView;
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(mPosition[position]);
    }

    @Override
    public int getDragHandler() {
        return mHandler;
    }

    @Override
    public void onItemDrag(DragNDropListView parent, View view, int position, long id) {

    }

    @Override
    public void onRowClicked(View view, BaseInfo baseInfo) {

    }

    @Override
    public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
        int position = mPosition[startPosition];

        if (startPosition < endPosition)
            for(int i = startPosition; i < endPosition; ++i)
                mPosition[i] = mPosition[i + 1];
        else if (endPosition < startPosition)
            for(int i = startPosition; i > endPosition; --i)
                mPosition[i] = mPosition[i - 1];

        mPosition[endPosition] = position;
    }

    // When the user clicks the delete button on a ship row in the ship list in the PlayerDetailsFragment
    // Go ahead and delete that row
    View.OnClickListener mOnDeleteButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View shipRowDeleteButton) {
            shipRowDeleteButton.setVisibility(View.INVISIBLE);
            final int position = (Integer) shipRowDeleteButton.getTag();
            removeSpecifiedRow(position);
        }
    };

    private void removeSpecifiedRow(int position) {
        this.remove(this.getItem(position));
        this.notifyDataSetChanged();

        if (mListener != null) {
            mListener.onShipDeleted();
        }
    }

}
