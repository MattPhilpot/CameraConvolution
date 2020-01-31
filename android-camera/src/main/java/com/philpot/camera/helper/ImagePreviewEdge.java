package com.philpot.camera.helper;


import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.philpot.camera.util.AspectRatioUtil;

public enum ImagePreviewEdge {

    LEFT,
    TOP,
    RIGHT,
    BOTTOM;

    private static final int MIN_CROP_LENGTH_PX = 40;

    private float mCoordinate;

    public void setCoordinate(float coordinate) {
        mCoordinate = coordinate;
    }

    public void offset(float distance) {
        mCoordinate += distance;
    }

    public float getCoordinate() {
        return mCoordinate;
    }

    public void adjustCoordinate(float x, float y, @NonNull RectF imageRect, float imageSnapRadius, float aspectRatio) {

        switch (this) {
            case LEFT:
                mCoordinate = adjustLeft(x, imageRect, imageSnapRadius, aspectRatio);
                break;
            case TOP:
                mCoordinate = adjustTop(y, imageRect, imageSnapRadius, aspectRatio);
                break;
            case RIGHT:
                mCoordinate = adjustRight(x, imageRect, imageSnapRadius, aspectRatio);
                break;
            case BOTTOM:
                mCoordinate = adjustBottom(y, imageRect, imageSnapRadius, aspectRatio);
                break;
        }
    }

    public void adjustCoordinate(float aspectRatio) {

        final float left = ImagePreviewEdge.LEFT.getCoordinate();
        final float top = ImagePreviewEdge.TOP.getCoordinate();
        final float right = ImagePreviewEdge.RIGHT.getCoordinate();
        final float bottom = ImagePreviewEdge.BOTTOM.getCoordinate();

        switch (this) {
            case LEFT:
                mCoordinate = AspectRatioUtil.getLeft(top, right, bottom, aspectRatio);
                break;
            case TOP:
                mCoordinate = AspectRatioUtil.getTop(left, right, bottom, aspectRatio);
                break;
            case RIGHT:
                mCoordinate = AspectRatioUtil.getRight(left, top, bottom, aspectRatio);
                break;
            case BOTTOM:
                mCoordinate = AspectRatioUtil.getBottom(left, top, right, aspectRatio);
                break;
        }
    }

