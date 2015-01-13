package me.panlong.realtimefftonimage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by panlong on 13/1/15.
 */
public class DrawingView extends View {
    public static final float PAINT_STROKE_WIDTH = 3f;

    private IChosenRecChangedListener mChosenAreaChangedListener;

    private int mChosenRecCenterX;
    private int mChosenRecCenterY;
    private int mChosenRecWidth;
    private int mChosenRecHeight;

    private Paint mPaint;

    private Boolean isDrawing;

    public DrawingView(Context context) {
        super(context);

        mChosenRecWidth = -1;
        mChosenRecHeight = -1;

        isDrawing = false;

        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(PAINT_STROKE_WIDTH);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public void setChosenRecWidth(int width) {
        mChosenRecWidth = width;
        invalidate();
        notifyChosenRecChangedListener();
    }

    public void setChosenRecHeight(int height) {
        mChosenRecHeight = height;
            invalidate();
            notifyChosenRecChangedListener();
        }

    public void setChosenAreaChangedListener(IChosenRecChangedListener listener) {
        mChosenAreaChangedListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDrawing) {
            final int motionAction = event.getAction();

            switch (motionAction) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    calculateCoorsFromAction(event);
                    invalidate();
                    notifyChosenRecChangedListener();
                    break;
                default:
                    break;
            }
        }

        return true;
    }

    private void calculateCoorsFromAction(MotionEvent event) {
        int viewX = (int) (event.getX() - this.getLeft());
        int viewY = (int) (event.getY() - this.getTop());

        if (viewX - mChosenRecWidth / 2 < 0) {
            mChosenRecCenterX = mChosenRecWidth / 2;
        } else if (viewX + mChosenRecWidth / 2 > this.getWidth()) {
            mChosenRecCenterX = this.getWidth() - mChosenRecWidth / 2;
        } else {
            mChosenRecCenterX = viewX;
        }

        if (viewY - mChosenRecHeight / 2 < 0) {
            mChosenRecCenterY = mChosenRecHeight / 2;
        } else if (viewY + mChosenRecHeight / 2 > this.getHeight()) {
            mChosenRecCenterY = this.getHeight() - mChosenRecHeight / 2;
        } else {
            mChosenRecCenterY = viewY;
        }
    }

    private void notifyChosenRecChangedListener() {
        if (mChosenAreaChangedListener != null) {
            int topLeftX = mChosenRecCenterX - mChosenRecWidth / 2;
            int topLeftY = mChosenRecCenterY - mChosenRecHeight / 2;
            mChosenAreaChangedListener.chosenRecChanged(topLeftX, topLeftY, mChosenRecWidth, mChosenRecHeight);
        }
    }

    public void startDrawing(int topLeftX, int topLeftY, int width, int height) {
        mChosenRecWidth = width;
        mChosenRecHeight = height;
        mChosenRecCenterX = topLeftX + mChosenRecWidth / 2;
        mChosenRecCenterY = topLeftY + mChosenRecHeight / 2;

        isDrawing = true;
        invalidate();
    }

    public void stopDrawing() {
        isDrawing = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isDrawing) {
            canvas.drawRect(mChosenRecCenterX - mChosenRecWidth / 2,
                    mChosenRecCenterY - mChosenRecHeight / 2,
                    mChosenRecCenterX + mChosenRecWidth / 2,
                    mChosenRecCenterY + mChosenRecHeight / 2,
                    mPaint);
        }
    }
}
