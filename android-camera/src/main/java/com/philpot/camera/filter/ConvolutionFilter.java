package com.philpot.camera.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.nio.IntBuffer;

public class ConvolutionFilter {

    public static Bitmap apply3x3Convolution(Bitmap bmpOriginal) {
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.Laplacian3x3, 3, 1);
    }

    public static Bitmap apply5x5Convolution(Bitmap bmpOriginal) {
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.Laplacian5x5, 5, 1);
    }

    public static Bitmap apply5x5OfGaussianConvolution(Bitmap bmpOriginal) {
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.Laplacian5x5OfGaussian, 5, 1);
    }

    public static Bitmap apply7x7Convolution(Bitmap bmpOriginal) {
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.Laplacian7x7, 7, 1);
    }

    public static Bitmap apply7x7_2Convolution(Bitmap bmpOriginal) {
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.Laplacian7x7_2, 7, 1);
    }

    public static Bitmap apply7x7_3Convolution(Bitmap bmpOriginal) {
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.Laplacian7x7_3, 7, 1);
    }

    public static Bitmap apply7x7OfGaussianConvolution(Bitmap bmpOriginal) {
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.Laplacian7x7OfGaussian, 7, 1);
    }

    public static Bitmap applyPrewittConvolution(Bitmap bmpOriginal) {
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.PrewittX, ConvolutionMatrix.PrewittY, 3);
    }

    public static Bitmap applyPrewittGaussianConvolution(Bitmap bmpOriginal) {
        bmpOriginal = applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.Gaussian5x5_1, 5, (1.0 / 256.0));
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.PrewittX, ConvolutionMatrix.PrewittY, 3);
    }

    public static Bitmap applyKirschConvolution(Bitmap bmpOriginal) {
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.KirschX, ConvolutionMatrix.KirschY, 3);
    }

    public static Bitmap applyKirschGaussianConvolution(Bitmap bmpOriginal) {
        bmpOriginal = applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.Gaussian5x5_1, 5, (1.0 / 256.0));
        return applyConvolutionFilter(bmpOriginal, ConvolutionMatrix.KirschX, ConvolutionMatrix.KirschY, 3);
    }

    private static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap enhanceBitmap(Bitmap bmp, float contrast) {
        bmp = toGrayscale(bmp);
        ColorMatrix cm = new ColorMatrix();

        float scale = contrast + 1.f;
        float translate = (-.5f * scale + .5f) * 255.f;
        cm.set(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0 });


        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    private static Bitmap applyConvolutionFilter(Bitmap bmp, int[][] filterMatrix, int filterDimensions, double factor) {
        int bitmapHeight = bmp.getHeight();
        int bitmapWidth = bmp.getWidth();
        int[] pixelBuffer = new int[bitmapHeight * bitmapWidth];
        bmp.getPixels(pixelBuffer, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
        int[] resultBuffer = new int[pixelBuffer.length];

        int blue;
        int green;
        int red;

        int filterOffset = (filterDimensions - 1) / 2;
        int byteOffset;
        int currentPixel;
        int currentFilter;

        /*
        if (grayscale) {
            float rgb = 0;

            for (int k = 0; k < pixelBuffer.length; k++) {
                currentPixel = pixelBuffer[k];
                rgb = ((currentPixel & 0x00FF0000) >> 16) * 0.299f; //red
                rgb += ((currentPixel & 0x0000FF00) >> 8) * 0.587f; //green
                rgb += (currentPixel & 0x000000FF) * 0.114f; //blue
                pixelBuffer[k] = ((int)rgb) + ((int)rgb << 8) + ((int)rgb << 16) + (255 << 24);
            }
        }
        */

        for (int offsetY = filterOffset; offsetY < bitmapHeight - filterOffset; offsetY++) {
            for (int offsetX = filterOffset; offsetX < bitmapWidth - filterOffset; offsetX++) {
                red = 0;
                green = 0;
                blue = 0;

                byteOffset = offsetY * bitmapWidth + offsetX;

                for (int filterY = -filterOffset; filterY <= filterOffset; filterY++) {
                    for (int filterX = -filterOffset; filterX <= filterOffset; filterX++) {
                        currentPixel = pixelBuffer[byteOffset + filterX + (filterY * bitmapWidth)];
                        currentFilter = filterMatrix[filterY + filterOffset][filterX + filterOffset];

                        red += ((currentPixel & 0x00FF0000) >> 16) * currentFilter;
                        green += ((currentPixel & 0x0000FF00) >> 8) * currentFilter;
                        blue += (currentPixel & 0x000000FF) * currentFilter;
                    }
                }

                red *= factor;
                blue *= factor;
                green *= factor;

                if (blue > 255) blue = 255;
                else if (blue < 0) blue = 0;

                if (green > 255) green = 255;
                else if (green < 0) green = 0;

                if (red > 255) red = 255;
                else if (red < 0) red = 0;

                //noinspection UnusedAssignment
                resultBuffer[byteOffset++] = (blue) + (green << 8) + (red << 16) + (255 << 24);
            }
        }

        bmp.copyPixelsFromBuffer(IntBuffer.wrap(resultBuffer));
        return bmp;
    }

    private static Bitmap applyConvolutionFilter(Bitmap bmp, int[][] xFilterMatrix, int[][] yFilterMatrix, int filterDimensions) {
        int bitmapHeight = bmp.getHeight();
        int bitmapWidth = bmp.getWidth();
        int[] pixelBuffer = new int[bitmapHeight * bitmapWidth];
        bmp.getPixels(pixelBuffer, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
        int[] resultBuffer = new int[pixelBuffer.length];

        int blueX;
        int greenX;
        int redX;

        int blueY;
        int greenY;
        int redY;

        int blueTotal;
        int greenTotal;
        int redTotal;

        int filterOffset = (filterDimensions - 1) / 2;
        int byteOffset;
        int currentPixel;
        int xCurrentFilter;
        int yCurrentFilter;

        /*
        if (grayscale){
            float rgb = 0;

            for (int k = 0; k < pixelBuffer.length; k++){
                currentPixel = pixelBuffer[k];
                rgb = ((currentPixel & 0x00FF0000) >> 16) * 0.299f; //red
                rgb += ((currentPixel & 0x0000FF00) >> 8) * 0.587f; //green
                rgb += (currentPixel & 0x000000FF) * 0.114f; //blue
                pixelBuffer[k] = ((int)rgb) + ((int)rgb << 8) + ((int)rgb << 16) + (255 << 24);
            }
        }
        */

        for (int offsetY = filterOffset; offsetY < bitmapHeight - filterOffset; offsetY++) {
            for (int offsetX = filterOffset; offsetX < bitmapWidth - filterOffset; offsetX++) {
                redX = greenX = blueX = 0;
                redY = greenY = blueY = 0;
                //noinspection UnusedAssignment
                redTotal = greenTotal = blueTotal = 0;

                byteOffset = offsetY * bitmapWidth + offsetX;

                for (int filterY = -filterOffset; filterY <= filterOffset; filterY++) {
                    for (int filterX = -filterOffset; filterX <= filterOffset; filterX++) {
                        currentPixel = pixelBuffer[byteOffset + filterX + (filterY * bitmapWidth)];
                        xCurrentFilter = xFilterMatrix[filterY + filterOffset][filterX + filterOffset];
                        yCurrentFilter = yFilterMatrix[filterY + filterOffset][filterX + filterOffset];

                        redX += ((currentPixel & 0x00FF0000) >> 16) * xCurrentFilter;
                        greenX += ((currentPixel & 0x0000FF00) >> 8) * xCurrentFilter;
                        blueX += (currentPixel & 0x000000FF) * xCurrentFilter;

                        redY += ((currentPixel & 0x00FF0000) >> 16) * yCurrentFilter;
                        greenY += ((currentPixel & 0x0000FF00) >> 8) * yCurrentFilter;
                        blueY += (currentPixel & 0x000000FF) * yCurrentFilter;
                    }
                }

                blueTotal = (int)Math.sqrt((blueX * blueX) + (blueY * blueY));
                greenTotal = (int)Math.sqrt((greenX * greenX) + (greenY * greenY));
                redTotal = (int)Math.sqrt((redX * redX) + (redY * redY));

                if (blueTotal > 255) blueTotal = 255;
                else if (blueTotal < 0) blueTotal = 0;

                if (greenTotal > 255) greenTotal = 255;
                else if (greenTotal < 0) greenTotal = 0;

                if (redTotal > 255) redTotal = 255;
                else if (redTotal < 0) redTotal = 0;

                //noinspection UnusedAssignment
                resultBuffer[byteOffset++] = (blueTotal) + (greenTotal << 8) + (redTotal << 16) + (255 << 24);
            }
        }

        bmp.copyPixelsFromBuffer(IntBuffer.wrap(resultBuffer));
        return bmp;
    }
}

