package me.panlong.realtimefftonimage;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by panlong on 11/1/15.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    public static final int CAMERA_PREVIEW_WIDTH = 320;
    public static final int CAMERA_PREVIEW_HEIGHT = 240;

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private CameraFrameListner mCameraFrameListner;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        Camera camera = CameraUtils.getCameraInstance();
        setCamera(camera);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        setPreferredCameraPreviewSize();

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    private void setPreferredCameraPreviewSize() {
        Camera.Parameters cameraParams = mCamera.getParameters();
        List<Camera.Size> supportedCameraPreviewSizes = cameraParams.getSupportedPreviewSizes();
        int preferredCameraPreviewSizeId = CameraUtils.closest(supportedCameraPreviewSizes, CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT);

        cameraParams.setPreviewSize(supportedCameraPreviewSizes.get(preferredCameraPreviewSizeId).width, supportedCameraPreviewSizes.get(preferredCameraPreviewSizeId).height);
        mCamera.setParameters(cameraParams);
    }

    public void setCameraFrameListner(CameraFrameListner listner) {
        mCameraFrameListner = listner;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();

            try {
                mCamera.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCameraFrameListner != null)
            mCameraFrameListner.onCameraFrame(data);
    }
}
