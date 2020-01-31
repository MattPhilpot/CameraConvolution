package com.philpot.camera.helper;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.philpot.camera.util.AspectRatioUtil;

class HorizontalHandleHelper extends HandleHelper {
    private final ImagePreviewEdge mEdge;


    HorizontalHandleHelper(ImagePreviewEdge edge) {
        super(edge, null);
        mEdge = edge;
    }

    @Override
    void updateCropWindow(float x, float y, float targetAspectRatio, @NonNull RectF imageRect, float snapRadius) {
        float left = ImagePreviewEdge.LEFT.getCoordinate();
        float right = ImagePreviewEdge.RIGHT.getCoordinate();
        final float targetWidth = AspectRatioUtil.getWidth(ImagePreviewEdge.getHeight(), targetAspectRatio);

        mEdge.adjustCoordinate(x, y, imageRect, snapRadius, targetAspectRatio);

        final float difference = targetWidth - ImagePreviewEdge.getWidth();
        final float halfDifference = difference / 2;
        left -= halfDifference;
        right += halfDifference;

        ImagePreviewEdge.LEFT.setCoordinate(left);
        ImagePreviewEdge.RIGHT.setCoordinate(right);

        if (ImagePreviewEdge.LEFT.isOutsideMargin(imageRect, snapRadius) && mEdge.isNewRectangleInBounds(ImagePreviewEdge.LEFT, imageRect, targetAspectRatio)) {
            final float offset = ImagePreviewEdge.LEFT.snapToRect(imageRect);
            ImagePreviewEdge.RIGHT.offset(-offset);
            mEdge.adjustCoordinate(targetAspectRatio);
        }

        if (ImagePreviewEdge.RIGHT.isOutsideMargin(imageRect, snapRadius) && mEdge.isNewRectangleInBounds(ImagePreviewEdge.RIGHT, imageRect, targetAspectRatio)) {
            final float offset = ImagePreviewEdge.RIGHT.snapToRect(imageRect);
            ImagePreviewEdge.LEFT.offset(-offset);
            mEdge.adjustCoordinate(targetAspectRatio);
        }
    }
}