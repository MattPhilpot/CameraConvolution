package com.philpot.camera.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import android.support.annotation.NonNull;

import com.philpot.camera.CropBitmapTask;
import com.philpot.camera.fragment.CropTaskCallback;
import com.philpot.camera.fragment.ImageHolder;
import com.philpot.camera.R;
import com.philpot.camera.util.CropFileUtils;

public class ImagePreviewFrameLayout extends FrameLayout implements CropTaskCallback {
    private static final String TAG = ImagePreviewFrameLayout.class.getSimpleName();
    private Context mContext;
    private ImageHolder mImageHolder;
    private ImagePreviewCropView mPreviewView;

    public ImagePreviewFrameLayout(Context context) {
        super(context);
        initialize(context);
    }

    public ImagePreviewFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    protected void initialize(@NonNull Context context) {
        this.mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        mPreviewView = (ImagePreviewCropView) inflater.inflate(R.layout.previewview, this, false);
        this.addView(mPreviewView);
    }

    public void setImageHolder(ImageHolder holder) {
        this.mImageHolder = holder;
        mPreviewView.setImage(holder.getImage());
    }

    public void setCropEnabled(boolean crop) {
        if (mPreviewView != null) mPreviewView.setCropEnabled(crop);
    }

    public void processAndSetCropResult() {
        ImagePreviewCropView.Result result = mPreviewView.getCropResult();
        Uri destUri = CropFileUtils.createFileUri(getContext());
        new CropBitmapTask(this,
                mImageHolder.getBaseImageUri(),
                destUri,
                /*"png",*/
                result,
                new RectF(0, 0,
                        mImageHolder.getImage().getWidth(),
                        mImageHolder.getImage().getHeight())
                ).execute();
    }

    @Override
    public void cropFinished(Uri data) {
        if (data != null) {
            Bitmap bmp = BitmapFactory.decodeFile(data.getEncodedPath());
            mImageHolder.setBaseImage(bmp);
            mImageHolder.setImage(bmp);
            mPreviewView.setImage(bmp);
        }
    }

    @Override
    public Context getCallbackContext() {
        return mContext;
    }
}
