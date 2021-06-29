package com.khadas.npudemo;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.graphics.Bitmap.Config;
import android.util.TypedValue;

public class ClassifierActivity extends CameraActivity {
    private static final Size DESIRED_PREVIEW_SIZE = new Size(1920, 1080);
    private static final float TEXT_SIZE_DIP = 10;
    private static final String TAG_ClassifierActivity = "ClassifierActivity";

    private Integer sensorOrientation;
    private Bitmap rgbFrameBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_photo_preview;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        //将Android系统中的非标准度量尺寸dp转变为标准度量尺寸px
//        final float textSizePx =
//                TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
//        borderedText = new BorderedText(textSizePx);
//        //设置等宽字体
//        borderedText.setTypeface(Typeface.MONOSPACE);

//        recreateClassifier(getModel(), getDevice(), getNumThreads());
//        if (classifier == null) {
//            LOGGER.e("No classifier on preview!");
//            return;
//        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        Log.i(TAG_ClassifierActivity, "Camera orientation relative to screen canvas: " + sensorOrientation);

        Log.i(TAG_ClassifierActivity, "Initializing at size " + previewWidth + "x" + previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    }
}
