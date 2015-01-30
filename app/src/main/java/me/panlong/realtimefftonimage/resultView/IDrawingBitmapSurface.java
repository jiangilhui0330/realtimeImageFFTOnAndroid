package me.panlong.realtimefftonimage.resultView;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by panlong on 11/1/15.
 */
public interface IDrawingBitmapSurface {
    public int getRequiredBitmapWidth();
    public int getRequiredBitmapHeight();
    public void draw(Bitmap bm);
}
