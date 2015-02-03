package me.panlong.realtimefftonimage.resultView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by panlong on 11/1/15.
 */
public class PhaseSurfaceView extends View implements IDrawingBitmapSurface {
    private static final float PAINT_STROKE_WIDTH = 2f;
    private Bitmap mBitmap;
    private Paint mPaint;

    private int mChosenCenterX;
    private int mChosenCenterY;
    private int mRecWidth;
    private int mRecHeight;

    private int mWidth;
    private int mHeight;

    private Boolean mIsFilteringPhase;

    public PhaseSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mIsFilteringPhase = false;

        initPaint();
    }

    @Override
    public int getRequiredBitmapWidth() {
        return getWidth();
    }

    @Override
    public int getRequiredBitmapHeight() {
        return getHeight();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

        if (mIsFilteringPhase) {
            canvas.drawRect(mChosenCenterX - mRecWidth / 2,
                    mChosenCenterY - mRecHeight / 2,
                    mChosenCenterX + mRecWidth / 2,
                    mChosenCenterY + mRecHeight / 2,
                    mPaint);

            canvas.drawRect(mWidth - mChosenCenterX - mRecWidth / 2,
                    mHeight - mChosenCenterY - mRecHeight / 2,
                    mWidth - mChosenCenterX + mRecWidth / 2,
                    mHeight - mChosenCenterY + mRecHeight / 2,
                    mPaint);
        }
    }

    public void draw(Bitmap bm) {
        mBitmap = bm;
        invalidate();
    }

    private void initPaint() {
        mPaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(PAINT_STROKE_WIDTH);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public void clean() {
        mIsFilteringPhase = false;
        invalidate();
    }

    public Boolean isFilteringPhase() {
        return mIsFilteringPhase;
    }

    public int getChosenCenterX() {
        return mChosenCenterX;
    }

    public int getChosenCenterY() {
        return mChosenCenterY;
    }

    public int getChosenWidth() {
        return mRecWidth;
    }

    public int getChosenHeight() {
        return mRecHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mWidth = getWidth();
        mHeight = getHeight();
        mRecWidth = mWidth / 4;
        mRecHeight = mHeight / 4;

        mIsFilteringPhase = true;

        final int motionAction = event.getAction();

        switch (motionAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int centerX = (int) event.getX();
                int centerY = (int) event.getY();

                // make sure the whole rectangle is in the region of surface view
                if (centerX - mRecWidth / 2 < 0) {
                    mChosenCenterX = mRecWidth / 2;
                } else if (centerX + mRecWidth / 2 > mWidth) {
                    mChosenCenterX = mWidth - mRecWidth / 2;
                } else {
                    mChosenCenterX = centerX;
                }

                if (centerY - mRecHeight / 2 < 0) {
                    mChosenCenterY = mRecHeight / 2;
                } else if (centerY + mRecHeight / 2 > mHeight) {
                    mChosenCenterY = mHeight - mRecHeight / 2;
                } else {
                    mChosenCenterY = centerY;
                }

                // make sure the rectangle does not cross the central lines
                if (mChosenCenterX < mWidth / 2 && mChosenCenterX + mRecWidth / 2 > mWidth / 2) {
                    mChosenCenterX = mWidth / 2 - mRecWidth / 2;
                } else if (mChosenCenterX > mWidth / 2 && mChosenCenterX - mRecWidth / 2 < mWidth / 2) {
                    mChosenCenterX = mWidth / 2 + mRecWidth / 2;
                }

                if (mChosenCenterY < mHeight / 2 && mChosenCenterY + mRecHeight / 2 > mHeight / 2) {
                    mChosenCenterY = mHeight / 2 - mRecHeight / 2;
                } else if (mChosenCenterY > mHeight / 2 && mChosenCenterY - mRecHeight / 2 < mHeight / 2) {
                    mChosenCenterY = mHeight / 2 + mRecHeight / 2;
                }

                invalidate();
                break;

            default:
                break;
        }
        return true;
    }
}
