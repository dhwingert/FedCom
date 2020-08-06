package com.dhwingert.fedcom.combatsimulator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.dhwingert.fedcom.R;
import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.database.DatabaseInfoFactory;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.hexgridmap.HexGridMapDefinition;
import com.dhwingert.fedcom.hexgridmap.HexGridPosition;
import com.dhwingert.fedcom.movetracker.MoveTrackerMessages;
import com.dhwingert.fedcom.shipStatusDisplay.ShipSystemsDisplay;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Removed animated movement of Demon and Knight
//import java.util.Timer;
//import java.util.TimerTask;

/**
 * Navigation map showing a hex grid and move buttons.
 *
 * Created by David Wingert on 1/2/2015.
 */
public class NavigationFragment extends Fragment {

    // Callback methods implemented by MainActivity
    public interface NavigationFragmentListener {
        // Called when the user clicks the Energy Allocation button
        // Called when the user clicks the Ship Status button
        // Called when the user clicks the Enemy Status button
        public void onEnergyAllocation();
        public void onShipStatus();
        public void onEnemyStatus();
    }

    // Reference to MainActivity as a listener to this fragment
    private NavigationFragmentListener mListener;

    // Buttons to go to other views
    private Button mEnergyAllocationButton;
    private Button mShipStatusButton;
    private Button mEnemyStatusButton;

    // Movement buttons
    private Button mMoveLeftTurnButton;
    private Button mMoveLeftSlipButton;
    private Button mMoveStraightButton;
    private Button mMoveRightSlipButton;
    private Button mMoveRightTurnButton;

    // Current status labels
    private TextView mTurnLabel;
    private TextView mMoveLabel;
    private TextView mShipStatusLabel;

    private SurfaceView mHexMapSurfaceView;
    private int mBitmapWidth = 0;
    private int mBitmapHeight = 0;
    private HexGridMapDefinition mHexGridMapDef;
    private HexGridPosition mPlayerShipHexGridPosition;
    private List<HexGridPosition> mDroneHexGridPositions;

    // DHW - this should go in ShipInfo class eventually
    private int mPlayerShipTurnModeAtSpeed = 3;
    private int mPlayerShipCurrentSpeed = 24;
    private ShipSystemsDisplay mPlayerShipStatus;

    private int mGameTurn = 1;
    private int mGameImpulse = 1;
    private int mGamePhase = 1;

    private GameInfo mGameInfo;

    // Set NavigationFragmentListener when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (NavigationFragmentListener) activity;
    }

    // Remove NavigationFragmentListener when fragment is detached
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Called after View is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate GUI and get references to its controls
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

