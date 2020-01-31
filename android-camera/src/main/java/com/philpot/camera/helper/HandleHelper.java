package com.philpot.camera.helper;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.philpot.camera.util.AspectRatioUtil;

abstract class HandleHelper {
    private static final float UNFIXED_ASPECT_RATIO_CONSTANT = 1;
    private final ImagePreviewEdge mHorizontalEdge;
    private final ImagePreviewEdge mVerticalEdge;
    private final ImagePreviewEdgePair mActiveEdges;

    HandleHelper(ImagePreviewEdge horizontalEdge, ImagePreviewEdge verticalEdge) {
        mHorizontalEdge = horizontalEdge;
        mVerticalEdge = verticalEdge;
        mActiveEdges = new ImagePreviewEdgePair(mHorizontalEdge, mVerticalEdge);
    }

    void updateCropWindow(float x, float y, @NonNull RectF imageRect, float snapRadius) {

        final ImagePreviewEdgePair activeEdges = getActiveEdges();
        final ImagePreviewEdge primaryEdge = activeEdges.primary;
        final ImagePreviewEdge secondaryEdge = activeEdges.secondary;

        if (primaryEdge != null) primaryEdge.adjustCoordinate(x, y, imageRect, snapRadius, UNFIXED_ASPECT_RATIO_CONSTANT);

        if (secondaryEdge != null) secondaryEdge.adjustCoordinate(x, y, imageRect, snapRadius, UNFIXED_ASPECT_RATIO_CONSTANT);
    }

    abstract void updateCropWindow(float x,
                                   float y,
                                   float targetAspectRatio,
                                   @NonNull RectF imageRect,
                                   float snapRadius);

    private ImagePreviewEdgePair getActiveEdges() {
        return mActiveEdges;
    }

    ImagePreviewEdgePair getActiveEdges(float x, float y, float targetAspectRatio) {

        // Calculate the aspect ratio if this handle were dragged to the given x-y coordinate.
        final float potentialAspectRatio = getAspectRatio(x, y);

        // If the touched point is wider than the aspect ratio, then x is the determining side. Else, y is the determining side.
        if (potentialAspectRatio > targetAspectRatio) {
            mActiveEdges.primary = mVerticalEdge;
            mActiveEdges.secondary = mHorizontalEdge;
        } else {
            mActiveEdges.primary = mHorizontalEdge;
            mActiveEdges.secondary = mVerticalEdge;
        }
        return mActiveEdges;
    }

    private float getAspectRatio(float x, float y) {
        final float left = (mVerticalEdge == ImagePreviewEdge.LEFT) ? x : ImagePreviewEdge.LEFT.getCoordinate();
        final float top = (mHorizontalEdge == ImagePreviewEdge.TOP) ? y : ImagePreviewEdge.TOP.getCoordinate();
        final float right = (mVerticalEdge == ImagePreviewEdge.RIGHT) ? x : ImagePreviewEdge.RIGHT.getCoordinate();
        final float bottom = (mHorizontalEdge == ImagePreviewEdge.BOTTOM) ? y : ImagePreviewEdge.BOTTOM.getCoordinate();

        return AspectRatioUtil.calculateAspectRatio(left, top, right, bottom);
    }
}
