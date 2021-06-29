package com.khadas.npudemo.customview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.khadas.npudemo.CameraActivity;
import com.khadas.npudemo.R;
import com.khadas.npudemo.util.ImageUtils;

import java.io.IOException;
import java.util.List;


public class LegacyCameraConnectionFragment extends Fragment {

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final String TAG_LegacyCameraConnectionFragment = "LegacyCameraConnectionFragment";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Camera camera;
    private Camera.PreviewCallback imageListener;
    private Size desiredSize;
    private Size previewSize;
    private RelativeLayout parentLayout;
    private int facing = -1;

    /**
     * The layout identifier to inflate for this Fragment.
     */
    private int layout;
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView textureView;

    public void openCamera(SurfaceTexture texture, int viewWidth, int viewHeight) {
        int index = getCameraId();
        Log.i(TAG_LegacyCameraConnectionFragment, "Selected cameraId:" + index);
        camera = Camera.open(index);
        Log.i(TAG_LegacyCameraConnectionFragment, "onSurfaceTextureAvailable11");
        try {
            Camera.Parameters parameters = camera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null
                    && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            List<Camera.Size> cameraSizes = parameters.getSupportedPreviewSizes();
            Size[] sizes = new Size[cameraSizes.size()];
            int i = 0;
            for (Camera.Size size : cameraSizes) {
                sizes[i++] = new Size(size.width, size.height);
            }
            previewSize = CameraConnectionFragment.chooseOptimalSize(sizes, desiredSize.getWidth(), desiredSize.getHeight());
            Log.d(TAG_LegacyCameraConnectionFragment, "previewSize.width:" + previewSize.getWidth() + ",previewSize.height:" + previewSize.getHeight());
            parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
            Camera.CameraInfo ci = new Camera.CameraInfo();
            Camera.getCameraInfo(index, ci);
            facing = ci.facing;
            configureTransform(viewWidth, viewHeight, facing);
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(index, info);
            int result;
            final Activity activity = getActivity();
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }
            Log.i(TAG_LegacyCameraConnectionFragment, "info.facing => " + info.facing);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                // 前置
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;
            } else {
                //后置
                result = (info.orientation - degrees + 360) % 360;
            }
//            Log.i(TAG_LegacyCameraConnectionFragment, "result => " + result + "," + info.orientation + "," + degrees);
            Log.d(TAG_LegacyCameraConnectionFragment, "result:" + result);
            camera.setDisplayOrientation(result);
            camera.setParameters(parameters);
            camera.setPreviewTexture(texture);
        } catch (IOException exception) {
            camera.release();
        }

        camera.setPreviewCallbackWithBuffer(imageListener);
        Camera.Size s = camera.getParameters().getPreviewSize();
        camera.addCallbackBuffer(new byte[ImageUtils.getYUVByteSize(s.width, s.height)]);
        Log.d(TAG_LegacyCameraConnectionFragment, "s.height:" + s.height + ",s.width:" + s.width);
        textureView.setAspectRatio(s.width, s.height );
    }

    private void configureTransform(int viewWidth, final int viewHeight, final int facing) {
        final Activity activity = getActivity();
//        if (null == textureView || null == previewSize || null == activity) {
//            return;
//        }
        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        final Matrix matrix = new Matrix();
        final RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        final RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        final float centerX = viewRect.centerX();
        final float centerY = viewRect.centerY();
        float px = previewSize.getWidth() / 2.0f;
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            final float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }

        if (facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            Log.d(TAG_LegacyCameraConnectionFragment, "facing = Camera.CameraInfo.CAMERA_FACING_FRONT");
            matrix.setScale(-1, 1);
            matrix.postTranslate(previewSize.getWidth(), 0);
        }
        textureView.setTransform(matrix);
    }

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
     * TextureView}.
     */
    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(
                        final SurfaceTexture texture, final int width, final int height) {
                    openCamera(texture, width, height);
                    camera.startPreview();
                }

                @Override
                public void onSurfaceTextureSizeChanged(
                        final SurfaceTexture texture, final int width, final int height) {
                    configureTransform(width, height, facing);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
                }
            };
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread backgroundThread;

    @SuppressLint("ValidFragment")
    public LegacyCameraConnectionFragment(
            final Camera.PreviewCallback imageListener, final int layout, final Size desiredSize) {
        this.imageListener = imageListener;
        this.layout = layout;
        this.desiredSize = desiredSize;
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(layout, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        parentLayout = view.findViewById(R.id.parent_layout);
        textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {

        super.onResume();
        startBackgroundThread();
        if (parentLayout.getChildAt(0) == null) {
            parentLayout.addView(textureView);
        }
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        Log.i(TAG_LegacyCameraConnectionFragment, "onResume");
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        if (textureView.isAvailable()) {
            if (camera != null) {
                camera.startPreview();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
        stopBackgroundThread();
        if (parentLayout.getChildAt(0) != null) {
            parentLayout.removeAllViews();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
        } catch (final InterruptedException e) {
            Log.e(TAG_LegacyCameraConnectionFragment, "Exception!", e);
        }
    }

    protected void stopCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private int getCameraId() {
        int defaultCameraId = -1;
        Camera.CameraInfo ci = new Camera.CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();
        Log.i(TAG_LegacyCameraConnectionFragment, "NumberOfCameras:" + numberOfCameras + "");
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, ci);
            Log.i(TAG_LegacyCameraConnectionFragment, "facing:" + ci.facing);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_BACK) return i;
        }
        if (numberOfCameras > 0) {
            defaultCameraId = 0;
        } else {
            Log.e(TAG_LegacyCameraConnectionFragment, "No camera available");
        }
        return defaultCameraId;
    }
}
