package com.philpot.camera.fragment;

public interface ImageEditor {
    void editImage();
    void returnImage();
    void takeImage();
    void cancelImage();
    String getWarningMessage();
    String getValidatingMessage();
    String getQualityQuestion();
    String getChoicePositive();
    String getChoiceNegative();
}
