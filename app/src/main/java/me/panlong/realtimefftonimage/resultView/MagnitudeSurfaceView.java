package me.panlong.realtimefftonimage.resultView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by panlong on 11/1/15.
 */
public class MagnitudeSurfaceView extends SurfaceView implements IDrawingBitmapSurface {
    private SurfaceHolder mHolder;
    private Bitmap mScaledBitmap;

    public MagnitudeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
    }

    public void draw(Bitmap bm) {
        mScaledBitmap = Bitmap.createScaledBitmap(bm, this.getWidth(), this.getHeight(), false);

        Canvas canvas = mHolder.lockCanvas();
        if (canvas != null) {
            canvas.drawBitmap(mScaledBitmap, 0, 0, null);
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
}
