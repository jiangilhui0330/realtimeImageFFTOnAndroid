package me.panlong.realtimefftonimage;

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

    public MagnitudeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
    }

    public void draw(Bitmap bm) {
        Canvas canvas = mHolder.lockCanvas();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm, this.getWidth(), this.getHeight(), false);
        canvas.drawBitmap(scaledBitmap, 0, 0, null);
        mHolder.unlockCanvasAndPost(canvas);
        scaledBitmap.recycle();
    }
}
