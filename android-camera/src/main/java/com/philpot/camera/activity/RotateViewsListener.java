package com.philpot.camera.activity;

import android.view.animation.Animation;

public interface RotateViewsListener {
    void rotateViews(int fromDegrees, int toDegrees, int duration, Animation.AnimationListener listener);
}
