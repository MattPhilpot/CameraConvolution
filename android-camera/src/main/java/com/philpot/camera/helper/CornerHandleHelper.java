package com.philpot.camera.helper;

import android.graphics.RectF;
import android.support.annotation.NonNull;

class CornerHandleHelper extends HandleHelper {

    CornerHandleHelper(ImagePreviewEdge horizontalEdge, ImagePreviewEdge verticalEdge) {
        super(horizontalEdge, verticalEdge);
    }

    @Override
    void updateCropWindow(float x, float y, float targetAspectRatio, @NonNull RectF imageRect, float snapRadius) {
        final ImagePreviewEdgePair activeEdges = getActiveEdges(x, y, targetAspectRatio);
        final ImagePreviewEdge primaryEdge = activeEdges.primary;
        final ImagePreviewEdge secondaryEdge = activeEdges.secondary;

        primaryEdge.adjustCoordinate(x, y, imageRect, snapRadius, targetAspectRatio);
        secondaryEdge.adjustCoordinate(targetAspectRatio);

        if (secondaryEdge.isOutsideMargin(imageRect, snapRadius)) {
            secondaryEdge.snapToRect(imageRect);
            primaryEdge.adjustCoordinate(targetAspectRatio);
        }
    }
}