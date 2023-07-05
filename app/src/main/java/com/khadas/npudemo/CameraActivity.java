package com.khadas.npudemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.media.Image.Plane;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;

import com.khadas.npudemo.customview.AutoFitTextureView;
import com.khadas.npudemo.customview.CameraConnectionFragment;
import com.khadas.npudemo.customview.LegacyCameraConnectionFragment;
import com.khadas.npudemo.customview.RectangleView;
import com.khadas.npudemo.domain.Recognition;
import com.khadas.npudemo.util.ImageUtils;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import com.khadas.npudemo.KhadasNpuManager;
import com.khadas.npudemo.DetectResult;
import com.quickbirdstudios.yuv2mat.Yuv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;


public abstract class CameraActivity extends AppCompatActivity implements Camera.PreviewCallback, OnImageAvailableListener {

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static String mStrboard;
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String TAG_CameraActivity = "CameraActivity";
    private static final Size DESIRED_PREVIEW_SIZE = new Size(1920, 1080);
    private ConstraintLayout mConstraintLayout;

    private RelativeLayout parentLayout;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    private boolean isProcessingFrame = false;
    private boolean useCamera2API = false;
    protected Fragment photoFragment;

    private int[] rgbBytes = null;
    private byte[][] yuvBytes = new byte[3][];
    private byte[] imageByte = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private Handler handler;
    private HandlerThread handlerThread;

    private KhadasNpuManager inceptionv3;
    private DetectResult detectresult;

    private int inputresult = -1;
    private int detectnum = -1;
    private int detresult = -1;
    private int setmoderesult = -1;
    private int index;

    private SurfaceHolder surfaceHolder;  // 用于画框的surfaceView的holder
    private Paint paint_rect;  // 画框的
    private Paint paint_txt;   // 写文本的
    private Canvas canvas;     // 画布

    public enum ModeType {
        DET_YOLOFACE_V2,
        DET_YOLO_V2,
        DET_YOLO_V3,
        DET_YOLO_V3_TINY,
        DET_YOLO_V7_TINY,
        DET_YOLO_V8N
    }
    static ModeType mode_type;

    public CameraActivity() {
        Log.d(TAG_CameraActivity, "CameraActivity enter ");
        inceptionv3 = new KhadasNpuManager(this);
        detectresult = new DetectResult();
        mStrboard = SystemProperties.get("ro.product.device");
    }

