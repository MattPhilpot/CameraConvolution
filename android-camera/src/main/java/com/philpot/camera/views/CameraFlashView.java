package com.philpot.camera.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.philpot.camera.fragment.CameraFlashMode;
import com.philpot.camera.R;

public class CameraFlashView extends ImageView {

    private static final int FLASH_STATE_OFF = 0;
    private static final int FLASH_STATE_ON = 1;
    private static final int FLASH_STATE_AUTO = 2;

    private CameraFlashMode currentFlash = CameraFlashMode.Auto;

    public CameraFlashView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraFlashView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CameraFlashView(Context context) {
        super(context);
    }

    public void setDefaultFlashMode(CameraFlashMode defaultFlash) {
        this.currentFlash = defaultFlash;
    }

    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        if (enable) {
            this.setVisibility(VISIBLE);
            this.setOnClickListener(clickListener);
        } else {
            this.setVisibility(INVISIBLE);
        }
    }

    private final ImageView.OnClickListener clickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            moveToNextFlashMode();
        }
    };

    private void moveToNextFlashMode() {
        if (currentFlash == CameraFlashMode.Auto) {
            currentFlash = CameraFlashMode.Off;
            this.setImageResource(R.drawable.ic_camera_noflash);
        } else if (currentFlash == CameraFlashMode.Off) {
            currentFlash = CameraFlashMode.On;
            this.setImageResource(R.drawable.ic_camera_flash);
        } else if (currentFlash == CameraFlashMode.On) {
            currentFlash = CameraFlashMode.Auto;
            this.setImageResource(R.drawable.ic_camera_autoflash);
        }
    }

    public static CameraFlashMode getFlashEnumFromInt(int input) {
        switch(input) {
            case FLASH_STATE_OFF:
                return CameraFlashMode.Off;
            case FLASH_STATE_ON:
                return CameraFlashMode.Torch;
            default:
            case FLASH_STATE_AUTO:
                return CameraFlashMode.Auto;
        }
    }

    public static int getIntFromFlashEnum(CameraFlashMode input) {
        if(input == CameraFlashMode.Off) return FLASH_STATE_OFF;
        if(input == CameraFlashMode.Torch) return FLASH_STATE_ON;
        return FLASH_STATE_AUTO;
    }

    public CameraFlashMode getCurrentFlash() {
        return this.currentFlash;
    }
}
