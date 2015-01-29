package me.panlong.realtimefftonimage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import me.panlong.realtimefftonimage.camera.CameraPreview;
import me.panlong.realtimefftonimage.camera.ICameraFrameListener;
import me.panlong.realtimefftonimage.fft.ImageFFTProcessor;
import me.panlong.realtimefftonimage.resultView.IChosenRecChangedListener;
import me.panlong.realtimefftonimage.resultView.InteractiveView;
import me.panlong.realtimefftonimage.resultView.MagnitudeSurfaceView;
import me.panlong.realtimefftonimage.resultView.PhaseSurfaceView;
import me.panlong.realtimefftonimage.utils.BitmapWorker;
import me.panlong.realtimefftonimage.utils.ImageFormatFactory;


public class FFTMainActivity extends Activity implements ICameraFrameListener, IChosenRecChangedListener {
    private CameraPreview mCameraPreview;
    private InteractiveView mInteractiveView;
    private MagnitudeSurfaceView mMagnitudeSurfaceView;
    private PhaseSurfaceView mPhaseSurfaceView;

    private Button mPauseAndResumeBtn;
    private Button mChooseAreaBtn;
    private Button mIncreaseRecBtn;
    private Button mDecreaseRecBtn;
    private Button mInverseBtn;
    private Boolean mIsStarted;
    private Boolean mIsChosenArea;
    private Boolean mIsDrawingInverse;

    private ImageFFTProcessor mFFTProcessor;

    private Bitmap mMagnitudeBitmap;
    private Bitmap mPhaseBitmap;

    private int mChosenRecTopLeftX;
    private int mChosenRecTopLeftY;
    private int mChosenRecWidth;
    private int mChosenRecHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fftmain);

        initPreviewFrameLayout();

        initMagAndPhaseViews();

        initButtons();

        mIsStarted = false;
        mIsChosenArea = false;
        mIsDrawingInverse = false;
        mFFTProcessor = null;
    }

    private void initButtons() {
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

        mChooseAreaBtn = (Button) findViewById(R.id.button_chooseArea);
        mChooseAreaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsChosenArea = !mIsChosenArea;
                if (mIsChosenArea) {
                    mInteractiveView.startDrawingRec();
                } else {
                    mInteractiveView.stopDrawingRec();

                    mIsDrawingInverse = false;
                    mInteractiveView.stopDrawingBitmap();
                }
            }
        });

        mIncreaseRecBtn = (Button) findViewById(R.id.button_increaseRec);
        mIncreaseRecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsChosenArea) {
                    mInteractiveView.increaseRecSize();
                }
            }
        });

        mDecreaseRecBtn = (Button) findViewById(R.id.button_decreaseRec);
        mDecreaseRecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsChosenArea) {
                    mInteractiveView.decreaseRecSize();
                }
            }
        });

        mInverseBtn = (Button) findViewById(R.id.button_inverse);
        mInverseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsDrawingInverse = !mIsDrawingInverse;
                if (mIsDrawingInverse) {
                    mInteractiveView.startDrawingBitmap();
                } else {
                    mInteractiveView.stopDrawingBitmap();
                }
            }
        });
    }

    private void initMagAndPhaseViews() {
        mMagnitudeSurfaceView = (MagnitudeSurfaceView) findViewById(R.id.surfaceView_magnitude);
        mPhaseSurfaceView = (PhaseSurfaceView) findViewById(R.id.surfaceView_phase);
    }

    private void initPreviewFrameLayout() {
        FrameLayout cameraPreviewFrame = (FrameLayout) findViewById(R.id.frameLayout_cameraPreview);

        mCameraPreview = new CameraPreview(this);
        mCameraPreview.setCameraFrameListener(this);

        mInteractiveView = new InteractiveView(this);
        mInteractiveView.setChosenAreaChangedListener(this);

        cameraPreviewFrame.addView(mCameraPreview);
        cameraPreviewFrame.addView(mInteractiveView);
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
            if (mIsChosenArea) {
                double scaledFactor = width * 1.0 / mInteractiveView.getWidth();
                int scaledTopLeftX = (int) (mChosenRecTopLeftX * scaledFactor);
                int scaledTopLeftY = (int) (mChosenRecTopLeftY * scaledFactor);
                int scaledRecWidth = (int) (mChosenRecWidth * scaledFactor);
                int scaledRecHeight = (int) (mChosenRecHeight * scaledFactor);

                mFFTProcessor = new ImageFFTProcessor(scaledRecHeight, scaledRecWidth);

                for (int i = 0; i < scaledRecHeight; i++) {
                    for (int j = 0; j < scaledRecWidth; j++) {
                        int chosenImageX = j + scaledTopLeftX;
                        int chosenImageY = i + scaledTopLeftY;

                        data[i * scaledRecWidth + j] = data[chosenImageY * width + chosenImageX];
                    }
                }

                width = scaledRecWidth;
                height = scaledRecHeight;
            } else {
                mFFTProcessor = new ImageFFTProcessor(height, width);
            }

            double[][] convertedGrayScaleData = ImageFormatFactory.NV21ToGrayScaleDouble2DArray(data, width, height);

            mFFTProcessor.readImageData(convertedGrayScaleData);

            BitmapWorker magWorker = new BitmapWorker(mMagnitudeSurfaceView, width, height);
            BitmapWorker phaseWorker = new BitmapWorker(mPhaseSurfaceView, width, height);

            magWorker.execute(mFFTProcessor.getMagnitudeOfResult());
            phaseWorker.execute(mFFTProcessor.getPhaseOfResult());

            if (mIsDrawingInverse) {
                BitmapWorker inverseWorker = new BitmapWorker(mInteractiveView, width, height);
                inverseWorker.execute(mFFTProcessor.getFFTInverse());
            }
        }
    }

    @Override
    public void chosenRecChanged(int topLeftX, int topLeftY, int width, int height) {
        mChosenRecTopLeftX = topLeftX;
        mChosenRecTopLeftY = topLeftY;
        mChosenRecWidth = width;
        mChosenRecHeight = height;
    }
}
