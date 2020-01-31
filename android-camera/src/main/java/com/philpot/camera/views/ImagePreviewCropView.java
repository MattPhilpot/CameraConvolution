package com.philpot.camera.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.support.annotation.NonNull;

import com.philpot.camera.util.CropMath;
import com.philpot.camera.helper.Handle;
import com.philpot.camera.helper.ImagePreviewEdge;
import com.philpot.camera.R;
import com.philpot.camera.util.HandleUtil;

public class ImagePreviewCropView extends View {
    private static final String TAG = ImagePreviewCropView.class.getName();
    private Context mContext;
    private Bitmap mImage;

    private RectF mCanvasRect;
    private RectF mScreenInCanvas;
    private RectF mCropInScreen;

    private RectF mCropRect;

    private int mRotation = 0;
    private Matrix mInitialDisplayImageMatrix;
    private Matrix mDisplayImageMatrix;
    private Matrix mDisplayCropMatrix;
    private Matrix mImageCropInverse;
    private boolean mDirty = false;
    private float mLastX = 0;
    private float mLastY = 0;
    private PointF mEventCenter = new PointF();
    private float mOriginalDistance = 1f;
    private float mOriginalDegrees = 0;
    private Matrix mSavedImageMatrix = new Matrix();
    private Matrix mSavedImageCropInverse = new Matrix();
    private static final int TOUCH_MODE_NONE = 0;
    private static final int TOUCH_MODE_DRAG = 1;
    private static final int TOUCH_MODE_ZOOM = 2;
    private int mTouchMode = TOUCH_MODE_NONE;
    private Paint mBorderPaint;
    private Paint mGuidelinePaint;
    private Paint mCornerPaint;
    private Paint mSurroundingAreaOverlayPaint;
    private float mHandleRadius;
    private float mSnapRadius;
    private float mCornerThickness;
    private float mBorderThickness;
    private float mCornerLength;

    @NonNull
    private RectF mBitmapRect = new RectF();

    @NonNull
    private PointF mTouchOffset = new PointF();
    private Handle mPressedHandle;
    private boolean mDrawCropGrid = false;


    public ImagePreviewCropView(Context context) {
        super(context);
        initialize(context);
    }

    public ImagePreviewCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ImagePreviewCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(@NonNull Context context) {
        this.mContext = context;
        mRotation = 0;
        mDrawCropGrid = false;

        final Resources resources = context.getResources();
        mBorderPaint = getNewBorderPaint(resources);
        mGuidelinePaint = getNewGuidelinePaint(resources);
        mSurroundingAreaOverlayPaint = getNewSurroundingAreaOverlayPaint(resources);
        mCornerPaint = getNewCornerPaint(resources);

        mHandleRadius = resources.getDimension(R.dimen.target_radius);
        mSnapRadius = resources.getDimension(R.dimen.snap_radius);
        mBorderThickness = resources.getDimension(R.dimen.border_thickness);
        mCornerThickness = resources.getDimension(R.dimen.corner_thickness);
        mCornerLength = resources.getDimension(R.dimen.corner_length);
    }

