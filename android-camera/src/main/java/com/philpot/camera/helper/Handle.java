package com.philpot.camera.helper;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;

public enum Handle {

    TOP_LEFT(new CornerHandleHelper(ImagePreviewEdge.TOP, ImagePreviewEdge.LEFT)),
    TOP_RIGHT(new CornerHandleHelper(ImagePreviewEdge.TOP, ImagePreviewEdge.RIGHT)),
    BOTTOM_LEFT(new CornerHandleHelper(ImagePreviewEdge.BOTTOM, ImagePreviewEdge.LEFT)),
    BOTTOM_RIGHT(new CornerHandleHelper(ImagePreviewEdge.BOTTOM, ImagePreviewEdge.RIGHT)),
    LEFT(new VerticalHandleHelper(ImagePreviewEdge.LEFT)),
    TOP(new HorizontalHandleHelper(ImagePreviewEdge.TOP)),
    RIGHT(new VerticalHandleHelper(ImagePreviewEdge.RIGHT)),
    BOTTOM(new HorizontalHandleHelper(ImagePreviewEdge.BOTTOM)),
    CENTER(new CenterHandleHelper());

    private final HandleHelper mHelper;

    Handle(HandleHelper helper) {
        mHelper = helper;
    }

    public void updateCropWindow(float x, float y, @NonNull RectF imageRect, float snapRadius) {
        mHelper.updateCropWindow(x, y, imageRect, snapRadius);
    }

    public PointF getOffset(float x, float y, float left, float top, float right, float bottom) {
        float touchOffsetX = 0;
        float touchOffsetY = 0;

        switch (this) {

            case TOP_LEFT:
                touchOffsetX = left - x;
                touchOffsetY = top - y;
                break;
            case TOP_RIGHT:
                touchOffsetX = right - x;
                touchOffsetY = top - y;
                break;
            case BOTTOM_LEFT:
                touchOffsetX = left - x;
                touchOffsetY = bottom - y;
                break;
            case BOTTOM_RIGHT:
                touchOffsetX = right - x;
                touchOffsetY = bottom - y;
                break;
            case LEFT:
                touchOffsetX = left - x;
                touchOffsetY = 0;
                break;
            case TOP:
                touchOffsetX = 0;
                touchOffsetY = top - y;
                break;
            case RIGHT:
                touchOffsetX = right - x;
                touchOffsetY = 0;
                break;
            case BOTTOM:
                touchOffsetX = 0;
                touchOffsetY = bottom - y;
                break;
            case CENTER:
                touchOffsetX = ((right + left) / 2) - x; //centerx - x
                touchOffsetY = ((top + bottom) / 2) - y; //centery - y
                break;
        }

        return new PointF(touchOffsetX, touchOffsetY);
    }
}