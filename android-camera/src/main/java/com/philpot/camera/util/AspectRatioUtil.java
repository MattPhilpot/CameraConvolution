package com.philpot.camera.util;

import android.content.res.Resources;

public class AspectRatioUtil {
    public static float calculateAspectRatio(float left, float top, float right, float bottom) {
        return (right - left) / (bottom - top);
    }

    public static float getLeft(float top, float right, float bottom, float targetAspectRatio) {
        return right - (targetAspectRatio * (bottom - top));
    }

    public static float getTop(float left, float right, float bottom, float targetAspectRatio) {
        return bottom - ((right - left) / targetAspectRatio);
    }

    public static float getRight(float left, float top, float bottom, float targetAspectRatio) {
        return (targetAspectRatio * (bottom - top)) + left;
    }

    public static float getBottom(float left, float top, float right, float targetAspectRatio) {
        return ((right - left) / targetAspectRatio) + top;
    }

    public static float getWidth(float height, float targetAspectRatio) {
        return targetAspectRatio * height;
    }

    public static float getHeight(float width, float targetAspectRatio) {
        return width / targetAspectRatio;
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}