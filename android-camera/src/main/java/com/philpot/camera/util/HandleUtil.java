package com.philpot.camera.util;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.philpot.camera.helper.Handle;

public class HandleUtil {

    public static Handle getPressedHandle(float x, float y, float left, float top, float right, float bottom, float targetRadius) {
        Handle closestHandle = null;
        float closestDistance = Float.POSITIVE_INFINITY;

        final float distanceToTopLeft = calculateDistance(x, y, left, top);
        if (distanceToTopLeft < closestDistance) {
            closestDistance = distanceToTopLeft;
            closestHandle = Handle.TOP_LEFT;
        }

        final float distanceToTopRight = calculateDistance(x, y, right, top);
        if (distanceToTopRight < closestDistance) {
            closestDistance = distanceToTopRight;
            closestHandle = Handle.TOP_RIGHT;
        }

        final float distanceToBottomLeft = calculateDistance(x, y, left, bottom);
        if (distanceToBottomLeft < closestDistance) {
            closestDistance = distanceToBottomLeft;
            closestHandle = Handle.BOTTOM_LEFT;
        }

        final float distanceToBottomRight = calculateDistance(x, y, right, bottom);
        if (distanceToBottomRight < closestDistance) {
            closestDistance = distanceToBottomRight;
            closestHandle = Handle.BOTTOM_RIGHT;
        }

        if (closestDistance <= targetRadius) {
            return closestHandle;
        }

        if (HandleUtil.isInHorizontalTargetZone(x, y, left, right, top, targetRadius)) {
            return Handle.CENTER;
        } else if (HandleUtil.isInHorizontalTargetZone(x, y, left, right, bottom, targetRadius)) {
            return Handle.CENTER;
        } else if (HandleUtil.isInVerticalTargetZone(x, y, left, top, bottom, targetRadius)) {
            return Handle.CENTER;
        } else if (HandleUtil.isInVerticalTargetZone(x, y, right, top, bottom, targetRadius)) {
            return Handle.CENTER;
        }

        /*
        if (isWithinBounds(x, y, left/2, top/2, right/2, bottom/2)) {
            return Handle.CENTER;
        }
        */

        return null;
    }

    private static float calculateDistance(float x1, float y1, float x2, float y2) {
        final float side1 = x2 - x1;
        final float side2 = y2 - y1;

        return (float) Math.sqrt(side1 * side1 + side2 * side2);
    }

    private static boolean isInHorizontalTargetZone(float x, float y, float handleXStart, float handleXEnd, float handleY, float targetRadius) {
        return (x > handleXStart && x < handleXEnd && Math.abs(y - handleY) <= targetRadius);
    }


    private static boolean isInVerticalTargetZone(float x, float y, float handleX, float handleYStart, float handleYEnd, float targetRadius) {
        return (Math.abs(x - handleX) <= targetRadius && y > handleYStart && y < handleYEnd);
    }

    /*
    private static boolean isWithinBounds(float x, float y, float left, float top, float right, float bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }
    */
}