    public static void copyNbFile(Context context, String tab_name) {
        InputStream in = null;
        FileOutputStream out = null;
        Log.d(TAG_CameraActivity, "copyNbFile enter");
        /**data/data/路径*/
        String path = "/data" + "/nn_data";
        File file = new File(path + "/" + tab_name);
        exec("chmod 777 /data/nn_data");

        try {
            //创建文件夹
            File file_ = new File(path);
            if (!file_.exists()) {
                Log.d(TAG_CameraActivity, "copyNbFile mkdirs ");
                file_.mkdirs();
            }

            if (file.exists())//删除已经存在的
                Log.d(TAG_CameraActivity, "copyNbFile deleteOnExit ");
                file.deleteOnExit();


            if (!file.exists()) {
                Log.d(TAG_CameraActivity, "copyNbFile !file.exists ");
                file.createNewFile();
            }

            AssetManager assmgr = context.getApplicationContext().getAssets();
            if(mode_type == ModeType.DET_YOLO_V2) {
                if(mStrboard.equals("kvim3")) {
                    in = assmgr.open("yolov2_88.nb");
                } else {
                    in = assmgr.open("yolov2_99.nb");
                }
            }  if(mode_type == ModeType.DET_YOLO_V3) {
                if(mStrboard.equals("kvim3")) {
                    in = assmgr.open("yolov3_88.nb");
                } else {
                    in = assmgr.open("yolov3_99.nb");
                }
            }  if(mode_type == ModeType.DET_YOLO_V3_TINY) {
                if(mStrboard.equals("kvim3")) {
                    in = assmgr.open("yolov3_tiny_88.nb");
                } else {
                    in = assmgr.open("yolov3_tiny_99.nb");
                }
            }  if(mode_type == ModeType.DET_YOLO_V7_TINY) {
                if(mStrboard.equals("kvim3")) {
                    in = assmgr.open("yolov7_tiny_88.nb");
                } else {
                    in = assmgr.open("yolov7_tiny_99.nb");
                }
            }  if(mode_type == ModeType.DET_YOLO_V8N) {
                if(mStrboard.equals("kvim3")) {
                    in = assmgr.open("yolov8n_88.nb");
                } else {
                    in = assmgr.open("yolov8n_99.nb");
                }
            }  if(mode_type == ModeType.DET_YOLOFACE_V2) {
                if(mStrboard.equals("kvim3")) {
                    in = assmgr.open("yolo_face_88.nb");
                } else {
                    in = assmgr.open("yolo_face_99.nb");
                }
                //in = assmgr.open("faceNet_99.nb");
            }
            out = new FileOutputStream(file);
            int length = -1;
            byte[] buf = new byte[1024];
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static String exec(String command) {

        Process process = null;
        BufferedReader reader = null;
        InputStreamReader is = null;
        DataOutputStream os = null;

        try {
            process = Runtime.getRuntime().exec("su");
            is = new InputStreamReader(process.getInputStream());
            reader = new BufferedReader(is);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            process.waitFor();
            return output.toString();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }

                if (reader != null) {
                    reader.close();
                }

                if (is != null) {
                    is.close();
                }

                if (process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent =getIntent();
        int modetype =intent.getIntExtra(MainActivity.Intent_key,0);
        mode_type = ModeType.values()[modetype];
        Log.d(TAG_CameraActivity, "------onCreate11   mode_type "+ mode_type);
        //setContentView(R.layout.activity_main);
        //startActivity(new Intent(CameraActivity.this,MainActivity.class));
        //让屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        parentLayout = findViewById(R.id.parent_layout);
        mConstraintLayout = findViewById(R.id.layout);
        if (hasPermission()) {
            setFragment();
        } else {
            requestPermission();
        }
        SurfaceView surfaceView = findViewById(R.id.preview_detector_surfaceView);
        surfaceView.setZOrderOnTop(true);  // 设置surfaceView在顶层
        surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT); // 设置surfaceView为透明
        surfaceHolder = surfaceView.getHolder();  // 获取surfaceHolder以便后面画框
        exec("chmod 777 data");
        if(mode_type == ModeType.DET_YOLO_V2) {
            if(mStrboard.equals("kvim3")) {
                copyNbFile(this, "yolov2_88.nb");
            } else {
                copyNbFile(this, "yolov2_99.nb");
            }
            setmoderesult = inceptionv3.npu_det_set_model(mode_type.ordinal());
        }
        if(mode_type == ModeType.DET_YOLO_V3) {
            if(mStrboard.equals("kvim3")) {
                copyNbFile(this, "yolov3_88.nb");
            } else {
                copyNbFile(this, "yolov3_99.nb");
            }
            setmoderesult = inceptionv3.npu_det_set_model(mode_type.ordinal());
        }
        if(mode_type == ModeType.DET_YOLO_V3_TINY) {
            if(mStrboard.equals("kvim3")) {
                copyNbFile(this, "yolov3_tiny_88.nb");
            } else {
                copyNbFile(this, "yolov3_tiny_99.nb");
            }
            setmoderesult = inceptionv3.npu_det_set_model(mode_type.ordinal());
        }
        if(mode_type == ModeType.DET_YOLO_V7_TINY) {
            if(mStrboard.equals("kvim3")) {
                copyNbFile(this, "yolov7_tiny_88.nb");
            } else {
                copyNbFile(this, "yolov7_tiny_99.nb");
            }
            setmoderesult = inceptionv3.npu_det_set_model(mode_type.ordinal());
        }
        if(mode_type == ModeType.DET_YOLO_V8N) {
            if(mStrboard.equals("kvim3")) {
                copyNbFile(this, "yolov8n_88.nb");
            } else {
                copyNbFile(this, "yolov8n_99.nb");
            }
            setmoderesult = inceptionv3.npu_det_set_model(mode_type.ordinal());
        }
        if(mode_type == ModeType.DET_YOLOFACE_V2) {
            if(mStrboard.equals("kvim3")) {
                copyNbFile(this, "yolo_face_88.nb");
            } else {
                copyNbFile(this, "yolo_face_99.nb");
            }
            //copyNbFile(this, "faceNet_99.nb");
            setmoderesult = inceptionv3.npu_det_set_model(mode_type.ordinal());
        }

        Log.d(TAG_CameraActivity, "------npu_det_set_model   setmoderesult " + setmoderesult);
    }

    @Override
    public synchronized void onStart() {
        Log.d(TAG_CameraActivity, "onStart " + this);
        super.onStart();
        paint_rect = new Paint();  // 画矩形的paint
        paint_rect.setColor(Color.YELLOW);
        paint_rect.setStyle(Paint.Style.STROKE);//不填充
        paint_rect.setStrokeWidth(5); //线的宽度

        paint_txt = new Paint();  // 写文本的paint
        paint_txt.setColor(Color.RED);
        paint_txt.setStyle(Paint.Style.FILL);//不填充
        paint_txt.setTextSize(30.0f);  // 文本大小
        paint_txt.setStrokeWidth(8); //线的宽度
        canvas = new Canvas();  // 画布
    }

    @Override
    public synchronized void onResume() {
        Log.d(TAG_CameraActivity, "onResume " + this);
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        Log.d(TAG_CameraActivity, "onPause " + this);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            Log.e(TAG_CameraActivity, "Exception!", e);
        }

        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        Log.d(TAG_CameraActivity, "onStop " + this);
//        AutoFitTextureView textureView = getFragmentManager().findFragmentById(R.id.container).getView().findViewById(R.id.texture);
//        parentLayout.removeView(textureView);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        Log.d(TAG_CameraActivity, "onDestroy " + this);
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (allPermissionsGranted(grantResults)) {
                setFragment();
            } else {
                requestPermission();
            }
        }
    }

