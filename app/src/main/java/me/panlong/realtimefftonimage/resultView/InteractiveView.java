package me.panlong.realtimefftonimage.resultView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by panlong on 13/1/15.
 */
public class InteractiveView extends View {
    public static final float PAINT_STROKE_WIDTH = 3f;
    public static final int DEFAULT_REC_WIDTH = 128;
    public static final int DEFAULT_REC_HEIGHT = 96;
    public static final int REC_WIDTH_INCREASE_UNIT = 32;
    public static final int REC_HEIGHT_INCREASES_UNIT = 24;
    public static final int MIN_CHOSEN_REC_WIDTH = 32;
    public static final int MIN_CHOSEN_REC_HEIGHT = 24;

    private IChosenRecChangedListener mChosenAreaChangedListener;

    private int mChosenRecCenterX;
    private int mChosenRecCenterY;
    private int mChosenRecWidth;
    private int mChosenRecHeight;

    private Paint mPaint;

    private Boolean isDrawing;

    public InteractiveView(Context context) {
        super(context);

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
        if (isDrawing) {
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

    public void startDrawing() {
        isDrawing = true;
        setDefaultRec();

        invalidate();
        notifyChosenRecChangedListener();
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