//        Toast.makeText(this,
//                       "PackageName = " + info.packageName + "\nVersionCode = "
//                               + info.versionCode + "\nVersionName = "
//                               + info.versionName + "\nPermissions = " + info.permissions, Toast.LENGTH_SHORT).show();

        // Setup Movement buttons
        mMoveLeftTurnButton = (Button) view.findViewById(R.id.moveLeftTurnButton);
        mMoveLeftSlipButton = (Button) view.findViewById(R.id.moveLeftSlipButton);
        mMoveStraightButton = (Button) view.findViewById(R.id.moveStraightButton);
        mMoveRightSlipButton = (Button) view.findViewById(R.id.moveRightSlipButton);
        mMoveRightTurnButton = (Button) view.findViewById(R.id.moveRightTurnButton);
        mMoveLeftTurnButton.setOnClickListener(onMoveLeftTurnButtonClicked);
        mMoveLeftSlipButton.setOnClickListener(onMoveLeftSlipButtonClicked);
        mMoveStraightButton.setOnClickListener(onMoveStraightButtonClicked);
        mMoveRightSlipButton.setOnClickListener(onMoveRightSlipButtonClicked);
        mMoveRightTurnButton.setOnClickListener(onMoveRightTurnButtonClicked);

        // Setup buttons to go to Energy Allocation, Ship Status, and Enemy Status fragments
        mEnergyAllocationButton = (Button) view.findViewById(R.id.energyAllocButton);
        mShipStatusButton = (Button) view.findViewById(R.id.shipStatusButton);
        mEnemyStatusButton = (Button) view.findViewById(R.id.enemyStatusButton);
        mEnergyAllocationButton.setOnClickListener(onEnergyAllocationButtonClicked);
        mShipStatusButton.setOnClickListener(onShipStatusButtonClicked);
        mEnemyStatusButton.setOnClickListener(onEnemyStatusButtonClicked);

        // Labels that show what the current turn/move is and the player ship's status
        mTurnLabel = (TextView) view.findViewById(R.id.turnLabel);
        mMoveLabel = (TextView) view.findViewById(R.id.moveLabel);
        mShipStatusLabel = (TextView) view.findViewById(R.id.shipStatusLabel);

        // Set up a listener on the LinerLayout that actually holds the Navigation Fragment
        // that is triggered once the LinearLayout's size is determined
        //      Once we have its size we want to draw a hex grid background on it
        mHexMapSurfaceView = (SurfaceView) view.findViewById(R.id.hexMapSurfaceView);
        final SurfaceView hexMapSurfaceView = (SurfaceView) view.findViewById(R.id.hexMapSurfaceView);
        hexMapSurfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                // Now we can retrieve the width and height
                mBitmapWidth = mHexMapSurfaceView.getWidth();
                mBitmapHeight = mHexMapSurfaceView.getHeight();

                // Create a hex grid map that represents the hex grid we are going to draw on the background
                mHexGridMapDef = new HexGridMapDefinition();

                // Start the Ship and Drone Icons in their initial locations
                mDroneHexGridPositions = new ArrayList<>();
                mDroneHexGridPositions.add( new HexGridPosition(4, 4, 3) );
                mDroneHexGridPositions.add( new HexGridPosition(12, 4, 3) );
                mDroneHexGridPositions.add( new HexGridPosition(4, 14, 3) );
                mDroneHexGridPositions.add( new HexGridPosition(12, 14, 3) );
                mPlayerShipHexGridPosition = new HexGridPosition(8, 12, 1);

                // Draw the initial hex grid background
                DrawHexGridBackground();

                //this is an important step not to keep receiving callbacks:
                //we should remove this listener
                //I use the function to remove it based on the api level!

                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    mHexMapSurfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mHexMapSurfaceView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        // Read in the JSON file containing the definition of a Federation Heavy Cruiser (147 pts)
        InputStream inputStream = this.getResources().openRawResource(R.raw.fed_heavy_cruiser_147pts);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
        Gson gson = new Gson();
        mPlayerShipStatus = gson.fromJson(jsonString, ShipSystemsDisplay.class);

        // Get the current GameInfo to find out if there is a current game and what turn it is in.
        mGameInfo = null;
        new GetGameInfoTask().execute();

        return view;
    }

    // Fills the Game Info object at startup - Performs database query outside GUI thread
    private class GetGameInfoTask extends AsyncTask<Object, Object, Cursor> {

        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        // Open database and return Cursor for all game info
        @Override
        protected Cursor doInBackground(Object... params) {
            databaseConnector.open();
            return databaseConnector.getAllGameInfo();
        }

        // Use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);

            mGameInfo = DatabaseInfoFactory.createGameInfo(result);

            result.close();
            databaseConnector.close();

            // Turn 0 is the Add Players step - That means the game has not been setup.
            // Create the default game.
            if (mGameInfo.getCurrentTurn() == 0) {
//                mGameInfo.
            }

//DHW            // Set text on Start (or Resume) game button
//DHW            // Once we know whether starting a game or resuming, Start Game button is always visible.
            String startGame = getActivity().getResources().getString(R.string.turn_add_players);
            mTurnLabel.setText(MoveTrackerMessages.getStartGameWPlanningLabelButton(startGame, startGame, mGameInfo, getActivity()));
        }
    }

    // Save currently displayed info
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // Called when the AboutFragment is paused
    @Override
    public void onPause() {
        super.onPause();
    }

    // Called when the AboutFragment resumes
    @Override
    public void onResume() {
        super.onResume();
    }

    // Now that we have the actual size, we can draw a hex grid on the background
    private void DrawHexGridBackground() {
        Paint paintLtGrayLine = new Paint();
        paintLtGrayLine.setAntiAlias(true);
        paintLtGrayLine.setStrokeWidth(3f);
        paintLtGrayLine.setColor(Color.DKGRAY);
        paintLtGrayLine.setStyle(Paint.Style.STROKE);
        paintLtGrayLine.setStrokeJoin(Paint.Join.ROUND);

        if (mBitmapWidth > 0 && mBitmapHeight > 0) {
            //  Bitmap.Config.RGB_565  Defaults to Black background
            Bitmap bg = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bg);

            long hexRowTopY = 0;

            while (hexRowTopY < mBitmapHeight) {
                long start1stTopX = 0;

                while (start1stTopX < mBitmapWidth) {
                    start1stTopX = DrawOneHexSegment(canvas, paintLtGrayLine, start1stTopX, hexRowTopY, mHexGridMapDef);
                }

                // Start next line of hex tops
                hexRowTopY = hexRowTopY + (2 * +mHexGridMapDef.getHexHeightBaseSize());
            }

            // Draw the remaining drones first, and then the player's ship.
            // If the player goes into the same hex as a drone we want the ship icon to show up on top.
            for (HexGridPosition oneDrone : mDroneHexGridPositions) {
                DrawIconOnHexGrid(canvas, R.drawable.ic_knight, oneDrone, mHexGridMapDef);
            }
            DrawIconOnHexGrid(canvas, R.drawable.ic_launcher, mPlayerShipHexGridPosition, mHexGridMapDef);

// Removed animated movement of Demon and Knight
//            DrawIconOnHexGrid(canvas, R.drawable.ic_knight, mDroneHexGridPosition, mHexGridMapDef);
//
//            // Move the Demon (App) Icon to the next hex in the same column
//            mPlayerShipHexGridPosition.moveOneHex(mPlayerShipHexGridPosition.getHeading());
//            mDroneHexGridPosition.moveOneHex(mDroneHexGridPosition.getHeading());

            // Set the completed Canvas as the background of the SurfaceView
            mHexMapSurfaceView.setBackgroundDrawable(new BitmapDrawable(bg));

            // Check which move buttons should be enabled
            //      Unit can Slip if it has moved at least 1 hex straight since last it's slip
            //      Unit can Turn if it has moved at least its turn mode at current speed in hexes straight since last it's turn
            boolean enableSlip = mPlayerShipHexGridPosition.getMoveSinceLastSlip() >= 1;
            boolean enableTurn = mPlayerShipHexGridPosition.getMoveSinceLastTurn() >= mPlayerShipTurnModeAtSpeed;

            mMoveLeftSlipButton.setEnabled(enableSlip);
            mMoveRightSlipButton.setEnabled(enableSlip);
            mMoveLeftTurnButton.setEnabled(enableTurn);
            mMoveRightTurnButton.setEnabled(enableTurn);

            String turnModeText = (enableTurn) ? "Yes" : (mPlayerShipHexGridPosition.getMoveSinceLastTurn() + "/" + mPlayerShipTurnModeAtSpeed);
            String moveText = "Heading: " + mPlayerShipHexGridPosition.getHeading()
                    + ", X: " + mPlayerShipHexGridPosition.getSquareGridPosX()
                    + ", Y: " + mPlayerShipHexGridPosition.getSquareGridPosY()
                    + ", Turn Mode: " + turnModeText
                    + ", Speed: " + mPlayerShipCurrentSpeed;
            mMoveLabel.setText(moveText);

            String turnText = "Turn: " + mGameTurn + ", Impulse: " + mGameImpulse + ", Phase: " + mGamePhase;
            mTurnLabel.setText(turnText);

            String playerShipStatus = mPlayerShipStatus.getEmpire()
                    + " - " + mPlayerShipStatus.getShipClass()
                    + " - Power: "
                    + mPlayerShipStatus.getPower().getPowerPoolUsedCount()
                    + "/" + mPlayerShipStatus.getPower().getPowerPoolUnusedCount()
                    + "/" + mPlayerShipStatus.getPower().getPowerPoolTotalCount();
            mShipStatusLabel.setText(playerShipStatus);
        }
    }

    private long DrawOneHexSegment(Canvas canvas, Paint paintLine, long start1stTopX, long topY, HexGridMapDefinition hexGridMapDef) {
        long end1stTopX = start1stTopX + (2 * hexGridMapDef.getHexWidthBaseSize());
        long start2ndTopX = end1stTopX + hexGridMapDef.getHexWidthBaseSize();
        long end2ndTopX = start2ndTopX + (2 * hexGridMapDef.getHexWidthBaseSize());
        long start3rdTopX = end2ndTopX + hexGridMapDef.getHexWidthBaseSize();

        long middleY = topY + hexGridMapDef.getHexHeightBaseSize();
        long bottomY = middleY + hexGridMapDef.getHexHeightBaseSize();

        // This is the pattern this draws.  Repeating it row after row makes a hex grid
        //     ____
        //      a  \ b     / e
        //          \____/
        //         /  d  \
        //      c /       \ f

        // a) Draw flat top of 1st hex - 2x base size
        canvas.drawLine(start1stTopX, topY, end1stTopX, topY, paintLine);

        // b) Draw diagonal down from right corner of flat top of 1st hex
        //    to flat top of 2nd hex (which is offset down)
        canvas.drawLine(end1stTopX, topY, start2ndTopX, middleY, paintLine);

        // c) Draw diagonal back down from left corner of flat top of 2nd hex
        //    to right corner of top of 1st hex in next hex row
        canvas.drawLine(start2ndTopX, middleY, end1stTopX, bottomY, paintLine);

        // d) Draw flat top of 2nd hex in row (which is offset down) - 2x base size
        canvas.drawLine(start2ndTopX, middleY, end2ndTopX, middleY, paintLine);

        // e) Draw diagonal from right corner of flat top of 2nd hex in row
        //    up to left corner of top of 3rd hex in row
        canvas.drawLine(end2ndTopX, middleY, start3rdTopX, topY, paintLine);

        // f) Draw diagonal from right corner of flat top of 2nd hex in row to
        //    to left corner of top of 3rd hex in next hex row
        canvas.drawLine(end2ndTopX, middleY, start3rdTopX, bottomY, paintLine);

        // Now can repeat cycle
        return start3rdTopX;
    }

    private void DrawIconOnHexGrid(Canvas canvas, int iconRId, HexGridPosition iconGridPos, HexGridMapDefinition hexGridMapDef) {

        Drawable appIcon = getResources().getDrawable(iconRId);

        // Determine next position for the icon
        long hexPosCenterY = hexGridMapDef.getHexCenterOffsetY(iconGridPos);
        if (hexPosCenterY > mBitmapHeight) {
            iconGridPos.setSquareGridPosY(0);
            iconGridPos.setSquareGridPosX(iconGridPos.getSquareGridPosX() + 1);
            hexPosCenterY = hexGridMapDef.getHexCenterOffsetY(iconGridPos);
        }

        long hexPosCenterX = hexGridMapDef.getHexCenterOffsetX(iconGridPos);
        if (hexPosCenterX > mBitmapWidth) {
            iconGridPos.setSquareGridPosX(0);
            iconGridPos.setSquareGridPosY(0);
            hexPosCenterX = hexGridMapDef.getHexCenterOffsetX(iconGridPos);
        }

        // Get the size of the icon and determine it's center and offset from the center of the hex it is in
        // NOTE: SCALED DOWN BY DIVIDING BY 2
        long iconWidth = appIcon.getIntrinsicWidth() / 2;
        long iconHeight = appIcon.getIntrinsicHeight() / 2;
        long iconCenterX = iconWidth / 2;
        long iconCenterY = iconHeight / 2;

        long iconPosLeft = hexPosCenterX - iconCenterX;
        long iconPosTop = hexPosCenterY - iconCenterY;

        double mHeading = 60.0 * (iconGridPos.getHeading() - 1);

        // Store the Canvas' current state before rotating it
        canvas.save();

        // Rotate Canvas around center point of icon we are about to draw on the Canvas
        // (We rotate the Canvas, not the icon)
        canvas.rotate((float) mHeading, (float) hexPosCenterX, (float) hexPosCenterY);

        // Now draw the icon on the rotated Canvas
        appIcon.setBounds((int) iconPosLeft, (int) iconPosTop, (int) (iconPosLeft + iconWidth), (int) (iconPosTop + iconHeight));
        appIcon.draw(canvas);

        // Restore the Canvas so it is the right orientation and the icon is now rotated
        canvas.restore();

    }

    View.OnClickListener onMoveLeftTurnButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Have the Demon (App) Icon do a left turn
            // Redraw the entire hex map with the updated positions
            mPlayerShipHexGridPosition.moveLeftTurn();
            incrementGamePhase();
            DrawHexGridBackground();
        }
    };

    View.OnClickListener onMoveLeftSlipButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Have the Demon (App) Icon do a left sideslip
            // Redraw the entire hex map with the updated positions
            mPlayerShipHexGridPosition.moveLeftSideslip();
            incrementGamePhase();
            DrawHexGridBackground();
        }
    };

    View.OnClickListener onMoveStraightButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Move the Demon (App) Icon to the next hex in the direction it is pointing
            // Redraw the entire hex map with the updated positions
            mPlayerShipHexGridPosition.moveStraight();
            incrementGamePhase();
            DrawHexGridBackground();
        }
    };

    View.OnClickListener onMoveRightSlipButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Have the Demon (App) Icon do a right sideslip
            // Redraw the entire hex map with the updated positions
            mPlayerShipHexGridPosition.moveRightSideslip();
            incrementGamePhase();
            DrawHexGridBackground();
        }
    };

    View.OnClickListener onMoveRightTurnButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Have the Demon (App) Icon do a right turn
            // Redraw the entire hex map with the updated positions
            mPlayerShipHexGridPosition.moveRightTurn();
            incrementGamePhase();
            DrawHexGridBackground();
        }
    };

    View.OnClickListener onEnergyAllocationButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Have the listener (MainActivity) show the Energy Allocation fragment
            mListener.onEnergyAllocation();
        }
    };

    View.OnClickListener onShipStatusButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Have the listener (MainActivity) show the Ship Status fragment
            mListener.onShipStatus();
        }
    };

    View.OnClickListener onEnemyStatusButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Have the listener (MainActivity) show the Enemy Status fragment
            mListener.onEnemyStatus();
        }
    };

    private void incrementGamePhase() {
        mGamePhase++;
        if (mGamePhase > 4) {
            mGamePhase = 1;
            mGameImpulse++;

            if (mGameImpulse > 8) {
                mGameImpulse = 1;
                mGameTurn++;
            }
        }
    }

}
