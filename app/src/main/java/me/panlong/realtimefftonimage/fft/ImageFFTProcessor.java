package me.panlong.realtimefftonimage.fft;

import android.util.Log;

import java.util.Arrays;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

/**
 * Created by panlong on 11/1/15.
 */
public class ImageFFTProcessor {
    private static DoubleFFT_2D mFFTProcessor;

    private double[][] mImageData;
    private double[][] mFFTResultData;
    private double[][] mFFTInverseImageData;
    private int mImageRows;
    private int mImageColumns;

    private int mFilterPhaseLeft;
    private int mFilterPhaseTop;
    private int mFilterPhaseRight;
    private int mFilterPhaseBottom;

    private double[][] mMagnitudeOfResult;
    private double[][] mPhaseOfResult;

    public ImageFFTProcessor(int rows, int columns) {
        mFFTProcessor = new DoubleFFT_2D(rows, columns);

        mImageRows = rows;
        mImageColumns = columns;

        mImageData = null;
        mFFTResultData = null;
        mFFTInverseImageData = null;
        mMagnitudeOfResult = null;
        mPhaseOfResult = null;
    }

    public void readImageData(double[][] data, int filterPhaseLeft, int filterPhaseTop, int filterPhaseRight, int filterPhaseBottom) {
        mFilterPhaseLeft = filterPhaseLeft;
        mFilterPhaseTop = filterPhaseTop;
        mFilterPhaseRight = filterPhaseRight;
        mFilterPhaseBottom = filterPhaseBottom;

        int centerX = (mFilterPhaseLeft + mFilterPhaseRight) / 2;
        int centerY = (mFilterPhaseTop + mFilterPhaseBottom) / 2;
        int newCenterX, newCenterY;

        if (centerX < mImageColumns / 2) {
            newCenterX = centerX + mImageColumns / 2;
        } else {
            newCenterX = centerX - mImageColumns / 2;
        }

        if (centerY < mImageRows / 2) {
            newCenterY = centerY + mImageRows / 2;
        } else {
            newCenterY = centerY - mImageRows / 2;
        }

        mFilterPhaseLeft = mFilterPhaseLeft - centerX + newCenterX;
        mFilterPhaseRight = mFilterPhaseRight - centerX + newCenterX;
        mFilterPhaseTop = mFilterPhaseTop - centerY + newCenterY;
        mFilterPhaseBottom = mFilterPhaseBottom - centerY + newCenterY;

        mImageData = new double[mImageRows][mImageColumns * 2];
        for (int i = 0; i < mImageRows; i++) {
            for (int j = 0; j < mImageColumns; j++) {
                mImageData[i][2 * j] = data[i][j];
                mImageData[i][2 * j + 1] = 0.0;
            }
        }

        performFFT();
    }

    public double[][] getFFTInverse() {
        if (mFFTInverseImageData == null) {
            mFFTProcessor.complexInverse(mFFTResultData, false);

            double maxValue = 0;
            for (int i = 0; i < mImageRows; i++) {
                for (int j = 0; j < mImageColumns; j++) {
                    maxValue = Math.max(maxValue, Math.abs(mFFTResultData[i][j * 2]));
                }
            }

            mFFTInverseImageData = new double[mImageRows][mImageColumns];
            int pixel;
            for (int i = 0; i < mImageRows; i ++) {
                for (int j = 0; j < mImageColumns; j ++) {
                    pixel = (int) Math.abs(mFFTResultData[i][j * 2] / maxValue * 255);
                    pixel = 255 - pixel;

                    mFFTInverseImageData[i][j] = (double) (0xff000000 | pixel << 16 | pixel << 8 | pixel);
                }
            }
        }

        return mFFTInverseImageData;
    }

    public double[][] getMagnitudeOfResult() {
        return mMagnitudeOfResult;
    }

    public double[][] getPhaseOfResult() {
        return mPhaseOfResult;
    }

