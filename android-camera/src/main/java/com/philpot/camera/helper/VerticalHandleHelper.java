package com.philpot.camera.helper;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.philpot.camera.util.AspectRatioUtil;


class VerticalHandleHelper extends HandleHelper {

    private final ImagePreviewEdge mEdge;

    VerticalHandleHelper(ImagePreviewEdge edge) {
        super(null, edge);
        mEdge = edge;
    }

    @Override
    void updateCropWindow(float x, float y, float targetAspectRatio, @NonNull RectF imageRect, float snapRadius) {
        float top = ImagePreviewEdge.TOP.getCoordinate();
        float bottom = ImagePreviewEdge.BOTTOM.getCoordinate();
        final float targetHeight = AspectRatioUtil.getHeight(ImagePreviewEdge.getWidth(), targetAspectRatio);

        mEdge.adjustCoordinate(x, y, imageRect, snapRadius, targetAspectRatio);

        final float difference = targetHeight - ImagePreviewEdge.getHeight();
        final float halfDifference = difference / 2;
        top -= halfDifference;
        bottom += halfDifference;

        ImagePreviewEdge.TOP.setCoordinate(top);
        ImagePreviewEdge.BOTTOM.setCoordinate(bottom);

        if (ImagePreviewEdge.TOP.isOutsideMargin(imageRect, snapRadius) && mEdge.isNewRectangleInBounds(ImagePreviewEdge.TOP, imageRect, targetAspectRatio)) {
            final float offset = ImagePreviewEdge.TOP.snapToRect(imageRect);
            ImagePreviewEdge.BOTTOM.offset(-offset);
            mEdge.adjustCoordinate(targetAspectRatio);
        }
        if (ImagePreviewEdge.BOTTOM.isOutsideMargin(imageRect, snapRadius) && mEdge.isNewRectangleInBounds(ImagePreviewEdge.BOTTOM, imageRect, targetAspectRatio)) {
            final float offset = ImagePreviewEdge.BOTTOM.snapToRect(imageRect);
            ImagePreviewEdge.TOP.offset(-offset);
            mEdge.adjustCoordinate(targetAspectRatio);
        }
    }
}