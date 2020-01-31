package com.philpot.camera.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.philpot.camera.activity.CameraActivity;
import com.philpot.camera.control.ImagePreviewController;
import com.philpot.camera.control.ImagePreviewControllerCallback;
import com.philpot.camera.util.AspectRatioUtil;
import com.philpot.camera.views.ImagePreviewFrameLayout;
import com.philpot.camera.R;
import com.philpot.camera.views.VerticalLabelView;

public class ImagePreviewFragment extends Fragment implements ImagePreviewControllerCallback {
    private static final String STATE_FIRST_RUN = "STATE_FIRST_RUN";

    private ImagePreviewFrameLayout mPicturePreview;
    private ImageView mWarningView;
    private View mSaveView;
    private View mCancelView;
    private SeekBar mContrastBar;
    private ProgressBar processingImageProgressBar;
    private View bottomButtonAccept;
    private View bottomButtonCancel;
    private View bottomButtonContrast;
    private View bottomButtonCrop;
    private View bottomButtonRotate;
    private float animationOffset;
    private boolean mBottomIsExpanded;

    private boolean mIsPortrait = true;
    private View mRoot;

    private ImageEditMode currentEditMode = ImageEditMode.NONE;


    protected ImagePreviewController previewController;

    public ImagePreviewFragment() {
    }

