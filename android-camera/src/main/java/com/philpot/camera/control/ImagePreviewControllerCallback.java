package com.philpot.camera.control;

public interface ImagePreviewControllerCallback {
    void imageQualityCheckFinished(boolean isBlurry);
    void imageQualityCheckStarted();
}
