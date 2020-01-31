package com.philpot.camera.fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.philpot.camera.activity.CameraActivity;
import com.philpot.camera.activity.RotateViewsListener;
import com.philpot.camera.util.CropMath;
import com.philpot.camera.views.CameraFlashView;
import com.philpot.camera.R;

public class CameraPreviewFragment extends Fragment implements SurfaceHolder.Callback, RotateViewsListener {
    private static final String TAG = CameraPreviewFragment.class.getSimpleName();
    private static final int SELECT_GALLERY_IMAGE = 0;
    private static final String FLASH_STATE = "FLASH_STATE";

    private View mRoot;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    @SuppressWarnings("deprecation")
    private Camera mCamera;
    private Matrix matrix;
    private boolean supportsMetering;
    private boolean supportsFocusing;
    private int mAutoFocusTries = 0;
    private boolean captureImage;
    private int orientationDegrees;

    private View mTakePictureView;
    private CameraFlashView mFlashChooserView;
    private View mFocusRing;
    private int focusRingRadius;
    private ImageView mGridLines;
    private boolean isGridEnabled;

    private CameraFlashMode m_FlashState = CameraFlashMode.Auto;
    private ImageHolder mImageHolder;
    private ImageEditor mImageEditor;

    public static CameraPreviewFragment getInstance() {
        return new CameraPreviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(FLASH_STATE)) {
            m_FlashState = CameraFlashView.getFlashEnumFromInt(savedInstanceState.getInt(FLASH_STATE));
        }
        mImageHolder = (ImageHolder) getActivity();
        mImageEditor = (ImageEditor) getActivity();
        supportsMetering = false;
        supportsFocusing = false;
        matrix = new Matrix();
        ((CameraActivity) getActivity()).setRotateViewsListener(this);
    }

    public void rotateViews(@NonNull final Animation animation) {
        if (mFlashChooserView != null) mFlashChooserView.startAnimation(animation);
        if (mGridLines != null) mGridLines.startAnimation(animation);
    }

    @Override
    public void rotateViews(int fromDegrees, int toDegrees, int duration, Animation.AnimationListener listener) {
        RotateAnimation a = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        a.setDuration((350) * ((toDegrees % 90) + 1));
        a.setFillAfter(true);
        a.setAnimationListener(listener);
        rotateViews(a);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.camera_preview, container, false);

        mTakePictureView = mRoot.findViewById(R.id.cam_prev_take_picture);
        mTakePictureView.setOnClickListener(mTakePictureClick);

        mFocusRing = mRoot.findViewById(R.id.camera_focus_circle);
        focusRingRadius = getResources().getDimensionPixelSize(R.dimen.focus_ring_size) / 2;

        mGridLines = (ImageView) mRoot.findViewById(R.id.cam_grid_lines_button);
        isGridEnabled = true;
        enableGridLines();
        mGridLines.setOnClickListener(new ImageView.OnClickListener() {

            @Override
            public void onClick(View v) {
                isGridEnabled = !isGridEnabled;
                enableGridLines();
                mGridLines.setImageResource(isGridEnabled ? R.drawable.ic_camera_grid_on : R.drawable.ic_camera_grid_off);
            }
        });

        mSurfaceView = (SurfaceView) mRoot.findViewById(R.id.cam_prev_opencv_view);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
            mSurfaceView.setOnTouchListener(mSurfaceViewTouchListener);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //noinspection deprecation
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        return mRoot;
    }

    private void enableGridLines() {
        if(isGridEnabled) {
            mRoot.findViewById(R.id.camera_grid_top).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.camera_grid_bottom).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.camera_grid_left).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.camera_grid_right).setVisibility(View.VISIBLE);
        } else {
            mRoot.findViewById(R.id.camera_grid_top).setVisibility(View.INVISIBLE);
            mRoot.findViewById(R.id.camera_grid_bottom).setVisibility(View.INVISIBLE);
            mRoot.findViewById(R.id.camera_grid_left).setVisibility(View.INVISIBLE);
            mRoot.findViewById(R.id.camera_grid_right).setVisibility(View.INVISIBLE);
        }
    }

    private void setGridLines() {
        int screenWidth = mSurfaceView.getMeasuredWidth();
        int screenHeight = mSurfaceView.getMeasuredHeight() - getResources().getDimensionPixelSize(R.dimen.camera_button_area);

        View topGrid = mRoot.findViewById(R.id.camera_grid_top);
        View bottomGrid = mRoot.findViewById(R.id.camera_grid_bottom);
        View leftGrid = mRoot.findViewById(R.id.camera_grid_left);
        View rightGrid = mRoot.findViewById(R.id.camera_grid_right);

        FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5);
        FrameLayout.LayoutParams bottomParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5);
        FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(5, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(5, ViewGroup.LayoutParams.MATCH_PARENT);

        topParams.setMargins(0, (screenHeight / 3), 0, 0);
        topGrid.setLayoutParams(topParams);

        bottomParams.setMargins(0, (screenHeight / 3) * 2, 0, 0);
        bottomGrid.setLayoutParams(bottomParams);

        leftParams.setMargins((screenWidth / 3), 0, 0, 0);
        leftGrid.setLayoutParams(leftParams);

        rightParams.setMargins((screenWidth / 3) * 2, 0, 0, 0);
        rightGrid.setLayoutParams(rightParams);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFlashChooserView != null) {
            outState.putInt(FLASH_STATE, CameraFlashView.getIntFromFlashEnum(mFlashChooserView.getCurrentFlash()));
        }
    }

    private final OnClickListener mTakePictureClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mTakePictureView.setEnabled(false);
            startFocus(true);
        }
    };

    @SuppressWarnings("deprecation")
    private final Camera.PictureCallback mJPGCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mImageHolder.loadBitmap(data);
            mTakePictureView.setEnabled(true);
            mImageEditor.editImage();

        }
    };

    private final SurfaceView.OnTouchListener mSurfaceViewTouchListener = new SurfaceView.OnTouchListener() {
        @SuppressWarnings("deprecation")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP && mCamera != null && !captureImage) {
                mCamera.cancelAutoFocus();

                final Rect focusRect = getTapArea(event.getX(), event.getY(), 1.0f);

                Camera.Parameters params = mCamera.getParameters();

                if (supportsFocusing) {
                    params.setFocusAreas(new ArrayList<Camera.Area>() {{new Camera.Area(focusRect, 1000);}});
                }

                if (supportsMetering) {
                    final Rect meteringRect = getTapArea(event.getX(), event.getY(), 1.5f);
                    params.setMeteringAreas(new ArrayList<Camera.Area>() {{new Camera.Area(meteringRect, 1000);}});
                }

                mCamera.setParameters(params);
                mFocusRing.setVisibility(View.VISIBLE);
                mFocusRing.bringToFront();
                captureImage = false;
                startFocus(false);
            }
            mFocusRing.setX(event.getX() - focusRingRadius);
            mFocusRing.setY(event.getY() - focusRingRadius);
            return true;
        }
    };

    private int clamp(float val, float min, float max) {
        return (int)Math.max(min, Math.min(max, val));
    }

    private Rect getTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(getResources().getDimensionPixelSize(R.dimen.focus_area_size) * coefficient).intValue();
        int left = clamp((int) x - areaSize / 2, 0, mSurfaceView.getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, mSurfaceView.getHeight() - areaSize);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        matrix.mapRect(rectF);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    @SuppressWarnings("deprecation")
    private final Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success || ++mAutoFocusTries >= 10) {
                mFocusRing.setVisibility(View.INVISIBLE);
                mAutoFocusTries = 0;
                if (captureImage) mCamera.takePicture(null, null, mJPGCallback);
                captureImage = false;
            } else {
                mCamera.autoFocus(mAutoFocusCallback);
            }
        }
    };

    @SuppressWarnings("deprecation")
    private void startFocus(boolean takeImage) {
        mAutoFocusTries = 0;
        captureImage = takeImage;
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(mFlashChooserView.getCurrentFlash().toString().toLowerCase());
        mCamera.setParameters(params);
        mCamera.autoFocus(mAutoFocusCallback);
    }

    private void refreshCamera() {
        if(mSurfaceHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch(Exception e) {
            Log.e(TAG, "failed stopping camera preview", e);
        }

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            supportsMetering = mCamera.getParameters().getMaxNumMeteringAreas() > 0;
            supportsFocusing = mCamera.getParameters().getMaxNumFocusAreas() > 0;

        } catch(Exception e) {
            Log.e(TAG, "failed starting camera preview", e);
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        refreshCamera();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        initializeCameraSurface();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @SuppressWarnings("deprecation")
    private void initializeCameraSurface() {
        try {
            mCamera = Camera.open();
        } catch (RuntimeException e) {
            Log.e(TAG, "Unable to obtain camera", e);
            return;
        }

        Display display = getActivity().getWindowManager().getDefaultDisplay();

        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                orientationDegrees = 90;
                break;
            case Surface.ROTATION_90:
                orientationDegrees = 0;
                break;
            case Surface.ROTATION_270:
                orientationDegrees = 180;
                break;
            case Surface.ROTATION_180:
                orientationDegrees = 270;
                break;
        }

        mCamera.setDisplayOrientation(orientationDegrees);

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            initFlash();
            initAutoFocus();
            setGridLines();
        } catch (Exception e) {
            Log.e(TAG, "failed to start preview", e);
        }
    }

    private void initFlash() {
        mFlashChooserView = (CameraFlashView) mRoot.findViewById(R.id.cam_prev_ic_flash);
        mFlashChooserView.setDefaultFlashMode(m_FlashState);
        mFlashChooserView.setEnabled(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH));
    }

    @SuppressWarnings("deprecation")
    private void initAutoFocus() {
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(CameraFocusMode.Auto.cameraParameter);
        mCamera.setParameters(params);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_GALLERY_IMAGE) {
            InputStream iStream;
            byte[] inputData = null;

            try {
                iStream = getActivity().getContentResolver().openInputStream(data.getData());
                inputData = getBytes(iStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (inputData != null) {
                mImageHolder.loadBitmap(inputData);
            }

            mImageEditor.editImage();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }
}
