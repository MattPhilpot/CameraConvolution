package com.philpot.camera.activity;

import java.io.File;
import java.util.Date;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import android.view.animation.Animation;

import com.philpot.camera.fragment.CameraPreviewFragment;
import com.philpot.camera.util.CameraStorage;
import com.philpot.camera.fragment.ImageEditor;
import com.philpot.camera.fragment.ImageHolder;
import com.philpot.camera.fragment.ImagePreviewFragment;
import com.philpot.camera.R;
import com.philpot.camera.util.CropMath;

public class CameraActivity extends Activity implements ImageHolder, ImageEditor {
    private static final String TAG = CameraActivity.class.getSimpleName();

    private Bitmap mImage = null;
    private Uri mFileUri = null;
    private boolean onlyEditPhoto = false;
    private static final String STATE_ONLY_EDIT = "STATE_ONLY_EDIT";
    private static final String BASE_IMAGE_PATH = "BASE_IMAGE_PATH";
    private static final String EDIT_IMAGE_PATH = "EDIT_IMAGE_PATH";

    public static final String MESSAGE_QUALITY_WARNING = "MSG_QLTY_WARN";
    public static final String MESSAGE_QUALITY_VALIDATING = "MSG_QLTY_VAL";
    public static final String QUALITY_WARNING_QUESTION = "QLTY_WARN_QST";
    public static final String CHOICE_POSITIVE = "MSG_SAVE";
    public static final String CHOICE_NEGATIVE = "MSG_CANCEL";

    private static final int REQUEST_CAMERA_PERMISSIONS = 1337;
    private static final int CURRENT_FRAGMENT_CAMERA = 0;
    private static final int CURRENT_FRAGMENT_EDITOR = 1;
    private int mCurrentFragment;

    private String mValidatingImageQuality = "Validating Image Quality";
    private String mWarningImageQuality = "Image Quality Sucks. Take it again plzkthx.";
    private String mQualityWarning = "Are you sure you want to save, bruh?";
    private String mSaveMessage = "Save";
    private String mCancelMessage = "Cancel";

