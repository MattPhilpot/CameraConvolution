package com.philpot.camera.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class VerticalLabelView extends View {
    private TextPaint mTextPaint;
    private String mText;
    private int mAscent;
    private boolean mRotate;
    private Rect text_bounds = new Rect();

    final static int DEFAULT_TEXT_SIZE = 50;

    public VerticalLabelView(Context context) {
        super(context);
        initLabelView();
    }

    public VerticalLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLabelView();
    }

    public void setMessage(String message) {

        setText(message);
    }

    private final void initLabelView() {
        mRotate = false;
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DEFAULT_TEXT_SIZE);
        mTextPaint.setColor(0xFFFFFFFF);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        setPadding(3, 3, 3, 3);
    }

    public void setText(String text) {
        mText = text;
        requestLayout();
        invalidate();
    }

    public void setTextSize(int size) {
        mTextPaint.setTextSize(size);
        requestLayout();
        invalidate();
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setRotate(boolean rotate) {
        this.mRotate = rotate;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mTextPaint.getTextBounds(mText, 0, mText.length(), text_bounds);
        setMeasuredDimension(
                measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = text_bounds.height() + getPaddingLeft() + getPaddingRight();

            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) mTextPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = text_bounds.width() + getPaddingTop() + getPaddingBottom();

            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float text_horizontally_centered_origin_x = (getPaddingLeft() + text_bounds.width()/2f);
        float text_horizontally_centered_origin_y = (getPaddingTop() - mAscent);
        canvas.translate(text_horizontally_centered_origin_y, text_horizontally_centered_origin_x);
        if (mRotate) canvas.rotate(90);

        Rect textBounds = new Rect();
        mTextPaint.getTextBounds(mText, 0, mText.length(), textBounds);

        //canvas.drawRect(100, 100, 100 + mTextPaint.measureText(mText), 100, mTextPaint);
        canvas.drawText(mText, 0, textBounds.height() - textBounds.bottom, mTextPaint);
    }
}