    private static boolean allPermissionsGranted(final int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否有相机权限
     *
     * @return
     */
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    /**
     * 请求相机权限
     *
     * @return
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                CameraActivity.this,
                                "Camera permission is required for this demo",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }
            requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
        }
    }

    protected void setFragment() {
        //获取cameraID并判断是否使用Camera2API
        String cameraId = chooseCamera();
        Log.d(TAG_CameraActivity,"cameraId:"+cameraId);
        android.app.Fragment fragment;
        if(useCamera2API) {
            Log.d(TAG_CameraActivity,"use Camera2 API");
            CameraConnectionFragment camera2Fragment =
                    CameraConnectionFragment.newInstance(
                            new CameraConnectionFragment.ConnectionCallback() {
                                @Override
                                public void onPreviewSizeChosen(final Size size, final int rotation) {
                                    previewHeight = size.getHeight();
                                    previewWidth = size.getWidth();
                                    Log.e(TAG_CameraActivity, " previewHeight = " + previewHeight + "previewWidth =" + previewWidth);
                                    CameraActivity.this.onPreviewSizeChosen(size, rotation);
                                }
                            },
                            this,
                            getLayoutId(),
                            getDesiredPreviewFrameSize());

            camera2Fragment.setCamera(cameraId);
            photoFragment = camera2Fragment;
        } else {
            Log.d(TAG_CameraActivity,"use Camera API");
            photoFragment =
                    new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, photoFragment).commit();
    }

    private String chooseCamera() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                Log.e(TAG_CameraActivity, "facing = " + facing);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                // Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distorted or otherwise broken previews.
                useCamera2API =
                        isHardwareLevelSupported(characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)
                                || (facing == CameraCharacteristics.LENS_FACING_EXTERNAL);
                Log.i(TAG_CameraActivity, "use Camera2 API?:" + useCamera2API);
                return cameraId;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG_CameraActivity, "Not allowed to access camera", e);
        }
        return null;
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }


    protected abstract int getLayoutId();


    protected abstract Size getDesiredPreviewFrameSize();

    protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

    /**
     * Callback for Camera2 API
     */
    @Override
    public void onImageAvailable(final ImageReader reader) {
        // We need wait until we have some size from onPreviewSizeChosen
        Log.d(TAG_CameraActivity,"Enter onImageAvailable");
        if (previewWidth == 0 || previewHeight == 0) {
            Log.d(TAG_CameraActivity,"Enter onImageAvailable previewWidth == 0 || previewHeight == 0");
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();

            if (image == null) {
                Log.d(TAG_CameraActivity,"Enter onImageAvailable image == null");
                return;
            }

            if (isProcessingFrame) {
                image.close();
                Log.d(TAG_CameraActivity,"Enter onImageAvailable isProcessingFrame");
                return;
            }
            isProcessingFrame = true;
            Trace.beginSection("imageAvailable");
            Mat mat = Yuv.rgb(image);
            Log.d(TAG_CameraActivity,"Enter onImageAvailable 22");
            // Mat(RGB)转Mat(BGR)
            Mat inputMat = new Mat();
            Imgproc.cvtColor(mat, inputMat, Imgproc.COLOR_RGB2BGR);

            // Mat(BGR)转byte数组
            byte[] data = new byte[inputMat.height() * inputMat.width()];
            Log.d(TAG_CameraActivity, "inputMat.height:" + inputMat.height() + ",inputMat.width:" + inputMat.width());
            inputMat.get(0, 0, data);
            //unuse
            //byte[] data = new byte[mat.height() * mat.width()];
            //Log.d(TAG_CameraActivity, "mat.height:" + mat.height() + ",mat.width:" + mat.width());
            //mat.get(0, 0, data);
            imageByte = data;
            Log.d(TAG_CameraActivity, "imageByte len :" + imageByte.length );
            postInferenceCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            Log.d(TAG_CameraActivity,"Enter onImageAvailable isProcessingFrame = false");
                            isProcessingFrame = false;
                        }
                    };

            processImage();
        } catch (final Exception e) {
            Log.e(TAG_CameraActivity, "Exception!", e);
            Trace.endSection();
            return;
        }
        Trace.endSection();
    }

    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        Log.d(TAG_CameraActivity,"Enter onPreviewFrame");
        if (isProcessingFrame) {
            Log.w(TAG_CameraActivity, "Dropping frame!");
            return;
        }
        Log.d(TAG_CameraActivity, "onPreviewFrame");

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            Camera.Size previewSize = camera.getParameters().getPreviewSize();
            previewHeight = previewSize.height;
            previewWidth = previewSize.width;
            if (rgbBytes == null) {
                rgbBytes = new int[previewWidth * previewHeight];
                Log.d(TAG_CameraActivity, "------ bytes.length" + bytes.length);
                Log.d(TAG_CameraActivity, "------previewHeight " + previewHeight);
                Log.d(TAG_CameraActivity, "------previewWidth " + previewWidth);
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
        } catch (final Exception e) {
            Log.e(TAG_CameraActivity, "Exception!", e);
            return;
        }
        Log.d(TAG_CameraActivity, "imageByte:" + bytes.length);
        isProcessingFrame = true;
        Mat mat = new Mat(previewHeight + (previewHeight / 2), previewWidth, CvType.CV_8UC1);
        mat.put(0, 0, bytes);
        Mat outPutMat = new Mat();

        Log.d(TAG_CameraActivity, "MAT: width:" + mat.width() + ",height:" + mat.height());
        Imgproc.cvtColor(mat, outPutMat, Imgproc.COLOR_YUV2BGR_NV21, 3);