    private int mOrientation;
    private boolean mIsRotating;
    private int mFromDegrees;
    private boolean mIsPortrait;
    private RotateViewsListener mRotateViewsListener;
    private OrientationEventListener mOrientationEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            if(extras.containsKey(BASE_IMAGE_PATH)) {
                onlyEditPhoto = true;
                String path = extras.getString(BASE_IMAGE_PATH);
                mFileUri = Uri.parse(path);
                setEditImageFromPath(path);
            }
            setMessagesFromBundle(extras);
        }
        mIsPortrait = true;
        mIsRotating = false;
        mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                //This determines the orientation and allows for the "rotating" of icons from previous orientation to the new orientation
                //I believe this code to be more "intensive" than it needs to be, but is a workable solution for now.
                if (!mIsRotating && mRotateViewsListener != null) {
                    if ((315 < orientation) || (orientation < 45)) {
                        rotateViews(0, Configuration.ORIENTATION_PORTRAIT);
                        mIsPortrait = true;
                    } else if ((45 < orientation) && (orientation < 135)) {
                        rotateViews(-90, -Configuration.ORIENTATION_LANDSCAPE);
                        mIsPortrait = false;
                    } else if ((135 < orientation) && (orientation < 225)) {
                        rotateViews(mFromDegrees > 0 ? 180 : -180, -Configuration.ORIENTATION_PORTRAIT);
                        mIsPortrait = true;
                    } else if ((225 < orientation) && (orientation < 315)) {
                        rotateViews(90, Configuration.ORIENTATION_LANDSCAPE);
                        mIsPortrait = false;
                    }
                }
            }
        };

        //disable for now
        mOrientationEventListener.disable();

        parseSavedInstanceState(savedInstanceState);
        if (checkPermissions()) startFunctions();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        //get temp image and delete all TEMP*.jpg
        /*
        if (mImage == null && savedInstanceState.containsKey(EDIT_IMAGE_PATH)) {
            setEditImageFromPath(savedInstanceState.getString(EDIT_IMAGE_PATH));
            try {
                //noinspection ResultOfMethodCallIgnored,ConstantConditions
                new File(savedInstanceState.getString(EDIT_IMAGE_PATH)).delete();
            } catch (Exception e) {
                Log.e(TAG, "failed deleting EDIT_IMAGE_PATH", e);
            }
        }

        if (mFileUri == null && savedInstanceState.containsKey(BASE_IMAGE_PATH)) {
            mFileUri = Uri.parse(savedInstanceState.getString(BASE_IMAGE_PATH));
        }
        */
    }

    @Override
    public void onPause() {
        super.onPause();
        //save temp image

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteBaseImage();
    }

    public boolean getIsPortrait() {
        return this.mIsPortrait;
    }

    public void setRotateViewsListener(RotateViewsListener listener) {
        this.mRotateViewsListener = listener;

        if (mOrientationEventListener != null) {
            if (mRotateViewsListener != null && mOrientationEventListener.canDetectOrientation()) {
                mOrientationEventListener.enable();
            } else {
                mOrientationEventListener.disable();
            }
        }
    }

    public void rotateViews(@NonNull final int toDegrees, @NonNull final int compareOrientation) {
        if (mOrientation != compareOrientation) {
            mRotateViewsListener.rotateViews(mFromDegrees, toDegrees, (350) * ((toDegrees % 90) + 1), mAnimationListener);
            mOrientation = compareOrientation;
            mFromDegrees =  toDegrees;
        }
    }

    private Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mIsRotating = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mIsRotating = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private void startFunctions() {
        if (onlyEditPhoto || mImage != null) {
            editImage();
        } else {
            takeImage();
        }
    }

    private Bitmap getBaseBitmap() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(mFileUri.getPath(), options);
    }
    
    private void setEditImageFromPath(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        setImage(BitmapFactory.decodeFile(path, options));
    }

    private void setMessagesFromBundle(Bundle bundle) {
        mWarningImageQuality = bundle.getString(MESSAGE_QUALITY_WARNING, mWarningImageQuality);
        mValidatingImageQuality = bundle.getString(MESSAGE_QUALITY_VALIDATING, mValidatingImageQuality);
        mQualityWarning = bundle.getString(QUALITY_WARNING_QUESTION, mQualityWarning);
        mSaveMessage = bundle.getString(CHOICE_POSITIVE, mSaveMessage);
        mCancelMessage = bundle.getString(CHOICE_NEGATIVE, mCancelMessage);
    }

    @Override
    public Bitmap getImage() {
        return mImage != null ? mImage : getBaseImage();
    }

    @Override
    public Bitmap getBaseImage() {
        return getBaseBitmap();
    }

    @Override
    public Uri getImageUri() {
        return null;
    }

    @Override
    public Uri getBaseImageUri() {
        return mFileUri;
    }

    @Override
    public void setBaseImage(Bitmap bmp) {
        mFileUri = Uri.fromFile(new File(saveImage(bmp, "BASE")));
    }

    @Override
    public void setImage(Bitmap img) {
        mImage = img.copy(img.getConfig(), true);
    }

    @Override
    public void loadBitmap(byte[] data) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = (size.x > size.y ? size.x : size.y);
        int screenHeight = (size.y > size.x ? size.y : size.x);

        if(screenWidth > 1280 && screenHeight > 720) {
            screenWidth = 1280;
            screenHeight = 720;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = CropMath.calculateInSampleSize(options.outWidth, options.outHeight, screenWidth, screenHeight);

        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        setBaseImage(bmp);
        setImage(bmp);
    }

    @Override
    public void editImage() {
        setRotateViewsListener(null);
        changeFragment(ImagePreviewFragment.getInstance(), CURRENT_FRAGMENT_EDITOR);
    }

    @Override
    public void returnImage() {
        if (saveImage()) {
            Intent i = getIntent();
            i.setData(mFileUri);
            setResult(RESULT_OK, i);
            finish();
        }
    }

    @Override
    public void takeImage() {
        if(onlyEditPhoto) {
            cancelImage();
        } else {
            clearImages();
            changeFragment(CameraPreviewFragment.getInstance(), CURRENT_FRAGMENT_CAMERA);
        }
    }

    @Override
    public void cancelImage() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public String getWarningMessage() {
        return mWarningImageQuality;
    }

    @Override
    public String getValidatingMessage() {
        return mValidatingImageQuality;
    }

    @Override
    public String getQualityQuestion() {
        return mQualityWarning;
    }

    @Override
    public String getChoicePositive() {
        return mSaveMessage;
    }

    @Override
    public String getChoiceNegative() {
        return mCancelMessage;
    }

    private void deleteBaseImage() {
        try {
            //noinspection ResultOfMethodCallIgnored,ConstantConditions
            new File(mFileUri.getPath()).delete();
        } catch (Exception e) {
            Log.e(TAG, "failed deleting base image", e);
        }
    }


    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_ONLY_EDIT, onlyEditPhoto);
        if (mImage != null) {
            outState.putString(EDIT_IMAGE_PATH, saveImage(getImage(), "TEMP"));
            outState.putString(BASE_IMAGE_PATH, mFileUri.toString());
        }
        outState.putString(MESSAGE_QUALITY_WARNING, mWarningImageQuality);
        outState.putString(MESSAGE_QUALITY_VALIDATING, mValidatingImageQuality);
        outState.putString(QUALITY_WARNING_QUESTION, mQualityWarning);
        outState.putString(CHOICE_POSITIVE, mSaveMessage);
        outState.putString(CHOICE_NEGATIVE, mCancelMessage);
    }


    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        parseSavedInstanceState(savedInstanceState);
    }

    protected void parseSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (mImage == null && savedInstanceState.containsKey(EDIT_IMAGE_PATH)) {
                setEditImageFromPath(savedInstanceState.getString(EDIT_IMAGE_PATH));
                try {
                    //noinspection ResultOfMethodCallIgnored,ConstantConditions
                    new File(savedInstanceState.getString(EDIT_IMAGE_PATH)).delete();
                } catch (Exception e) {
                    Log.e(TAG, "failed deleting EDIT_IMAGE_PATH", e);
                }
            }

            if (mFileUri == null && savedInstanceState.containsKey(BASE_IMAGE_PATH)) {
                mFileUri = Uri.parse(savedInstanceState.getString(BASE_IMAGE_PATH));
            }

            if (savedInstanceState.containsKey(STATE_ONLY_EDIT)) {
                onlyEditPhoto = savedInstanceState.getBoolean(STATE_ONLY_EDIT);
                onlyEditPhoto = mImage != null && onlyEditPhoto;
            }
            setMessagesFromBundle(savedInstanceState);
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFragment == CURRENT_FRAGMENT_CAMERA) cancelImage();
        else takeImage();
    }

    private void changeFragment(final Fragment frag, final int currentFragment) {
        mIsRotating = false;
        mCurrentFragment = currentFragment;
        FragmentManager fragMgr = getFragmentManager();
        FragmentTransaction fragTrans = fragMgr.beginTransaction();
        fragTrans.replace(R.id.camera_main_fragment, frag);
        fragTrans.commit();
    }

    private void clearImages() {
        try {
            mImage = null;
            //noinspection ResultOfMethodCallIgnored
            new File(mFileUri.getPath()).delete();
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear images", e);
        }
    }

    private String saveImage(Bitmap bmp, String prefix) {
        CameraStorage externalStorage = new CameraStorage();

        String fileName = prefix + "_IMG_" + DateFormat.format("yyyyMMdd_hhmmss", new Date()) + ".jpg";
        File savedFile = externalStorage.saveBitmap(bmp, fileName, getFilesDir());
        if(savedFile != null) {
            return savedFile.getAbsolutePath();
        }
        return "";
    }

    private boolean saveImage() {
        boolean retVal = false;
        CameraStorage externalStorage = new CameraStorage();

        String fileName = "IMG_" + DateFormat.format("yyyyMMdd_hhmmss", new Date()) + ".jpg";
        File savedFile = externalStorage.saveBitmap(getImage(), fileName, getFilesDir());
        if(savedFile != null) {
            deleteBaseImage();
            mFileUri = Uri.parse(savedFile.toURI().toString());
            retVal = true;
        }

        return retVal;
    }

    private boolean checkPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSIONS);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        boolean cameraResult = false;
        boolean storageResult = false;

        for (int i = 0; i < permissions.length; ++i) {
            if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    cameraResult = true;
                } else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    storageResult = true;
                }
            }
        }

        if (cameraResult && storageResult) {
            startFunctions();
        } else {
            checkPermissions();
        }
    }
}