    private void performFFT() {
        mFFTProcessor.complexForward(mImageData);

        mMagnitudeOfResult = new double[mImageRows][mImageColumns];
        mPhaseOfResult = new double[mImageRows][mImageColumns];
        mFFTResultData = new double[mImageRows][mImageColumns * 2];

        double maxMag = 0;
        double maxPhase = 0;

        for (int i = 0; i < mImageRows; i++) {
            for (int j = 0; j < mImageColumns; j++) {
                double re = mImageData[i][2 * j];
                double im = mImageData[i][2 * j + 1];


                if (mFilterPhaseLeft == mFilterPhaseRight
                        || (i >= mFilterPhaseLeft && i <= mFilterPhaseRight && j >= mFilterPhaseTop && j <= mFilterPhaseBottom)
                        || (i >= 2 * mImageColumns - mFilterPhaseRight && i <= 2 * mImageColumns - mFilterPhaseLeft
                && j >= 2 * mImageRows - mFilterPhaseBottom && j <= 2 * mImageRows - mFilterPhaseTop)) {
                    mMagnitudeOfResult[i][j] = Math.log(re * re + im * im + 0.01);
                    mPhaseOfResult[i][j] = Math.atan2(im, re) + Math.PI;
                } else {
                    mMagnitudeOfResult[i][j] = 0;
                    mPhaseOfResult[i][j] = 0;
                }

                maxMag = Math.max(mMagnitudeOfResult[i][j], maxMag);
                maxPhase = Math.max(mPhaseOfResult[i][j], maxPhase);
            }
        }

        int magPixel, phasePixel;
        double angle;

        for (int i = 0; i < mImageRows; i ++) {
            for (int j = 0; j < mImageColumns; j ++) {
                magPixel = (int) (Math.sqrt(Math.exp(mMagnitudeOfResult[i][j])) / Math.sqrt(Math.exp(maxMag)) * 255);
                phasePixel = (int) (mPhaseOfResult[i][j] / maxPhase * 255);

                angle = phasePixel / 255.0 * 2 * Math.PI - Math.PI;

                mFFTResultData[i][j * 2] = magPixel * Math.cos(angle);
                mFFTResultData[i][j * 2 + 1] = magPixel * Math.sin(angle);
            }
        }

        mImageData = shiftOrigin(mImageData);

        maxMag = maxPhase = 0;
        for (int i = 0; i < mImageRows; i++) {
            for (int j = 0; j < mImageColumns; j++) {
                double re = mImageData[i][2 * j];
                double im = mImageData[i][2 * j + 1];

                mMagnitudeOfResult[i][j] = Math.log(re * re + im * im + 0.01);
                maxMag = Math.max(mMagnitudeOfResult[i][j], maxMag);

                mPhaseOfResult[i][j] = Math.atan2(im, re) + Math.PI;
                maxPhase = Math.max(mPhaseOfResult[i][j], maxPhase);
            }
        }

        for (int i = 0; i < mImageRows; i ++) {
            for (int j = 0; j < mImageColumns; j ++) {
                magPixel = (int) (mMagnitudeOfResult[i][j] / maxMag * 255);
                phasePixel = (int) (mPhaseOfResult[i][j] / maxPhase * 255);

                mMagnitudeOfResult[i][j] = (double) (0xff000000 | magPixel << 16 | magPixel << 8 | magPixel);
                mPhaseOfResult[i][j] = (double) (0xff000000 | phasePixel << 16 | phasePixel << 8 | phasePixel);
            }
        }
    }

    private static double[][] shiftOrigin(double[][] data) {
        int numberOfRows = data.length;
        int numberOfCols = data[0].length;
        int newRows;
        int newCols;

        double[][] output =
                new double[numberOfRows][numberOfCols];

        //Must treat the data differently when the
        // dimension is odd than when it is even.
        if (numberOfRows % 2 != 0) {
            newRows = numberOfRows +
                    (numberOfRows + 1) / 2;
        } else {
            newRows = numberOfRows + numberOfRows / 2;
        }

        if (numberOfCols % 2 != 0) {
            newCols = numberOfCols +
                    (numberOfCols + 1) / 2;
        } else {
            newCols = numberOfCols + numberOfCols / 2;
        }

        //Create a temporary working array.
        double[][] temp =
                new double[newRows][newCols];

        //Copy input data into the working array.
        for (int row = 0; row < numberOfRows; row++) {
            for (int col = 0; col < numberOfCols; col++) {
                temp[row][col] = data[row][col];
            }
        }

        //Do the horizontal shift first
        if (numberOfCols % 2 != 0) {//shift for odd
            //Slide leftmost (numberOfCols+1)/2 columns
            // to the right by numberOfCols columns
            for (int row = 0; row < numberOfRows; row++) {
                for (int col = 0;
                     col < (numberOfCols + 1) / 2; col++) {
                    temp[row][col + numberOfCols] =
                            temp[row][col];
                }
            }
            //Now slide everything back to the left by
            // (numberOfCols+1)/2 columns
            for (int row = 0; row < numberOfRows; row++) {
                for (int col = 0;
                     col < numberOfCols; col++) {
                    temp[row][col] =
                            temp[row][col + (numberOfCols + 1) / 2];
                }
            }

        } else {//shift for even
            //Slide leftmost (numberOfCols/2) columns
            // to the right by numberOfCols columns.
            for (int row = 0; row < numberOfRows; row++) {
                for (int col = 0;
                     col < numberOfCols / 2; col++) {
                    temp[row][col + numberOfCols] =
                            temp[row][col];
                }
            }

            //Now slide everything back to the left by
            // numberOfCols/2 columns
            for (int row = 0; row < numberOfRows; row++) {
                for (int col = 0;
                     col < numberOfCols; col++) {
                    temp[row][col] =
                            temp[row][col + numberOfCols / 2];
                }
            }
        }//end else
        //Now do the vertical shift
        if (numberOfRows % 2 != 0) {//shift for odd
            //Slide topmost (numberOfRows+1)/2 rows
            // down by numberOfRows rows.
            for (int col = 0; col < numberOfCols; col++) {
                for (int row = 0;
                     row < (numberOfRows + 1) / 2; row++) {
                    temp[row + numberOfRows][col] =
                            temp[row][col];
                }
            }
            //Now slide everything back up by
            // (numberOfRows+1)/2 rows.
            for (int col = 0; col < numberOfCols; col++) {
                for (int row = 0;
                     row < numberOfRows; row++) {
                    temp[row][col] =
                            temp[row + (numberOfRows + 1) / 2][col];
                }
            }

        } else {//shift for even
            //Slide topmost (numberOfRows/2) rows down
            // by numberOfRows rows
            for (int col = 0; col < numberOfCols; col++) {
                for (int row = 0;
                     row < numberOfRows / 2; row++) {
                    temp[row + numberOfRows][col] =
                            temp[row][col];
                }
            }

            //Now slide everything back up by
            // numberOfRows/2 rows.
            for (int col = 0; col < numberOfCols; col++) {
                for (int row = 0;
                     row < numberOfRows; row++) {
                    temp[row][col] =
                            temp[row + numberOfRows / 2][col];
                }
            }
        }

        //Shifting of the origin is complete.  Copy
        // the rearranged data from temp to output
        // array.
        for (int row = 0; row < numberOfRows; row++) {
            for (int col = 0; col < numberOfCols; col++) {
                output[row][col] = temp[row][col];
            }
        }
        return output;
    }

