package com.dhwingert.fedcom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.database.DatabaseInfoFactory;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.hexgridmap.HexGridMapDefinition;
import com.dhwingert.fedcom.hexgridmap.HexGridPosition;
import com.dhwingert.fedcom.movetracker.MoveTrackerMessages;

// Removed animated movement of Demon and Knight
//import java.util.Timer;
//import java.util.TimerTask;

/**
 * About this program
 *
 * Created by David Wingert on 1/2/2015.
 */
public class AboutFragment extends Fragment {

    // Callback methods implemented by MainActivity
    public interface AboutFragmentListener {
        // Called when the user clicks the Resume Game button
        // Called when user decides to delete this game and start a new one
        // Called when user clicks the Goto Hex Map button
        public void onAboutFragmentResumeGame(GameInfo gameInfo);
        public void onDeleteGame();
        public void onAboutFragmentGotoHexMap();
    }

    // Reference to MainActivity as a listener to this fragment
    private AboutFragmentListener mListener;

    private Button mStartGameButton;
    private Button mDeleteGameButton;
    private Button mGotoHexMapButton;

    private GameInfo mGameInfo;

    private SurfaceView mHexMapSurfaceView;
    private int mBitmapWidth = 0;
    private int mBitmapHeight = 0;
    private HexGridMapDefinition mHexGridMapDef;
    private HexGridPosition mDemonHexGridPosition;

// Removed animated movement of Demon and Knight
//      private HexGridPosition mKnightHexGridPosition;
//    private Timer mTimer;
//    private TimerTask mTimerTask;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    // Set AboutFragmentListener when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (AboutFragmentListener) activity;
    }

    // Remove AboutFragmentListener when fragment is detached
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
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // Display the version info
        TextView versionTextView = (TextView) view.findViewById(R.id.versionTextView);

        PackageManager manager = getActivity().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            versionTextView.setText(info.versionName);
        } catch (Exception e) {
            versionTextView.setText(getResources().getString(R.string.message_version_not_found));
        }

//        Toast.makeText(this,
//                       "PackageName = " + info.packageName + "\nVersionCode = "
//                               + info.versionCode + "\nVersionName = "
//                               + info.versionName + "\nPermissions = " + info.permissions, Toast.LENGTH_SHORT).show();

        // Setup Start Game button
        mStartGameButton = (Button) view.findViewById(R.id.aboutResumeButton);
        mStartGameButton.setOnClickListener(mStartGameButtonClicked);

        // Setup Delete Game button
        mDeleteGameButton = (Button) view.findViewById(R.id.deleteGameButton);
        mDeleteGameButton.setOnClickListener(mDeleteGameButtonClicked);

        // Setup Goto Hex Map Game button
        mGotoHexMapButton = (Button) view.findViewById(R.id.gotoHexMapButton);
        mGotoHexMapButton.setOnClickListener(mGotoHexMapButtonClicked);

        // To start with none of the buttons are visible until
        // we find out if there is a game in progress or not
        mStartGameButton.setVisibility(View.GONE);
        mDeleteGameButton.setVisibility(View.GONE);
        mGotoHexMapButton.setVisibility(View.GONE);

        // Get the current GameInfo to find out if there is a current game and what turn it is in.
        mGameInfo = null;
        new GetGameInfoTask().execute();

        // Set up a listener on the LinerLayout that actually holds the About information
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

                // Start the Demon (App) Icon on the 0, 0 location in the hex grid (upper left corner)
// Removed animated movement of Demon and Knight
//                mDemonHexGridPosition = new HexGridPosition(0, 0, 3);
//                mKnightHexGridPosition = new HexGridPosition(2, 0, 3);
                mDemonHexGridPosition = new HexGridPosition(2, 1, 1);

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

// Removed animated movement of Demon and Knight
//                startTimer();
            }
        });

        return view;
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
// Removed animated movement of Demon and Knight
//        stopTimerTask();
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

            DrawIconOnHexGrid(canvas, R.drawable.ic_launcher, mDemonHexGridPosition, mHexGridMapDef);
