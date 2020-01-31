package com.philpot.camera.fragment;

import android.graphics.Bitmap;
import android.net.Uri;

public interface ImageHolder {
    void setBaseImage(Bitmap bmp);
    Bitmap getImage();
    Bitmap getBaseImage();
    Uri getImageUri();
    Uri getBaseImageUri();
    void setImage(Bitmap img);
    void loadBitmap(byte[] data);
}
