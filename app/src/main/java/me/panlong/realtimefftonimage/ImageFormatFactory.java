package me.panlong.realtimefftonimage;

import android.graphics.Bitmap;

/**
 * Created by panlong on 11/1/15.
 */
public class ImageFormatFactory {
    public static double[][] NV21ToGrayScaleDouble2DArray(byte[] data, int width, int height) {
        double[][] pixels = new double[height][width];
        int p;
        for(int i = 0; i < height; i++) {
            for (int j = 0; j < width; j ++) {
                p = data[i * width + j] & 0xFF;
                pixels[i][j] = (double) (0xff000000 | p << 16 | p << 8 | p);
            }
        }

        return pixels;
    }

    public static Bitmap double2DArrayToBitmap(double[][] data, int width, int height) {
        int[] intData = new int[width * height];
        int p;

        for (int i = 0; i < height; i ++) {
            for (int j = 0; j < width; j ++) {
                intData[i * width + j] = (int) data[i][j];
            }
        }

        return Bitmap.createBitmap(intData, width, height, Bitmap.Config.ARGB_8888);
    }
}
