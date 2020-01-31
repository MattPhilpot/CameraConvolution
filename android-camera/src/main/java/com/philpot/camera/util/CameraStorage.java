package com.philpot.camera.util;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraStorage {
    private static final String TAG = CameraStorage.class.getSimpleName();

    private File getPicturesDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }

    public File saveBitmap(Bitmap image, String fileName, File defaultDir) {
        File parentPath;
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            parentPath = getPicturesDirectory();
        } else {
            parentPath = defaultDir;
        }

        File file = new File(parentPath, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch(Exception e) {
            Log.e(TAG, "failed compressing bitmap", e);
        } finally {
            try {
                if (out != null) out.close();
            } catch(IOException e) {
                Log.e(TAG, "failed closing FileOutputStream", e);
            }
        }

        return file;
    }

    private boolean isExternalStorageReadable() {
        boolean retVal = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            retVal = true;
        }

        return retVal;
    }

    private boolean isExternalStorageWritable() {
        boolean retVal = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            retVal = true;
        }

        return retVal;
    }
}