    private static double[][] inverseShift(double[][] data) {
        int numberOfRows = data.length;
        int numberOfCols = data[0].length;
        int newRows;
        int newCols;

        double[][] output =
                new double[numberOfRows][numberOfCols];

        //Must treat the data differently when the
        // dimension is odd than when it is even.
        if (numberOfRows % 2 != 0) {
            newRows = numberOfRows +
                    (numberOfRows - 1) / 2;
        } else {
            newRows = numberOfRows + numberOfRows / 2;
        }

        if (numberOfCols % 2 != 0) {
            newCols = numberOfCols +
                    (numberOfCols - 1) / 2;
        } else {
            newCols = numberOfCols + numberOfCols / 2;
        }

        //Create a temporary working array.
        double[][] temp =
                new double[newRows][newCols];

        //Copy input data into the working array.
        for (int row = 0; row < numberOfRows; row++) {
            for (int col = 0; col < numberOfCols; col++) {
                temp[row][col] = data[row][col];
            }
        }

        //do the vertical shift first
        if (numberOfRows % 2 != 0) {//shift for odd
            //Slide topmost (numberOfRows-1)/2 rows
            // down by numberOfRows rows.
            for (int col = 0; col < numberOfCols; col++) {
                for (int row = 0;
                     row < (numberOfRows - 1) / 2; row++) {
                    temp[row + numberOfRows][col] =
                            temp[row][col];
                }
            }
            //Now slide everything back up by
            // (numberOfRows-1)/2 rows.
            for (int col = 0; col < numberOfCols; col++) {
                for (int row = 0;
                     row < numberOfRows; row++) {
                    temp[row][col] =
                            temp[row + (numberOfRows - 1) / 2][col];
                }
            }

        } else {//shift for even
            //Slide topmost (numberOfRows/2) rows down
            // by numberOfRows rows
            for (int col = 0; col < numberOfCols; col++) {
                for (int row = 0;
                     row < numberOfRows / 2; row++) {
                    temp[row + numberOfRows][col] =
                            temp[row][col];
                }
            }

            //Now slide everything back up by
            // numberOfRows/2 rows.
            for (int col = 0; col < numberOfCols; col++) {
                for (int row = 0;
                     row < numberOfRows; row++) {
                    temp[row][col] =
                            temp[row + numberOfRows / 2][col];
                }
            }
        }

        //now do the horizontal shift
        if (numberOfCols % 2 != 0) {//shift for odd
            //Slide leftmost (numberOfCols-1)/2 columns
            // to the right by numberOfCols columns
            for (int row = 0; row < numberOfRows; row++) {
                for (int col = 0;
                     col < (numberOfCols - 1) / 2; col++) {
                    temp[row][col + numberOfCols] =
                            temp[row][col];
                }
            }
            //Now slide everything back to the left by
            // (numberOfCols-1)/2 columns
            for (int row = 0; row < numberOfRows; row++) {
                for (int col = 0;
                     col < numberOfCols; col++) {
                    temp[row][col] =
                            temp[row][col + (numberOfCols - 1) / 2];
                }
            }

        } else {//shift for even
            //Slide leftmost (numberOfCols/2) columns
            // to the right by numberOfCols columns.
            for (int row = 0; row < numberOfRows; row++) {
                for (int col = 0;
                     col < numberOfCols / 2; col++) {
                    temp[row][col + numberOfCols] =
                            temp[row][col];
                }
            }

            //Now slide everything back to the left by
            // numberOfCols/2 columns
            for (int row = 0; row < numberOfRows; row++) {
                for (int col = 0;
                     col < numberOfCols; col++) {
                    temp[row][col] =
                            temp[row][col + numberOfCols / 2];
                }
            }
        }//end else


        //Shifting of the origin is complete.  Copy
        // the rearranged data from temp to output
        // array.
        for (int row = 0; row < numberOfRows; row++) {
            for (int col = 0; col < numberOfCols; col++) {
                output[row][col] = temp[row][col];
            }
        }
        return output;
    }
}
