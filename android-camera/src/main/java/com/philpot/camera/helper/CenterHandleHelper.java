package com.philpot.camera.helper;

import android.graphics.RectF;
import android.support.annotation.NonNull;

class CenterHandleHelper extends HandleHelper {
    CenterHandleHelper() {
        super(null, null);
    }

    @Override
    void updateCropWindow(float x, float y, @NonNull RectF imageRect, float snapRadius) {
        float left = ImagePreviewEdge.LEFT.getCoordinate();
        float top = ImagePreviewEdge.TOP.getCoordinate();
        float right = ImagePreviewEdge.RIGHT.getCoordinate();
        float bottom = ImagePreviewEdge.BOTTOM.getCoordinate();

        final float currentCenterX = (left + right) / 2;
        final float currentCenterY = (top + bottom) / 2;

        final float offsetX = x - currentCenterX;
        final float offsetY = y - currentCenterY;

        ImagePreviewEdge.LEFT.offset(offsetX);
        ImagePreviewEdge.TOP.offset(offsetY);
        ImagePreviewEdge.RIGHT.offset(offsetX);
        ImagePreviewEdge.BOTTOM.offset(offsetY);

        if (ImagePreviewEdge.LEFT.isOutsideMargin(imageRect, snapRadius)) {
            final float offset = ImagePreviewEdge.LEFT.snapToRect(imageRect);
            ImagePreviewEdge.RIGHT.offset(offset);
        } else if (ImagePreviewEdge.RIGHT.isOutsideMargin(imageRect, snapRadius)) {
            final float offset = ImagePreviewEdge.RIGHT.snapToRect(imageRect);
            ImagePreviewEdge.LEFT.offset(offset);
        }

        if (ImagePreviewEdge.TOP.isOutsideMargin(imageRect, snapRadius)) {
            final float offset = ImagePreviewEdge.TOP.snapToRect(imageRect);
            ImagePreviewEdge.BOTTOM.offset(offset);
        } else if (ImagePreviewEdge.BOTTOM.isOutsideMargin(imageRect, snapRadius)) {
            final float offset = ImagePreviewEdge.BOTTOM.snapToRect(imageRect);
            ImagePreviewEdge.TOP.offset(offset);
        }
    }

    @Override
    void updateCropWindow(float x, float y, float targetAspectRatio, @NonNull RectF imageRect, float snapRadius) {
        updateCropWindow(x, y, imageRect, snapRadius);
    }
}