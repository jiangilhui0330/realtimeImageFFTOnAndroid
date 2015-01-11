package me.panlong.realtimefftonimage;

import org.jtransforms.fft.DoubleFFT_2D;

/**
 * Created by panlong on 11/1/15.
 */
public class ImageFFTProcessor {
    private static DoubleFFT_2D fftProcessor;

    private double[][] imageData;
    private int imageRows;
    private int imageColumns;
    private double[][] magnitudeOfResult;
    private double[][] phaseOfResult;

    public ImageFFTProcessor() {
        imageData = null;
        magnitudeOfResult = null;
        phaseOfResult = null;
    }

    public void readImageData(double[][] data) {
        imageRows = data.length;
        imageColumns = data[0].length;

        imageData = new double[imageRows][imageColumns * 2];
        for (int i = 0; i < imageRows; i++) {
            for (int j = 0; j < imageColumns; j++) {
                imageData[i][2 * j] = data[i][j];
                imageData[i][2 * j + 1] = 0.0;
            }
        }

        magnitudeOfResult = null;
        phaseOfResult = null;
    }

    public double[][] getMagnitudeOfResult() {
        if (magnitudeOfResult == null) {
            performFFT();
        }
        return magnitudeOfResult;
    }

    public double[][] getPhaseOfResult() {
        if (phaseOfResult == null) {
            performFFT();
        }
        return phaseOfResult;
    }

    private void performFFT() {
        fftProcessor.complexForward(imageData);

        imageData = shiftOrigin(imageData);
        magnitudeOfResult = new double[imageRows][imageColumns];
        phaseOfResult = new double[imageRows][imageColumns];

        for (int i = 0; i < imageRows; i++) {
            for (int j = 0; j < imageColumns; j++) {
                double re = imageData[i][2 * j];
                double im = imageData[i][2 * j + 1];

                magnitudeOfResult[i][j] = Math.log(re * re + im * im + 0.01);
                phaseOfResult[i][j] = Math.atan2(im, re);
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
