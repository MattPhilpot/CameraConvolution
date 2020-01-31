package com.philpot.camera.util;

import android.graphics.Matrix;
import android.graphics.RectF;

public class CropMath {

    public static float[] getCornersFromRect(RectF r) {
        return new float[] { r.left, r.top, r.right, r.top, r.right, r.bottom, r.left, r.bottom };
    }

    public static RectF trapToRect(float[] array) {
        RectF r = new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (int i = 1; i < array.length; i += 2) {
            float x = array[i - 1];
            float y = array[i];
            r.left = (x < r.left) ? x : r.left;
            r.top = (y < r.top) ? y : r.top;
            r.right = (x > r.right) ? x : r.right;
            r.bottom = (y > r.bottom) ? y : r.bottom;
        }
        r.sort();
        return r;
    }

    public static void getEdgePoints(RectF imageBound, float[] array) {
        if (array.length >= 2) {
            for (int x = 0; x < array.length; x += 2) {
                array[x] = clamp(array[x], imageBound.left, imageBound.right);
                array[x + 1] = clamp(array[x + 1], imageBound.top, imageBound.bottom);
            }
        }
    }

    private static float clamp(float i, float low, float high) {
        return Math.max(Math.min(i, high), low);
    }

    public static RectF getScaledCropBounds(RectF cropBounds,
                                            RectF photoBounds, RectF displayBounds) {
        Matrix m = new Matrix();
        m.setRectToRect(photoBounds, displayBounds, Matrix.ScaleToFit.FILL);
        RectF trueCrop = new RectF(cropBounds);
        if (!m.mapRect(trueCrop)) {
            return null;
        }
        return trueCrop;
    }

    public static boolean setImageToScreenMatrix(Matrix dst, RectF image,
                                                 RectF screen, int rotation) {
        RectF rotatedImageRect = new RectF();
        dst.setRotate(rotation);
        dst.mapRect(rotatedImageRect, image);
        float swidth = screen.width();
        float sheight = screen.height();
        float iwidth = rotatedImageRect.width();
        float iheight = rotatedImageRect.height();
        float scale, dx, dy;

        if (iwidth <= swidth && iheight <= sheight) {
            scale = 1;
        } else {
            scale = Math.min(swidth / iwidth, sheight / iheight);
        }

        dx = (swidth - iwidth * scale) * 0.5f;
        dy = (sheight - iheight * scale) * 0.5f;
        dst.setRotate(rotation);
        boolean ps = dst.postScale(scale, scale);
        boolean pt = dst.postTranslate(dx, dy);
        return ps && pt;
    }

    public static boolean setCropToScreenMatrix(Matrix dst, RectF crop,
                                                RectF screen) {
        float swidth = screen.width();
        float sheight = screen.height();
        float cwidth = crop.width();
        float cheight = crop.height();
        float scale, dx, dy;

        if (cwidth <= swidth && cheight <= sheight) {
            scale = 1;
        } else {
            scale = Math.min(swidth / cwidth, sheight / cheight);
        }

        dx = (swidth - cwidth * scale) * 0.5f;
        dy = (sheight - cheight * scale) * 0.5f;
        dst.setScale(scale, scale);
        return dst.postTranslate(dx, dy);
    }

    public static int calculateInSampleSize(int rawWidth, int rawHeight, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            final int halfHeight = rawHeight / 2;
            final int halfWidth = rawWidth / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
