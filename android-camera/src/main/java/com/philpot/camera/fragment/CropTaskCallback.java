package com.philpot.camera.fragment;

import android.content.Context;
import android.net.Uri;

public interface CropTaskCallback {
    void cropFinished(Uri data);

    Context getCallbackContext();
}
