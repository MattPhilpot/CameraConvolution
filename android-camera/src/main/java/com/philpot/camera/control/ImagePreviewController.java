package com.philpot.camera.control;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;

import com.philpot.camera.filter.ConvolutionFilter;
import com.philpot.camera.fragment.ImageEditor;
import com.philpot.camera.fragment.ImageHolder;


public class ImagePreviewController {
    private static final int LAP_THRESHOLD = -6118750;

    private ImageHolder mImageHolder;
    private ImageEditor mImageEditor;
    private ImagePreviewControllerCallback mControllerCallback;
    private boolean isGrayImagePreviewed = false;
    private boolean mIsBlurryCheckRunning = false;
    private boolean mIsImageBlurry = false;
    private boolean mFirstRun = true;


    public ImagePreviewController(ImageHolder holder, ImageEditor editor, ImagePreviewControllerCallback callback, boolean firstRun) {
        this.mImageHolder = holder;
        this.mImageEditor = editor;
        this.mControllerCallback = callback;
        this.mFirstRun = firstRun;
    }

    public ImageHolder getImageHolder() {
        return this.mImageHolder;
    }

    public ImageEditor getImageEditor() {
        return this.mImageEditor;
    }

    public void rotateImages(int degrees) {
        mImageHolder.setBaseImage(rotateImage(mImageHolder.getBaseImage(), degrees));
        mImageHolder.setImage(rotateImage(mImageHolder.getImage(), degrees));
    }

    private Bitmap rotateImage(Bitmap src, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(src , 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public void fixContrast(float contrastLevel) {
        if(isGrayImagePreviewed) {
            mImageHolder.setImage(mImageHolder.getBaseImage());
        } else {

            mImageHolder.setImage(ConvolutionFilter.enhanceBitmap(mImageHolder.getBaseImage(), contrastLevel));
        }
        isGrayImagePreviewed = !isGrayImagePreviewed;
    }

    public void adjustContrast(float contrastLevel) {
        mImageHolder.setImage(ConvolutionFilter.enhanceBitmap(mImageHolder.getBaseImage(), contrastLevel));
    }

    public void returnToCamera() {
        mImageEditor.takeImage();
    }

    public void saveImageAndFinish() {
        mImageEditor.returnImage();
    }

    public boolean checkImageQuality() {
        if(!mIsBlurryCheckRunning && mFirstRun) {
            mIsBlurryCheckRunning = true;
            mControllerCallback.imageQualityCheckStarted();

            new Thread(new Runnable() {
                public void run() {
                    mIsImageBlurry = checkIsImageBlurry();
                    blurryHandler.post(blurryRunnable);
                    mFirstRun = false;
                    mIsBlurryCheckRunning = false;
                }
            }).start();
        }
        return mIsBlurryCheckRunning;
    }

    private boolean checkIsImageBlurry() {
        Bitmap bmp = mImageHolder.getImage().copy(mImageHolder.getImage().getConfig(), true);
        bmp = ConvolutionFilter.applyPrewittConvolution(bmp);
        bmp = ConvolutionFilter.enhanceBitmap(bmp, 2);
        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        for (int pixel : pixels) {
            if (pixel > LAP_THRESHOLD) return false;
        }
        return true;
    }

    private final Handler blurryHandler = new Handler();

    private final Runnable blurryRunnable = new Runnable() {
        public void run() {
            mControllerCallback.imageQualityCheckFinished(isImageBlurry());
        }
    };

    public boolean isImageBlurry() {
        return this.mIsImageBlurry;
    }

    public boolean getFirstRun() {
        return this.mFirstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.mFirstRun = firstRun;
    }

}