    public boolean isNewRectangleInBounds(@NonNull ImagePreviewEdge edge, @NonNull RectF imageRect, float aspectRatio) {
        final float offset = edge.snapOffset(imageRect);

        switch (this) {
            case LEFT:
                if (edge.equals(ImagePreviewEdge.TOP)) {
                    final float top = imageRect.top;
                    final float bottom = ImagePreviewEdge.BOTTOM.getCoordinate() - offset;
                    final float right = ImagePreviewEdge.RIGHT.getCoordinate();
                    final float left = AspectRatioUtil.getLeft(top, right, bottom, aspectRatio);
                    return isInsideBounds(top, left, bottom, right, imageRect);
                } else if (edge.equals(ImagePreviewEdge.BOTTOM)) {
                    final float bottom = imageRect.bottom;
                    final float top = ImagePreviewEdge.TOP.getCoordinate() - offset;
                    final float right = ImagePreviewEdge.RIGHT.getCoordinate();
                    final float left = AspectRatioUtil.getLeft(top, right, bottom, aspectRatio);
                    return isInsideBounds(top, left, bottom, right, imageRect);
                }
                break;
            case TOP:
                if (edge.equals(ImagePreviewEdge.LEFT)) {
                    final float left = imageRect.left;
                    final float right = ImagePreviewEdge.RIGHT.getCoordinate() - offset;
                    final float bottom = ImagePreviewEdge.BOTTOM.getCoordinate();
                    final float top = AspectRatioUtil.getTop(left, right, bottom, aspectRatio);
                    return isInsideBounds(top, left, bottom, right, imageRect);
                } else if (edge.equals(ImagePreviewEdge.RIGHT)) {
                    final float right = imageRect.right;
                    final float left = ImagePreviewEdge.LEFT.getCoordinate() - offset;
                    final float bottom = ImagePreviewEdge.BOTTOM.getCoordinate();
                    final float top = AspectRatioUtil.getTop(left, right, bottom, aspectRatio);
                    return isInsideBounds(top, left, bottom, right, imageRect);
                }
                break;
            case RIGHT:
                if (edge.equals(ImagePreviewEdge.TOP)) {
                    final float top = imageRect.top;
                    final float bottom = ImagePreviewEdge.BOTTOM.getCoordinate() - offset;
                    final float left = ImagePreviewEdge.LEFT.getCoordinate();
                    final float right = AspectRatioUtil.getRight(left, top, bottom, aspectRatio);
                    return isInsideBounds(top, left, bottom, right, imageRect);
                } else if (edge.equals(ImagePreviewEdge.BOTTOM)) {
                    final float bottom = imageRect.bottom;
                    final float top = ImagePreviewEdge.TOP.getCoordinate() - offset;
                    final float left = ImagePreviewEdge.LEFT.getCoordinate();
                    final float right = AspectRatioUtil.getRight(left, top, bottom, aspectRatio);
                    return isInsideBounds(top, left, bottom, right, imageRect);
                }
                break;
            case BOTTOM:
                if (edge.equals(ImagePreviewEdge.LEFT)) {
                    final float left = imageRect.left;
                    final float right = ImagePreviewEdge.RIGHT.getCoordinate() - offset;
                    final float top = ImagePreviewEdge.TOP.getCoordinate();
                    final float bottom = AspectRatioUtil.getBottom(left, top, right, aspectRatio);
                    return isInsideBounds(top, left, bottom, right, imageRect);
                } else if (edge.equals(ImagePreviewEdge.RIGHT)) {
                    final float right = imageRect.right;
                    final float left = ImagePreviewEdge.LEFT.getCoordinate() - offset;
                    final float top = ImagePreviewEdge.TOP.getCoordinate();
                    final float bottom = AspectRatioUtil.getBottom(left, top, right, aspectRatio);
                    return isInsideBounds(top, left, bottom, right, imageRect);
                }
                break;
        }
        return false;
    }

    private boolean isInsideBounds(float top, float left, float bottom, float right, @NonNull RectF imageRect) {
        return (top >= imageRect.top && left >= imageRect.left && bottom <= imageRect.bottom && right <= imageRect.right);
    }

    public float snapToRect(@NonNull RectF imageRect) {
        final float oldCoordinate = mCoordinate;

        switch (this) {
            case LEFT:
                mCoordinate = imageRect.left;
                break;
            case TOP:
                mCoordinate = imageRect.top;
                break;
            case RIGHT:
                mCoordinate = imageRect.right;
                break;
            case BOTTOM:
                mCoordinate = imageRect.bottom;
                break;
        }

        return mCoordinate - oldCoordinate;
    }

    private float snapOffset(@NonNull RectF imageRect) {
        final float oldCoordinate = mCoordinate;
        final float newCoordinate;

        switch (this) {
            case LEFT:
                newCoordinate = imageRect.left;
                break;
            case TOP:
                newCoordinate = imageRect.top;
                break;
            case RIGHT:
                newCoordinate = imageRect.right;
                break;
            default: // BOTTOM
                newCoordinate = imageRect.bottom;
                break;
        }

        return newCoordinate - oldCoordinate;
    }

    public static float getWidth() {
        return ImagePreviewEdge.RIGHT.getCoordinate() - ImagePreviewEdge.LEFT.getCoordinate();
    }

    public static float getHeight() {
        return ImagePreviewEdge.BOTTOM.getCoordinate() - ImagePreviewEdge.TOP.getCoordinate();
    }

    public boolean isOutsideMargin(@NonNull RectF rect, float margin) {
        switch (this) {
            case LEFT:
                return mCoordinate - rect.left < margin;
            case TOP:
                return mCoordinate - rect.top < margin;
            case RIGHT:
                return rect.right - mCoordinate < margin;
            default: // BOTTOM
                return rect.bottom - mCoordinate < margin;
        }
    }

