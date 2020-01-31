package com.philpot.camera.fragment;

import android.hardware.Camera;

@SuppressWarnings("deprecation")
public enum CameraFlashMode {
    Auto(Camera.Parameters.FLASH_MODE_AUTO),
    Off(Camera.Parameters.FLASH_MODE_OFF),
    On(Camera.Parameters.FLASH_MODE_ON),
    RedEye(Camera.Parameters.FLASH_MODE_RED_EYE),
    Torch(Camera.Parameters.FLASH_MODE_TORCH);

    private final String cameraParameter;

    CameraFlashMode(String cameraParameter) {
        this.cameraParameter = cameraParameter;
    }

    @Override
    public String toString() {
        return this.cameraParameter;
    }
}

