package me.panlong.realtimefftonimage;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

/**
 * Created by panlong on 11/1/15.
 */
public class ImageFFTProcessor {
    private static DoubleFFT_2D mFFTProcessor;

    private double[][] mImageData;
    private int mImageRows;
    private int mImageColumns;
    private double[][] mMagnitudeOfResult;
    private double[][] mPhaseOfResult;

    public ImageFFTProcessor(int rows, int columns) {
        mFFTProcessor = new DoubleFFT_2D(rows, columns);

        mImageRows = rows;
        mImageColumns = columns;

        mImageData = null;
        mMagnitudeOfResult = null;
        mPhaseOfResult = null;
    }

    public void readImageData(double[][] data) {
        mImageData = new double[mImageRows][mImageColumns * 2];
        for (int i = 0; i < mImageRows; i++) {
            for (int j = 0; j < mImageColumns; j++) {
                mImageData[i][2 * j] = data[i][j];
                mImageData[i][2 * j + 1] = 0.0;
            }
        }

        performFFT();
    }

    public double[][] getMagnitudeOfResult() {
        if (mMagnitudeOfResult == null) {
            performFFT();
        }
        return mMagnitudeOfResult;
    }

    public double[][] getPhaseOfResult() {
        if (mPhaseOfResult == null) {
            performFFT();
        }
        return mPhaseOfResult;
    }

    private void performFFT() {
        mFFTProcessor.complexForward(mImageData);

        mImageData = shiftOrigin(mImageData);
        mMagnitudeOfResult = new double[mImageRows][mImageColumns];
        mPhaseOfResult = new double[mImageRows][mImageColumns];

        for (int i = 0; i < mImageRows; i++) {
            for (int j = 0; j < mImageColumns; j++) {
                double re = mImageData[i][2 * j];
                double im = mImageData[i][2 * j + 1];

                mMagnitudeOfResult[i][j] = Math.sqrt(re * re + im * im);
                int pixel = (int) (Math.abs(Math.atan2(im, re)) / Math.PI * 255);
                mPhaseOfResult[i][j] = (double) (0xff000000 | pixel << 16 | pixel << 8 | pixel);
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
}