    private static float adjustLeft(float x, @NonNull RectF imageRect, float imageSnapRadius, float aspectRatio) {
        final float resultX;

        if (x - imageRect.left < imageSnapRadius) {
            resultX = imageRect.left;
        } else {
            float resultXHoriz = Float.POSITIVE_INFINITY;
            float resultXVert = Float.POSITIVE_INFINITY;

            if (x >= ImagePreviewEdge.RIGHT.getCoordinate() - MIN_CROP_LENGTH_PX) {
                resultXHoriz = ImagePreviewEdge.RIGHT.getCoordinate() - MIN_CROP_LENGTH_PX;
            }
            if (((ImagePreviewEdge.RIGHT.getCoordinate() - x) / aspectRatio) <= MIN_CROP_LENGTH_PX) {
                resultXVert = ImagePreviewEdge.RIGHT.getCoordinate() - (MIN_CROP_LENGTH_PX * aspectRatio);
            }
            resultX = Math.min(x, Math.min(resultXHoriz, resultXVert));
        }
        return resultX;
    }

    private static float adjustRight(float x, @NonNull RectF imageRect, float imageSnapRadius, float aspectRatio) {
        final float resultX;

        if (imageRect.right - x < imageSnapRadius) {
            resultX = imageRect.right;
        } else {
            float resultXHoriz = Float.NEGATIVE_INFINITY;
            float resultXVert = Float.NEGATIVE_INFINITY;

            if (x <= ImagePreviewEdge.LEFT.getCoordinate() + MIN_CROP_LENGTH_PX) {
                resultXHoriz = ImagePreviewEdge.LEFT.getCoordinate() + MIN_CROP_LENGTH_PX;
            }
            if (((x - ImagePreviewEdge.LEFT.getCoordinate()) / aspectRatio) <= MIN_CROP_LENGTH_PX) {
                resultXVert = ImagePreviewEdge.LEFT.getCoordinate() + (MIN_CROP_LENGTH_PX * aspectRatio);
            }
            resultX = Math.max(x, Math.max(resultXHoriz, resultXVert));
        }
        return resultX;
    }

    private static float adjustTop(float y, @NonNull RectF imageRect, float imageSnapRadius, float aspectRatio) {
        final float resultY;

        if (y - imageRect.top < imageSnapRadius) {
            resultY = imageRect.top;
        } else {
            float resultYVert = Float.POSITIVE_INFINITY;
            float resultYHoriz = Float.POSITIVE_INFINITY;

            if (y >= ImagePreviewEdge.BOTTOM.getCoordinate() - MIN_CROP_LENGTH_PX) {
                resultYHoriz = ImagePreviewEdge.BOTTOM.getCoordinate() - MIN_CROP_LENGTH_PX;
            }
            if (((ImagePreviewEdge.BOTTOM.getCoordinate() - y) * aspectRatio) <= MIN_CROP_LENGTH_PX) {
                resultYVert = ImagePreviewEdge.BOTTOM.getCoordinate() - (MIN_CROP_LENGTH_PX / aspectRatio);
            }
            resultY = Math.min(y, Math.min(resultYHoriz, resultYVert));
        }
        return resultY;
    }

    private static float adjustBottom(float y, @NonNull RectF imageRect, float imageSnapRadius, float aspectRatio) {
        final float resultY;

        if (imageRect.bottom - y < imageSnapRadius) {
            resultY = imageRect.bottom;
        } else {
            float resultYVert = Float.NEGATIVE_INFINITY;
            float resultYHoriz = Float.NEGATIVE_INFINITY;

            if (y <= ImagePreviewEdge.TOP.getCoordinate() + MIN_CROP_LENGTH_PX) {
                resultYVert = ImagePreviewEdge.TOP.getCoordinate() + MIN_CROP_LENGTH_PX;
            }
            if (((y - ImagePreviewEdge.TOP.getCoordinate()) * aspectRatio) <= MIN_CROP_LENGTH_PX) {
                resultYHoriz = ImagePreviewEdge.TOP.getCoordinate() + (MIN_CROP_LENGTH_PX / aspectRatio);
            }
            resultY = Math.max(y, Math.max(resultYHoriz, resultYVert));
        }
        return resultY;
    }
}