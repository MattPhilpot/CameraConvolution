package com.philpot.camera.fragment;

import android.hardware.Camera;

@SuppressWarnings("deprecation")
public enum CameraFocusMode {
    Auto(Camera.Parameters.FOCUS_MODE_AUTO),
    ContinuousVideo(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO),
    EDOF(Camera.Parameters.FOCUS_MODE_EDOF),
    Fixed(Camera.Parameters.FOCUS_MODE_FIXED),
    Infinity(Camera.Parameters.FOCUS_MODE_INFINITY),
    Macro(Camera.Parameters.FOCUS_MODE_MACRO);

    public final String cameraParameter;

    CameraFocusMode(String cameraParameter) {
        this.cameraParameter = cameraParameter;
    }
}
