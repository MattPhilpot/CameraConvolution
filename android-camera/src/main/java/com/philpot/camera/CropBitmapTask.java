package com.philpot.camera;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.philpot.camera.fragment.CropTaskCallback;
import com.philpot.camera.util.CropFileUtils;
import com.philpot.camera.util.CropMath;
import com.philpot.camera.views.ImagePreviewCropView;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CropBitmapTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = CropBitmapTask.class.getSimpleName();

    private CropTaskCallback mCropTaskCallback = null;
    private ImagePreviewCropView.Result mResult = null;
    private RectF mOriginalBounds = null;
    private InputStream mInStream = null;
    private OutputStream mOutStream = null;
    private String mOutputFormat = null;
    private Uri mOutUri = null;
    private Uri mInUri = null;
    private int mOutputX = 0;
    private int mOutputY = 0;

    public CropBitmapTask(CropTaskCallback callback, Uri sourceUri, Uri destUri, ImagePreviewCropView.Result result, RectF originalBounds) {
        //this.mContext = context;
        //mOutputFormat = outputFormat;
        mCropTaskCallback = callback;
        mOutputFormat = "png";
        mOutStream = null;
        mOutUri = destUri;
        mInUri = sourceUri;
        mOutputX = (int)result.displayCropRect.width();
        mOutputY = (int)result.displayCropRect.height();
        mResult = result;
        mOriginalBounds = originalBounds;

        try {
            mOutStream = callback.getCallbackContext().getContentResolver().openOutputStream(mOutUri);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "cannot make file: " + mInUri.toString(), e);
            e.printStackTrace();
        }

        try {
            mInStream = callback.getCallbackContext().getContentResolver().openInputStream(mInUri);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "cannot read file: " + mInUri.toString(), e);
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected Boolean doInBackground(Void... params) {
        RectF returnRect = new RectF(0, 0, mOutputX, mOutputY);
        Bitmap canvasBitmap = Bitmap.createBitmap((int) returnRect.width(), (int) returnRect.height(), Bitmap.Config.ARGB_8888);
        canvasBitmap.eraseColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        Canvas canvas = new Canvas(canvasBitmap);
        Bitmap cropBitmap;
        if (mResult != null) {
            RectF trueCrop = CropMath.getScaledCropBounds(mResult.rawIntersectionRect, mResult.rawImageRect, mOriginalBounds);
            if (trueCrop == null) {
                Log.w(TAG, "cannot find crop for full size image");
                return false;
            }

            Rect roundedTrueCrop = new Rect();
            trueCrop.roundOut(roundedTrueCrop);
            if (roundedTrueCrop.width() <= 0 || roundedTrueCrop.height() <= 0) {
                Log.w(TAG, "crop has bad values for full size image");
                return false;
            }

            BitmapRegionDecoder decoder;
            try {
                decoder = BitmapRegionDecoder.newInstance(mInStream, true);
            } catch (IOException e) {
                Log.w(TAG, "cannot open region decoder for file: " + mInUri.toString(), e);
                return false;
            }

            cropBitmap = null;
            if (decoder != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                if (Build.VERSION.SDK_INT >= 11) {
                    options.inMutable = true;
                }

                if (mOutputX != 0 && mOutputY != 0) {
                    options.inSampleSize = CropMath.calculateInSampleSize(roundedTrueCrop.right - roundedTrueCrop.left, roundedTrueCrop.bottom - roundedTrueCrop.top, mOutputX, mOutputY);
                }
                cropBitmap = decoder.decodeRegion(roundedTrueCrop, options);
                decoder.recycle();
            }

            if (cropBitmap == null) {
                Log.w(TAG, "cannot decode file: " + mInUri.toString());
                return false;
            }

            Matrix drawMatrix = new Matrix();
            RectF cropRect = new RectF(0, 0, cropBitmap.getWidth(), cropBitmap.getHeight());
            drawMatrix.setRectToRect(cropRect, mResult.rawIntersectionRect, Matrix.ScaleToFit.FILL);
            drawMatrix.postConcat(mResult.displayImageMatrix);
            Matrix m = new Matrix();
            m.setRectToRect(mResult.displayCropRect, returnRect, Matrix.ScaleToFit.FILL);
            drawMatrix.postConcat(m);
            canvas.drawBitmap(cropBitmap, drawMatrix, paint);
        }
        cropBitmap = canvasBitmap;

        Bitmap.CompressFormat cf = CropFileUtils.convertExtensionToCompressFormat(CropFileUtils.getImageFileExtension(mOutputFormat));

        if (mOutStream == null || !cropBitmap.compress(cf, 90, mOutStream)) {
            Log.w(TAG, "failed to compress bitmap to file: " + mOutUri.toString());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        closeStream(mOutStream);
        closeStream(mInStream);
        mCropTaskCallback.cropFinished(mOutUri);
    }

    private void closeStream(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException t) {
                Log.w(TAG, "Closeable stream error", t);
            }
        }
    }
}