// Removed animated movement of Demon and Knight
//            DrawIconOnHexGrid(canvas, R.drawable.ic_knight, mKnightHexGridPosition, mHexGridMapDef);
//
//            // Move the Demon (App) Icon to the next hex in the same column
//            mDemonHexGridPosition.moveOneHex(mDemonHexGridPosition.getHeading());
//            mKnightHexGridPosition.moveOneHex(mKnightHexGridPosition.getHeading());

            // Set the completed Canvas as the background of the SurfaceView
            mHexMapSurfaceView.setBackgroundDrawable(new BitmapDrawable(bg));
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
        long iconWidth = appIcon.getIntrinsicWidth();
        long iconHeight = appIcon.getIntrinsicHeight();
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

// Removed animated movement of Demon and Knight
//    public void startTimer() {
//        //set a new Timer
//        mTimer = new Timer();
//
//        //initialize the TimerTask's job
//        initializeTimerTask();
//
//        //schedule the timer, after the first 2000ms the TimerTask will run every 2000ms
//        mTimer.schedule(mTimerTask, 2000, 2000); //
//    }
//
//    public void stopTimerTask() {
//        //stop the timer, if it's not already null
//        if (mTimer != null) {
//            mTimer.cancel();
//            mTimer = null;
//        }
//    }
//
//    public void initializeTimerTask() {
//        mTimerTask = new TimerTask() {
//            public void run() {
//
//                //use a handler to run a toast that shows the current timestamp
//                handler.post(new Runnable() {
//                    public void run() {
//                        DrawHexGridBackground();
//                    }
//                });
//            }
//        };
//    }

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

            // Set text on New (or resume) game button
            // Once we know whether starting a new game or resuming, Start Game button is always visible.
            String startGame = getActivity().getResources().getString(R.string.turn_new_game);
            String addPlayersTitle = getActivity().getResources().getString(R.string.turn_add_players);
            mStartGameButton.setText(MoveTrackerMessages.getStartGameWPlanningLabelButton(startGame, addPlayersTitle, mGameInfo, getActivity()));
            mStartGameButton.setVisibility(View.VISIBLE);

            // Delete Game button is only visible if game has at least gotten to the planning stage
            if (mGameInfo.getCurrentTurn() >= 0) {
                mDeleteGameButton.setVisibility(View.VISIBLE);
                mGotoHexMapButton.setVisibility(View.GONE);
            } else {
                mDeleteGameButton.setVisibility(View.GONE);
                mGotoHexMapButton.setVisibility(View.VISIBLE);
            }
        }
    }

    // Display the Current Impulse when the Start Turn button is clicked
    View.OnClickListener mStartGameButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
// Removed animated movement of Demon and Knight
//            stopTimerTask();

            // If there was a game at least at the planning step, resume it
            if (mGameInfo.getCurrentTurn() >= 0) {
                mListener.onAboutFragmentResumeGame(mGameInfo);
            }
            // Else the previous game never got past the config step
            //      Flush the entire database and start over
            //      To be sure we don't have remnants of the previous game in the tables
            else {
                final DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

                // AsyncTask restarts game and notifies Listener
                AsyncTask<Object, Object, Object> newGameTask = new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        databaseConnector.restartGame();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        mListener.onDeleteGame();
                    }
                };

                newGameTask.execute();
            }
        }
    };

    View.OnClickListener mDeleteGameButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // use FragmentManager to display the mConfirmDelete DialogFragment
            mConfirmNewGame.show(getFragmentManager(), "confirm new game");
        }
    };

    View.OnClickListener mGotoHexMapButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onAboutFragmentGotoHexMap();
        }
    };

    // DialogFragment to confirm new game (deletion of all existing game content)
    private DialogFragment mConfirmNewGame = new DialogFragment() {

        // create an AlertDialog and return it
        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.confirm_new_game_title);
            builder.setMessage(R.string.confirm_new_game_message);

            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

                    // AsyncTask restarts game and notifies Listener
                    AsyncTask<Object, Object, Object> newGameTask = new AsyncTask<Object, Object, Object>() {
                        @Override
                        protected Object doInBackground(Object... params) {
                            databaseConnector.restartGame();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object result) {
// Removed animated movement of Demon and Knight
//                            stopTimerTask();
                            mListener.onDeleteGame();
                        }
                    };

                    newGameTask.execute();
                }
            });

            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create();
        }

    };

}
