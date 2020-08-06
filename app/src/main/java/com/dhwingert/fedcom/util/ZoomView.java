package com.dhwingert.fedcom.util;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;

/**
 * Created by David Wingert on 12/22/2015.
 */
public class ZoomView extends SurfaceView {
    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 5f;

    private float mScaleFactor = 1.f;
    private ScaleGestureDetector mDetector;

    public ZoomView(Context context) {
        super(context);
        mDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor);

        // Your canvas-drawing code goes here


        canvas.restore();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(MIN_ZOOM, Math.min(mScaleFactor, MAX_ZOOM));
            return true;
        }
    }
}
