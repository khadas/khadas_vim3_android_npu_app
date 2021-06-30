# khadas_android_npu_app
## npu app brief description
App implements the demo function of NPU model, including yolov2, yolov3 image recognition and yoloface face detection
## Main catalog description
 app/libs/armeabi-v7a/
 
``1)`` yolo model lib: libnn_yolo_v2.so,libnn_yolo_v3.so,libnn_yolo_face.so

``2)`` opencv image processing lib: libopencv_java4.so

``3)`` amlogic npu lib: libovxlib.so libOpenVx.so libVSC.so libGAL.so and so on

``4)`` khadsa jni lib: libkhadas_npu_jni.so

``5)`` other linked lib:libc++_shared.so libc.so and so on

app/src/main/assets/

This directory contains yolo model nb files used by vim3 vim3l platform

yolov2_88.nb
yolov2_99.nb
yolov3_88.nb
yolov3_99.nb
yolo_face_88.nb
yolo_face_99.nb