    public static ImagePreviewFragment getInstance() {
        return new ImagePreviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBottomIsExpanded = false;
        boolean firstRun = true;

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_FIRST_RUN)) {
            firstRun = savedInstanceState.getBoolean(STATE_FIRST_RUN);
        }

        previewController = new ImagePreviewController(
                (ImageHolder)getActivity(),
                (ImageEditor)getActivity(),
                this,
                firstRun);

        mIsPortrait = ((CameraActivity)getActivity()).getIsPortrait();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.image_preview, container, false);

        mPicturePreview = (ImagePreviewFrameLayout) mRoot.findViewById(R.id.img_preview_preview);

        processingImageProgressBar = (ProgressBar)mRoot.findViewById(R.id.processingImageProgressBar);
        processingImageProgressBar.setVisibility(View.GONE);

        mWarningView = (ImageView)mRoot.findViewById(R.id.img_preview_warning);

        mSaveView = mRoot.findViewById(R.id.img_preview_save);
        mSaveView.setOnClickListener(mOnSaveClick);

        mContrastBar = (SeekBar)mRoot.findViewById(R.id.contrast_slider);
        mContrastBar.setMax(20);
        mContrastBar.setProgress(10);
        mContrastBar.setOnSeekBarChangeListener(mOnContrastChangeListener);

        mCancelView = mRoot.findViewById(R.id.img_preview_cancel);
        mCancelView.setOnClickListener(mOnRetakeClick);

        bottomButtonAccept = mRoot.findViewById(R.id.ic_camera_accept);
        bottomButtonCancel = mRoot.findViewById(R.id.ic_camera_not_accept);
        bottomButtonContrast = mRoot.findViewById(R.id.buttoncontrast);
        bottomButtonCrop = mRoot.findViewById(R.id.buttoncrop);
        bottomButtonRotate = mRoot.findViewById(R.id.buttonrotate);
        animationOffset = (float)AspectRatioUtil.dpToPx(60);

        bottomButtonAccept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentEditMode) {
                    case CONTRAST:
                        sliderVerticalTranslateAnimation();
                        break;
                    case CROP:
                        mPicturePreview.processAndSetCropResult();
                        break;
                }
                setPreviewView();

                buttonVerticalTranslateAnimation();
            }
        });

        bottomButtonCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(currentEditMode) {
                    case CONTRAST:
                        previewController.fixContrast(getContrastLevel());
                        setPreviewImage();
                        sliderVerticalTranslateAnimation();
                        break;
                }
                setPreviewView();

                buttonVerticalTranslateAnimation();
            }
        });

        bottomButtonRotate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                previewController.rotateImages(90);
                setPreviewImage();
            }
        });

        bottomButtonContrast.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEditMode = ImageEditMode.CONTRAST;
                previewController.fixContrast(getContrastLevel());
                setPreviewImage();
                sliderVerticalTranslateAnimation();
                buttonVerticalTranslateAnimation();
            }
        });

        bottomButtonCrop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEditMode = ImageEditMode.CROP;
                setCropView();
                buttonVerticalTranslateAnimation();
            }
        });

        if (!mIsPortrait) {
            buttonPreRotation();
        }
        setPreviewImage();
        setPreviewView();
        previewController.setFirstRun(false);
        return mRoot;
    }

    private void buttonPreRotation() {
        bottomButtonRotate.setRotation(90);
        bottomButtonContrast.setRotation(90);
        bottomButtonCrop.setRotation(90);
        bottomButtonCancel.setRotation(90);
        bottomButtonAccept.setRotation(90);
        mSaveView.setRotation(90);
        mCancelView.setRotation(90);
        mWarningView.setRotation(90);
        processingImageProgressBar.setRotation(90);
    }

    private void sliderVerticalTranslateAnimation() {
        if (mBottomIsExpanded) {
            mContrastBar.animate().yBy(animationOffset);
        } else {
            mContrastBar.animate().yBy(-animationOffset);
        }
    }

    private void buttonVerticalTranslateAnimation() {
        if (mBottomIsExpanded) {
            bottomButtonRotate.animate().yBy(-animationOffset);
            bottomButtonContrast.animate().yBy(-animationOffset);
            bottomButtonCrop.animate().yBy(-animationOffset);
            bottomButtonCancel.animate().yBy(animationOffset);
            bottomButtonAccept.animate().yBy(animationOffset);
            mSaveView.animate().yBy(animationOffset);
            mCancelView.animate().yBy(animationOffset);
        } else {
            bottomButtonRotate.animate().yBy(animationOffset);
            bottomButtonContrast.animate().yBy(animationOffset);
            bottomButtonCrop.animate().yBy(animationOffset);
            bottomButtonCancel.animate().yBy(-animationOffset);
            bottomButtonAccept.animate().yBy(-animationOffset);
            mSaveView.animate().yBy(-animationOffset);
            mCancelView.animate().yBy(-animationOffset);
        }
        mBottomIsExpanded = !mBottomIsExpanded;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_FIRST_RUN, previewController.getFirstRun());
    }

    private void setPreviewImage() {
        mPicturePreview.setImageHolder(previewController.getImageHolder());
    }

    private void setPreviewView() {
        currentEditMode = ImageEditMode.NONE;
        mPicturePreview.setCropEnabled(false);
        previewController.checkImageQuality();
    }

    private void setCropView() {
        mPicturePreview.setCropEnabled(true);
    }

    private void disableControlsWhileValidating() {
        mSaveView.setVisibility(View.GONE);
        mCancelView.setVisibility(View.GONE);
    }

    private void enableControlsAfterValidating() {
        mSaveView.setVisibility(View.VISIBLE);
        mCancelView.setVisibility(View.VISIBLE);
    }

    @Override
    public void imageQualityCheckFinished(boolean isBlurry) {
        enableControlsAfterValidating();
        if (isBlurry) {
            mWarningView.setVisibility(View.VISIBLE);
            showWarning();
        } else {
            mWarningView.setVisibility(View.GONE);
        }
        processingImageProgressBar.setVisibility(View.GONE);
        mWarningView.setOnClickListener(mOnWarningClick);
    }

    @Override
    public void imageQualityCheckStarted() {
        mWarningView.setVisibility(View.GONE);
        disableControlsWhileValidating();
        showValidating();
        processingImageProgressBar.setVisibility(View.VISIBLE);
    }

    private void showWarning() {
        showToast(previewController.getImageEditor().getWarningMessage());
    }

    private void showValidating() {
        showToast(previewController.getImageEditor().getValidatingMessage());
    }

    private void showToast(String message) {
        if (!mIsPortrait) {
            VerticalLabelView view = new VerticalLabelView(getActivity().getApplicationContext());
            view.setMessage(message);
            view.setRotate(true);
            view.bringToFront();
            Toast toast = new Toast(getActivity().getApplicationContext());
            toast.setGravity(Gravity.LEFT | Gravity.CENTER, 50, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(view);
            toast.show();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private float getContrastLevel() {
        float retval = (((float)mContrastBar.getProgress())/2) - 4.0f;
        return retval < 0 ? --retval : retval;
    }

    private final SeekBar.OnSeekBarChangeListener mOnContrastChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            previewController.adjustContrast(getContrastLevel());
            setPreviewImage();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private final OnClickListener mOnWarningClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(previewController.isImageBlurry()) showWarning();
        }
    };

    private final OnClickListener mOnSaveClick = new OnClickListener() {

        @SuppressWarnings("deprecation")
        @Override
        public void onClick(View v) {
            saveImage();
        }
    };

    private final OnClickListener mOnRetakeClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            previewController.returnToCamera();
        }
    };

    @Deprecated
    private void saveImage() {
        if (previewController.isImageBlurry()) {
            showBlurryDialog();
        } else {
            previewController.saveImageAndFinish();
        }
    }

    private void showBlurryDialog() {
        final LinearLayout bob = (LinearLayout) mRoot.findViewById(R.id.warning_dialog_root);
        if (!mIsPortrait) bob.setRotation(90);
        TextView message = (TextView) mRoot.findViewById(R.id.warning_dialog_message);
        Button cancel = (Button) mRoot.findViewById(R.id.warning_dialog_cancel);
        Button accept = (Button) mRoot.findViewById(R.id.warning_dialog_save);

        message.setText(previewController.getImageEditor().getQualityQuestion());
        cancel.setText(previewController.getImageEditor().getChoiceNegative());
        accept.setText(previewController.getImageEditor().getChoicePositive());

        bob.setVisibility(View.VISIBLE);
        bob.bringToFront();
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bob.setVisibility(View.GONE);
            }
        });

        accept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bob.setVisibility(View.GONE);
                previewController.saveImageAndFinish();
            }
        });
    }


}
