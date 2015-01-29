package me.panlong.realtimefftonimage.camera;

/**
 * Created by panlong on 11/1/15.
 */
public interface ICameraFrameListener {
    public void onCameraFrame(byte[] data, int width, int height);
}
