package com.khadas.npudemo;

import android.content.Context;
import android.util.Log;
public class KhadasNpuManager {
    private static final String TAG = "KhadasNpuManager";
    private static final boolean DEBUG = true;


    private Context mContext = null;


    static {
        System.loadLibrary("khadas_npu_jni");
    }



    private native static int native_npu_det_get_result(DetectResult detectresult,int dettype);
    private native static int native_npu_det_set_input(byte[] imgbyte,int pixel_format,int width,int height,int channel,int modetype);
    private native static int native_npu_det_set_model(int dettype);


    public KhadasNpuManager(Context context){
        mContext = context;
    }


    public static int npu_det_get_result(DetectResult detectresult,int dettype) {

        if (DEBUG)
            Log.d(TAG, "------npu_det_get_result begin " );
        return native_npu_det_get_result(detectresult,dettype);
    }

    public static int npu_det_set_model(int dettype) {

        if (DEBUG)
            Log.d(TAG, "------npu_det_set_model begin " );
        return native_npu_det_set_model(dettype);
    }

    public static int npu_det_set_input(byte[] imgbyte,int pixel_format,int width,int height,int channel,int modetype) {

        if (DEBUG)
            Log.d(TAG, "------native_npu_det_set_input begin " );
        return native_npu_det_set_input(imgbyte,pixel_format,width,height,channel,modetype);
    }

}

