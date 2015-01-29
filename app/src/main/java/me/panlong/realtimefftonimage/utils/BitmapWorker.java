package me.panlong.realtimefftonimage.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import me.panlong.realtimefftonimage.resultView.IDrawingBitmapSurface;

/**
 * Created by panlong on 29/1/15.
 */
public class BitmapWorker extends AsyncTask<double[][], Void, Bitmap> {
    private final WeakReference<IDrawingBitmapSurface> mDrawingViewReference;

    private final int mWidth;
    private final int mHeight;

    private Bitmap mBitmap;

    public BitmapWorker(IDrawingBitmapSurface view, int width, int height) {
        mDrawingViewReference = new WeakReference<IDrawingBitmapSurface>(view);

        mWidth = width;
        mHeight = height;
    }

    @Override
    protected Bitmap doInBackground(double[][]... params) {
        return ImageFormatFactory.double2DArrayToBitmap(params[0], mWidth, mHeight);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mDrawingViewReference != null && bitmap != null) {
            final IDrawingBitmapSurface drawingView = mDrawingViewReference.get();
            if (drawingView != null) {
                drawingView.draw(bitmap);
            }
        }
    }
}