    private Paint getNewBorderPaint(@NonNull Resources resources) {
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimension(R.dimen.border_thickness));
        paint.setColor(getColor(resources, R.color.border));
        return paint;
    }

    private Paint getNewGuidelinePaint(@NonNull Resources resources) {
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimension(R.dimen.guideline_thickness));
        paint.setColor(getColor(resources, R.color.guideline));
        return paint;
    }

    private Paint getNewSurroundingAreaOverlayPaint(@NonNull Resources resources) {
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getColor(resources, R.color.surrounding_area));
        return paint;
    }

    private Paint getNewCornerPaint(@NonNull Resources resources) {
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimension(R.dimen.corner_thickness));
        paint.setColor(getColor(resources, R.color.corner));
        return paint;
    }

    @SuppressWarnings("deprecation")
    private int getColor(@NonNull Resources resources, int color) {
        if (Build.VERSION.SDK_INT >= 23) {
            return ContextCompat.getColor(mContext, color);
        } else {
            return resources.getColor(color);
        }
    }

    public void setImage(Bitmap bmp) {
        mCropRect = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
        this.mImage = bmp;
        mDirty = true;
        invalidate();
    }

    public void setCropEnabled(boolean cropEnabled) {
        this.mDrawCropGrid = cropEnabled;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mBitmapRect = getBitmapRect();
        initCropWindow(mBitmapRect);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mImage == null) return;

        if (mDirty) {
            mDisplayImageMatrix = null;
            mDisplayCropMatrix = null;
            mDirty = false;
        }

        if (mCanvasRect == null || (mCanvasRect.width() != canvas.getWidth() && mCanvasRect.height() != canvas.getHeight())) {
            mCanvasRect = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
            mScreenInCanvas = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
            //mScreenInCanvas.inset(0, 0);
        }

        if (mDisplayImageMatrix == null || mDisplayCropMatrix == null) {
            mDisplayImageMatrix = new Matrix();
            mDisplayImageMatrix.reset();
            if (!CropMath.setImageToScreenMatrix(mDisplayImageMatrix, mCropRect, mScreenInCanvas, mRotation)) {
                Log.e(TAG, "failed to get image matrix");
                mDisplayImageMatrix = null;
                return;
            }
            mInitialDisplayImageMatrix = new Matrix(mDisplayImageMatrix);
            mCropInScreen = (mCropInScreen == null ? new RectF() : mCropInScreen);
            mCropInScreen.set(generateCropRect());
            mDisplayCropMatrix = new Matrix();
            mDisplayCropMatrix.reset();
            if (!CropMath.setCropToScreenMatrix(mDisplayCropMatrix, mCropInScreen, mScreenInCanvas)) {
                Log.e(TAG, "failed to get crop matrix");
                mDisplayCropMatrix = null;
                return;
            }
            mDisplayCropMatrix.mapRect(mCropInScreen);
            mImageCropInverse = new Matrix();
        }

        canvas.drawBitmap(mImage, mDisplayImageMatrix, null);

        if (this.mDrawCropGrid) {
            drawDarkenedSurroundingArea(canvas);
            drawGuidelines(canvas);
            drawBorder(canvas);
            drawCorners(canvas);
        }
    }

    private void drawDarkenedSurroundingArea(@NonNull Canvas canvas) {
        final RectF bitmapRect = mBitmapRect;

        final float left = ImagePreviewEdge.LEFT.getCoordinate();
        final float top = ImagePreviewEdge.TOP.getCoordinate();
        final float right = ImagePreviewEdge.RIGHT.getCoordinate();
        final float bottom = ImagePreviewEdge.BOTTOM.getCoordinate();

        canvas.drawRect(bitmapRect.left, bitmapRect.top, bitmapRect.right, top, mSurroundingAreaOverlayPaint);
        canvas.drawRect(bitmapRect.left, bottom, bitmapRect.right, bitmapRect.bottom, mSurroundingAreaOverlayPaint);
        canvas.drawRect(bitmapRect.left, top, left, bottom, mSurroundingAreaOverlayPaint);
        canvas.drawRect(right, top, bitmapRect.right, bottom, mSurroundingAreaOverlayPaint);
    }

    private void drawGuidelines(@NonNull Canvas canvas) {
        final float left = ImagePreviewEdge.LEFT.getCoordinate();
        final float top = ImagePreviewEdge.TOP.getCoordinate();
        final float right = ImagePreviewEdge.RIGHT.getCoordinate();
        final float bottom = ImagePreviewEdge.BOTTOM.getCoordinate();

        final float oneThirdCropWidth = ImagePreviewEdge.getWidth() / 3;

        final float x1 = left + oneThirdCropWidth;
        canvas.drawLine(x1, top, x1, bottom, mGuidelinePaint);
        final float x2 = right - oneThirdCropWidth;
        canvas.drawLine(x2, top, x2, bottom, mGuidelinePaint);

        final float oneThirdCropHeight = ImagePreviewEdge.getHeight() / 3;

        final float y1 = top + oneThirdCropHeight;
        canvas.drawLine(left, y1, right, y1, mGuidelinePaint);
        final float y2 = bottom - oneThirdCropHeight;
        canvas.drawLine(left, y2, right, y2, mGuidelinePaint);
    }

    private void drawBorder(@NonNull Canvas canvas) {
        canvas.drawRect(ImagePreviewEdge.LEFT.getCoordinate(), ImagePreviewEdge.TOP.getCoordinate(), ImagePreviewEdge.RIGHT.getCoordinate(), ImagePreviewEdge.BOTTOM.getCoordinate(), mBorderPaint);
    }

    private void drawCorners(@NonNull Canvas canvas) {
        final float left = ImagePreviewEdge.LEFT.getCoordinate();
        final float top = ImagePreviewEdge.TOP.getCoordinate();
        final float right = ImagePreviewEdge.RIGHT.getCoordinate();
        final float bottom = ImagePreviewEdge.BOTTOM.getCoordinate();

        final float lateralOffset = (mCornerThickness - mBorderThickness) / 2f;
        final float startOffset = mCornerThickness - (mBorderThickness / 2f);

        canvas.drawLine(left - lateralOffset, top - startOffset, left - lateralOffset, top + mCornerLength, mCornerPaint); // Top-left corner: left side
        canvas.drawLine(left - startOffset, top - lateralOffset, left + mCornerLength, top - lateralOffset, mCornerPaint); // Top-left corner: top side
        canvas.drawLine(right + lateralOffset, top - startOffset, right + lateralOffset, top + mCornerLength, mCornerPaint); // Top-right corner: right side
        canvas.drawLine(right + startOffset, top - lateralOffset, right - mCornerLength, top - lateralOffset, mCornerPaint); // Top-right corner: top side
        canvas.drawLine(left - lateralOffset, bottom + startOffset, left - lateralOffset, bottom - mCornerLength, mCornerPaint); // Bottom-left corner: left side
        canvas.drawLine(left - startOffset, bottom + lateralOffset, left + mCornerLength, bottom + lateralOffset, mCornerPaint); // Bottom-left corner: bottom side
        canvas.drawLine(right + lateralOffset, bottom + startOffset, right + lateralOffset, bottom - mCornerLength, mCornerPaint); // Bottom-right corner: right side
        canvas.drawLine(right + startOffset, bottom + lateralOffset, right - mCornerLength, bottom + lateralOffset, mCornerPaint); // Bottom-right corner: bottom side
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDisplayImageMatrix == null || mDisplayCropMatrix == null) {
            return true;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouchMode = TOUCH_MODE_DRAG;
                mLastX = event.getX();
                mLastY = event.getY();
                mSavedImageMatrix.set(mDisplayImageMatrix);
                mSavedImageCropInverse.set(mImageCropInverse);
                onActionDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchMode = TOUCH_MODE_ZOOM;
                mSavedImageMatrix.set(mDisplayImageMatrix);
                mSavedImageCropInverse.set(mImageCropInverse);
                mOriginalDistance = getDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                mOriginalDegrees = getDegrees(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                getCenter(mEventCenter, event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode == TOUCH_MODE_ZOOM) {
                    mDisplayImageMatrix.set(mSavedImageMatrix);
                    mImageCropInverse.set(mSavedImageCropInverse);
                    float degrees = getDegrees(event.getX(0), event.getY(0), event.getX(1), event.getY(1)) - mOriginalDegrees;
                    float distance = getDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    float scale = distance / mOriginalDistance;
                    mDisplayImageMatrix.postScale(scale, scale, mEventCenter.x, mEventCenter.y);
                    mImageCropInverse.postScale(scale, scale, mEventCenter.x, mEventCenter.y);
                    mDisplayImageMatrix.postRotate(degrees % 360, mEventCenter.x, mEventCenter.y);
                    mImageCropInverse.postRotate((degrees) % 360, mEventCenter.x, mEventCenter.y);
                    invalidate();
                } else if (mTouchMode == TOUCH_MODE_DRAG) {
                    if (onActionMove(event.getX(), event.getY())) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        mDisplayImageMatrix.set(mSavedImageMatrix);
                        mImageCropInverse.set(mSavedImageCropInverse);
                        mDisplayImageMatrix.postTranslate(event.getX() - mLastX, event.getY() - mLastY);
                        mImageCropInverse.postTranslate((event.getX() - mLastX), (event.getY() - mLastY));
                        invalidate();
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                mTouchMode = TOUCH_MODE_NONE;
                break;
        }
        return true;
    }

    private void getCenter(PointF point, MotionEvent event) {
        float x = mLastX + event.getX(1);
        float y = mLastY + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private float getDistance(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float)Math.sqrt(x * x + y * y);
    }

    private float getDegrees(float x1, float y1, float x2, float y2) {
        double deltaX = x1 - x2;
        double deltaY = y1 - y2;
        double radians = Math.atan2(deltaY, deltaX);
        return (float) Math.toDegrees(radians);
    }

    private boolean onActionDown(float x, float y) {
        final float left = ImagePreviewEdge.LEFT.getCoordinate();
        final float top = ImagePreviewEdge.TOP.getCoordinate();
        final float right = ImagePreviewEdge.RIGHT.getCoordinate();
        final float bottom = ImagePreviewEdge.BOTTOM.getCoordinate();

        mPressedHandle = HandleUtil.getPressedHandle(x, y, left, top, right, bottom, mHandleRadius);
        if (mPressedHandle != null) {
            mTouchOffset = mPressedHandle.getOffset(x, y, left, top, right, bottom);
            invalidate();
        }
        return mPressedHandle != null;
    }

    private boolean onActionMove(float x, float y) {
        if (mPressedHandle != null && mDrawCropGrid) {
            x += mTouchOffset.x;
            y += mTouchOffset.y;
            mPressedHandle.updateCropWindow(x, y, mBitmapRect, mSnapRadius);
            invalidate();
            return true;
        }
        return false;
    }

    private RectF getBitmapRect() {
        if (mImage == null) return new RectF();
        return new RectF(0, 0, Math.min((Math.round(mImage.getWidth())), getWidth()), Math.min((Math.round(mImage.getHeight())), getHeight()));
    }

    private void initCropWindow(@NonNull RectF bitmapRect) {
        final float horizontalPadding = 0.1f * bitmapRect.width();
        final float verticalPadding = 0.1f * bitmapRect.height();

        ImagePreviewEdge.LEFT.setCoordinate(bitmapRect.left + horizontalPadding);
        ImagePreviewEdge.TOP.setCoordinate(bitmapRect.top + verticalPadding);
        ImagePreviewEdge.RIGHT.setCoordinate(bitmapRect.right - horizontalPadding);
        ImagePreviewEdge.BOTTOM.setCoordinate(bitmapRect.bottom - verticalPadding);
    }

    public Result getCropResult() {
        Result result = new Result();
        RectF imageRect = mCropRect;
        RectF cropRect = generateCropRect();
        RectF invertedImageRect = new RectF();
        RectF invertedCropRect = new RectF();
        Matrix tempMatrix = new Matrix();
        float newPoints[] = new float[9];
        mDisplayCropMatrix.getValues(newPoints);
        newPoints[2] = ImagePreviewEdge.LEFT.getCoordinate();
        newPoints[5] = ImagePreviewEdge.TOP.getCoordinate();
        mDisplayCropMatrix.setValues(newPoints);

        mInitialDisplayImageMatrix.mapRect(invertedImageRect, imageRect);
        tempMatrix.reset();
        if (!mImageCropInverse.invert(tempMatrix)) {
            return null;
        }
        tempMatrix.preConcat(mDisplayCropMatrix);
        tempMatrix.mapRect(invertedCropRect, cropRect);

        float[] cropCorners = CropMath.getCornersFromRect(invertedCropRect);
        RectF unrotatedCropRect = CropMath.trapToRect(cropCorners);

        float[] unrotatedCropCorners = CropMath.getCornersFromRect(unrotatedCropRect);
        CropMath.getEdgePoints(invertedImageRect, unrotatedCropCorners);
        RectF intersectionRect = CropMath.trapToRect(unrotatedCropCorners);

        if (intersectionRect.width() == 0 || intersectionRect.height() == 0) {
            return null;
        }

        tempMatrix = new Matrix();
        tempMatrix.reset();
        if (!mInitialDisplayImageMatrix.invert(tempMatrix)) {
            return null;
        }
        RectF rawIntersectionRect = new RectF();
        tempMatrix.mapRect(rawIntersectionRect, intersectionRect);

        RectF displayCropRect = new RectF();
        mDisplayCropMatrix.mapRect(displayCropRect, cropRect);

        result.rawImageRect = imageRect;
        result.rawIntersectionRect = rawIntersectionRect;
        result.displayImageMatrix = mDisplayImageMatrix;
        result.displayCropRect = displayCropRect;

        Log.d(TAG, "rawImageRect: " + result.rawImageRect.toString() + ",rawIntersectionRect: " + result.rawIntersectionRect.toString());
        return result;
    }

    public static class Result {
        public RectF displayCropRect;
        public RectF rawImageRect;
        public RectF rawIntersectionRect;
        public Matrix displayImageMatrix;
    }

    private RectF generateCropRect() {
        return new RectF(0, 0, ImagePreviewEdge.getWidth(), ImagePreviewEdge.getHeight());
    }
}
