package me.panlong.realtimefftonimage.resultView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by panlong on 13/1/15.
 */
public class InteractiveView extends View implements IDrawingBitmapSurface {
    public static final float PAINT_STROKE_WIDTH = 3f;
    public static final int DEFAULT_REC_WIDTH = 128;
    public static final int DEFAULT_REC_HEIGHT = 96;
    public static final int REC_WIDTH_INCREASE_UNIT = 32;
    public static final int REC_HEIGHT_INCREASES_UNIT = 24;
    public static final int MIN_CHOSEN_REC_WIDTH = 32;
    public static final int MIN_CHOSEN_REC_HEIGHT = 24;

    private IChosenRecChangedListener mChosenAreaChangedListener;

    private Bitmap mBitmap;

    private int mChosenRecCenterX;
    private int mChosenRecCenterY;
    private int mChosenRecWidth;
    private int mChosenRecHeight;

    private Paint mPaint;

    private Boolean isDrawingRec;
    private Boolean isDrawingBitmap;

    public InteractiveView(Context context) {
        super(context);

        isDrawingRec = false;
        isDrawingBitmap = false;

        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(PAINT_STROKE_WIDTH);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public void increaseRecSize() {
        setChosenRecSize(mChosenRecWidth + REC_WIDTH_INCREASE_UNIT,
                mChosenRecHeight + REC_HEIGHT_INCREASES_UNIT);

        invalidate();
        notifyChosenRecChangedListener();
    }

    public void decreaseRecSize() {
        setChosenRecSize(mChosenRecWidth - REC_WIDTH_INCREASE_UNIT,
                mChosenRecHeight - REC_HEIGHT_INCREASES_UNIT);

        invalidate();
        notifyChosenRecChangedListener();
    }

    private Boolean isRecSizePossibleToSet(int width, int height) {
        if (width < MIN_CHOSEN_REC_WIDTH || width > this.getWidth()) {
            return false;
        }

        if (height < MIN_CHOSEN_REC_HEIGHT || height > this.getHeight()) {
            return false;
        }

        return true;
    }

    private void setChosenRecSize(int width, int height) {
        if (isRecSizePossibleToSet(width, height)) {
            mChosenRecWidth = width;
            mChosenRecHeight = height;

            if (mChosenRecCenterX - mChosenRecWidth / 2 < 0) {
                mChosenRecCenterX = mChosenRecWidth / 2;
            } else if (mChosenRecCenterX + mChosenRecWidth / 2 > this.getWidth()) {
                mChosenRecCenterX = this.getWidth() - mChosenRecWidth / 2;
            }

            if (mChosenRecCenterY - mChosenRecHeight / 2 < 0) {
                mChosenRecCenterY = mChosenRecHeight / 2;
            } else if (mChosenRecCenterY + mChosenRecHeight / 2 > this.getHeight()) {
                mChosenRecCenterY = this.getHeight() - mChosenRecHeight / 2;
            }
        }
    }

    public void setChosenAreaChangedListener(IChosenRecChangedListener listener) {
        mChosenAreaChangedListener = listener;
        notifyChosenRecChangedListener();
    }

    private void setDefaultRec() {
        mChosenRecWidth = DEFAULT_REC_WIDTH;
        mChosenRecHeight = DEFAULT_REC_HEIGHT;
        mChosenRecCenterX = this.getWidth() / 2;
        mChosenRecCenterY = this.getHeight() / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDrawingRec) {
            final int motionAction = event.getAction();

            switch (motionAction) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    int viewX = (int) (event.getX() - this.getLeft());
                    int viewY = (int) (event.getY() - this.getTop());

                    setCenter(viewX, viewY);
                    invalidate();
                    notifyChosenRecChangedListener();
                    break;

                default:
                    break;
            }
        }

        return true;
    }

    private void setCenter(int x, int y) {
        if (x - mChosenRecWidth / 2 < 0) {
            mChosenRecCenterX = mChosenRecWidth / 2;
        } else if (x + mChosenRecWidth / 2 > this.getWidth()) {
            mChosenRecCenterX = this.getWidth() - mChosenRecWidth / 2;
        } else {
            mChosenRecCenterX = x;
        }

        if (y - mChosenRecHeight / 2 < 0) {
            mChosenRecCenterY = mChosenRecHeight / 2;
        } else if (y + mChosenRecHeight / 2 > this.getHeight()) {
            mChosenRecCenterY = this.getHeight() - mChosenRecHeight / 2;
        } else {
            mChosenRecCenterY = y;
        }
    }

    private void notifyChosenRecChangedListener() {
        Thread notifyUserThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mChosenAreaChangedListener != null) {
                    int topLeftX = mChosenRecCenterX - mChosenRecWidth / 2;
                    int topLeftY = mChosenRecCenterY - mChosenRecHeight / 2;
                    mChosenAreaChangedListener.chosenRecChanged(topLeftX, topLeftY, mChosenRecWidth, mChosenRecHeight);
                }
            }
        });

        notifyUserThread.start();
    }

    public void startDrawingRec() {
        isDrawingRec = true;
        setDefaultRec();

        invalidate();
        notifyChosenRecChangedListener();
    }

    public void stopDrawingRec() {
        isDrawingRec = false;
        invalidate();
    }

    public void startDrawingBitmap() {
        isDrawingBitmap = true;
        invalidate();
    }

    public void stopDrawingBitmap() {
        isDrawingBitmap = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isDrawingRec) {
            canvas.drawRect(mChosenRecCenterX - mChosenRecWidth / 2,
                    mChosenRecCenterY - mChosenRecHeight / 2,
                    mChosenRecCenterX + mChosenRecWidth / 2,
                    mChosenRecCenterY + mChosenRecHeight / 2,
                    mPaint);
        }

        if (isDrawingBitmap && mBitmap != null) {
            int topLeftX, topLeftY;

            if (isDrawingRec) {
                topLeftX = mChosenRecCenterX - mChosenRecWidth / 2;
                topLeftY = mChosenRecCenterY - mChosenRecHeight / 2;
                mBitmap = Bitmap.createScaledBitmap(mBitmap, mChosenRecWidth, mChosenRecHeight, false);
            } else {
                topLeftX = topLeftY = 0;
                mBitmap = Bitmap.createScaledBitmap(mBitmap, this.getWidth(), this.getHeight(), false);
            }

            canvas.drawBitmap(mBitmap, topLeftX, topLeftY, null);
        }
    }

    @Override
    public void draw(Bitmap bm) {
        mBitmap = bm;
        invalidate();
    }
}
