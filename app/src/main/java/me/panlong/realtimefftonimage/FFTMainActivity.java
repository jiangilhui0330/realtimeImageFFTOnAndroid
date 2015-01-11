package me.panlong.realtimefftonimage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class FFTMainActivity extends Activity implements ICameraFrameListener {
    private CameraPreview mCameraPreview;
    private MagnitudeSurfaceView mMagnitudeSurfaceView;
    private PhaseSurfaceView mPhaseSurfaceView;

    private Button mPauseAndResumeBtn;
    private Boolean mIsStarted;

    private ImageFFTProcessor mFFTProcessor;

    private Bitmap mMagnitudeBitmap;
    private Bitmap mPhaseBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fftmain);

        mCameraPreview = (CameraPreview) findViewById(R.id.surfaceView_cameraPreview);
        mCameraPreview.setCameraFrameListener(this);

        mMagnitudeSurfaceView = (MagnitudeSurfaceView) findViewById(R.id.surfaceView_magnitude);
        mPhaseSurfaceView = (PhaseSurfaceView) findViewById(R.id.surfaceView_phase);

        mIsStarted = false;

        mPauseAndResumeBtn = (Button) findViewById(R.id.button_startFFT);
        mPauseAndResumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsStarted = !mIsStarted;
                if (!mIsStarted) {
                    mCameraPreview.freezeCameraPreview();
                } else {
                    mCameraPreview.resumeCameraPreview();
                }
            }
        });

        mFFTProcessor = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fftmain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraFrame(byte[] data, int width, int height) {
        if (mIsStarted) {
            if (mFFTProcessor == null) {
                mFFTProcessor = new ImageFFTProcessor(height, width);
            }

            double[][] convertedGrayScaleData = ImageFormatFactory.NV21ToGrayScaleDouble2DArray(data, width, height);

            mFFTProcessor.readImageData(convertedGrayScaleData);

            mMagnitudeBitmap = ImageFormatFactory.double2DArrayToBitmap(mFFTProcessor.getMagnitudeOfResult(), width, height);
            mPhaseBitmap = ImageFormatFactory.double2DArrayToBitmap(mFFTProcessor.getPhaseOfResult(), width, height);

            mMagnitudeSurfaceView.draw(mMagnitudeBitmap);
            mPhaseSurfaceView.draw(mPhaseBitmap);

            mMagnitudeBitmap.recycle();
            mPhaseBitmap.recycle();
        }
    }
}
