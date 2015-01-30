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
    private IDrawingBitmapSurface mDrawingViewInstance;

    private final int mWidth;
    private final int mHeight;

    public BitmapWorker(IDrawingBitmapSurface view, int width, int height) {
        mDrawingViewReference = new WeakReference<IDrawingBitmapSurface>(view);

        mWidth = width;
        mHeight = height;
    }

    @Override
    protected Bitmap doInBackground(double[][]... params) {
        if (mDrawingViewReference != null) {
            mDrawingViewInstance = mDrawingViewReference.get();

            if (mDrawingViewInstance != null) {
                return Bitmap.createScaledBitmap(ImageFormatFactory.double2DArrayToBitmap(params[0], mWidth, mHeight), mDrawingViewInstance.getRequiredBitmapWidth(), mDrawingViewInstance.getRequiredBitmapHeight(), false);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mDrawingViewInstance!= null && bitmap != null) {
            if (mDrawingViewInstance != null) {
                mDrawingViewInstance.draw(bitmap);
            }
        }
    }
}
