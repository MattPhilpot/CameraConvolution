package com.philpot.camera.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

public class CropFileUtils {
    private static final String TAG = CropFileUtils.class.getSimpleName();

    public static Uri createFileUri(Context context) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getDiskCacheDir(context, "image");

            if (!storageDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                storageDir.mkdirs();
            }
            File file = File.createTempFile(imageFileName, ".jpg", storageDir);
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //noinspection ConstantConditions
            cachePath = context.getExternalCacheDir().getPath();
        }

        if (TextUtils.isEmpty(cachePath)) {
            cachePath = context.getCacheDir().getPath();
        }

        return new File(cachePath + File.separator + uniqueName);
    }

    public static String getImageFileExtension(String requestFormat) {
        String outputFormat = (requestFormat == null) ? "jpg" : requestFormat;
        outputFormat = outputFormat.toLowerCase(Locale.ENGLISH);
        return (outputFormat.equals("png") || outputFormat.equals("gif")) ? "png" : "jpg"; // We don't support gif compression
    }

    public static CompressFormat convertExtensionToCompressFormat(String extension) {
        return extension.equals("png") ? CompressFormat.PNG : CompressFormat.JPEG;
    }
}