//        Imgproc.cvtColor(mat, outPutMat, Imgproc.COLOR_YUV2BGR_NV21);
        Log.d(TAG_CameraActivity, "outPutMat.width：" + outPutMat.width() + ",outPutMat.height:" + outPutMat.height());
        byte[] data = new byte[outPutMat.width() * outPutMat.height()];
        outPutMat.get(0, 0, data);
        imageByte = data;
        //imageByte = bytes;
        postInferenceCallback =
                new Runnable() {
                    @Override
                    public void run() {
                        camera.addCallbackBuffer(bytes);
                        Log.d(TAG_CameraActivity, "onPreviewFrame isProcessingFrame = false");
                        isProcessingFrame = false;
                    }
                };

        processImage();
    }

    private void processImage() {
        Log.d(TAG_CameraActivity, "processImage enter");
        inputresult = inceptionv3.npu_det_set_input(imageByte, 3, previewWidth, previewHeight, 3, mode_type.ordinal());
        Log.d(TAG_CameraActivity, "------npu_det_set_input   inputresult " + inputresult);
        if(inputresult == 0) {
            detresult = inceptionv3.npu_det_get_result(detectresult, mode_type.ordinal());
            detectnum = detectresult.getDetectnum();
            Log.d(TAG_CameraActivity, "detectnum:" + detectnum);
        }

        runInBackground(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                            final RectF[] imageLocationRectF = new RectF[detectnum];
                            final String[] resultText = new String[detectnum];
                            for (index = 0; index < detectnum; index++) {
                                Log.d(TAG_CameraActivity, "------npu_det_get_result   detresult " + detresult + " detectresult det num :" + detectnum + " index:" + index + " x:" + detectresult.left[index] + " y:" + detectresult.top[index]);
                                Log.d(TAG_CameraActivity, "------npu_det_get_result   detresult " + detresult + " detectresult det num :" + detectnum + " index:" + index + " width:" + detectresult.right[index] + " height:" + detectresult.bottom[index]);
                                Log.d(TAG_CameraActivity, "------npu_det_get_result   detresult " + detresult + " detectresult det num :" + detectnum + " index:" + index + " lable_id:" + detectresult.lable_id[index] + " lable_name:" + detectresult.lable_name[index]);
                                imageLocationRectF[index] = new RectF();
                                float x = detectresult.left[index];
                                float y = detectresult.top[index];
                                float width = detectresult.right[index];
                                float height = detectresult.bottom[index];

//
//                                float x_1920 = (x-width/2)*1920;
//                                float b = (1-1080/1920)/2;
//                                //float y_1920 = (y-height/2- (1-1080/1920)/2 )*1920;
//                                float y_1920 = (y-height/2 - b )*1920;
//                                float w2 = 1920*width;
//                                //float h2 = 1080*2*height*(1-1080/1920);
//                                float h2 = 1920*height;
//
//                                Log.d(TAG_CameraActivity, "TAG_CameraActivity_jason x_1920:" + x_1920 + ",y_1920:"
//                                        + y_1920 + ",w2:" + w2 + ",h2:"
//                                        + h2 );
//
//                                imageLocationRectF[index].top = y_1920;
//                                imageLocationRectF[index].bottom = y_1920 + h2;
//                                imageLocationRectF[index].left = x_1920;
//                                imageLocationRectF[index].right = x_1920 +w2;

                                imageLocationRectF[index].top = 1080 * (y - height / 2.0f);
                                imageLocationRectF[index].bottom = 1080 * (y + height / 2.0f);
                                imageLocationRectF[index].left = 1920 * (x - width / 2.0f);
                                imageLocationRectF[index].right = 1920 * (x + width / 2.0f);

//                                imageLocationRectF[index].top = previewHeight * (y - height / 2.0f);
//                                imageLocationRectF[index].bottom = previewHeight * (y + height / 2.0f);
//                                imageLocationRectF[index].left = previewWidth * (x - width / 2.0f);
//                                imageLocationRectF[index].right = previewWidth * (x + width / 2.0f);


                                Log.d(TAG_CameraActivity, "TAG_CameraActivity left:" + imageLocationRectF[index].left + ",right:"
                                        + imageLocationRectF[index].right + ",top:" + imageLocationRectF[index].top + ",bottom:"
                                        + imageLocationRectF[index].bottom + ",previewWidth:" + previewWidth + ",previewHeight:" + previewHeight);
                                resultText[index] = detectresult.lable_name[index];
                            }
                            show_detect_results(imageLocationRectF, resultText);

                    }
                });
                readyForNextImage();
            }
        });
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    private void show_detect_results(final RectF[] imageLocationRectF,
                                     final String[] resultText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ClearDraw();  // 先清空上次画的框

                canvas = surfaceHolder.lockCanvas();   // 得到surfaceView的画布
                // 根据屏幕旋转角度调整canvas，以使画框方向正确
//                if(mRotateDegree != 0){
//                    if(mRotateDegree == 270){
//                        canvas.translate(mPreviewSize.getHeight(),0); // 坐标原点在x轴方向移动屏幕宽度的距离
//                        canvas.rotate(90);   // canvas顺时针旋转90°
//                    } else if(mRotateDegree == 90){
//                        canvas.translate(0,mPreviewSize.getWidth());
//                        canvas.rotate(-90);
//                    } else if(mRotateDegree == 180){
//                        canvas.translate(mPreviewSize.getHeight(),mPreviewSize.getWidth());
//                        canvas.rotate(180);
//                    }
//                }
                for (int i = 0; i < imageLocationRectF.length; i++) {   // 画框并在框上方输出识别结果和置信度
                    canvas.drawRect(imageLocationRectF[i], paint_rect);
                    canvas.drawText(resultText[i],
                            imageLocationRectF[i].left, imageLocationRectF[i].top + 20, paint_txt);
                }
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);  // 释放
                }
            }
        });
    }

    /**
     * 清空上次的框
     */
    private void ClearDraw() {
        try {
            canvas = surfaceHolder.lockCanvas(null);
            canvas.drawColor(Color.WHITE);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    protected void drawRectangle(RectF[] rectF, String[] resultText) {
        RectangleView rectangleView = new RectangleView(getApplicationContext());
        rectangleView.setRectF(rectF);
        rectangleView.setResultText(resultText);
        mConstraintLayout.addView(rectangleView);
    }

    protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                Log.d(TAG_CameraActivity, "Initializing buffer " + i + " at size " + buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

}