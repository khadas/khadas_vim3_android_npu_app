/*

typedef struct _DetectResult {
	int  detect_num;
        float facenet_result[128];
	det_classify_result_t result_name[MAX_DETECT_NUM];
	det_position_float_t point[MAX_DETECT_NUM];
}DetectResult,*pDetResult;

///Classification of label results
typedef struct classify_result_t {
  int lable_id;                        ///> Classification of label ID
  char lable_name[MAX_LABEL_LENGTH];   ///> Label name
} det_classify_result_t;


/// point definition
typedef struct det_position_t {
    det_position_type type;
	union {
		det_rect_point_t rectPoint;
		det_circle_point_t circlePoint;
		det_single_point_t singlePoint;
		input_image_t imageData;
	}point;
    char reserved[4];
} det_position_float_t;


typedef enum {
	DET_SINGLEPOINT_TYPE = 1,
	DET_RECTANGLE_TYPE = 2,
	DET_CIRCLE_TYPE = 3,
	DET_IMAGE_TYPE = 4,
} det_position_type;



typedef struct rect_point_t{
	float left;   ///< The value of left direction of the rectangle
	float top;    ///< The value of top direction of the rectangle
	float right;  ///< The value of right direction of the rectangle
	float bottom; ///< The value of bottom direction of the rectangle
	float score;  ///< The value of score of the rectangle
} det_rect_point_t;

typedef struct circle_point_t{
	float center;   ///<circle ceter point
	float radius;   ///<radius
    float score;    ///<The value of score of the score
} det_circle_point_t;

typedef struct single_point_t{
	float x;        ///<point x value
	float y;        ///<point y value
	float param;    ///<point param value if need
} det_single_point_t;

typedef struct input_image {
  unsigned char *data;            ///<picture data ptr value
  det_pixel_format pixel_format;  ///< color format
  int width;                      ///< width value of pixel
  int height;                     ///< height value of pixel
  int channel;                    ///< stride or channel for picture
} input_image_t;

/// pixel format definition
typedef enum {
  ///< Y 1 8bpp(Single channel 8bit gray pixels )
  PIX_FMT_GRAY8,
  ///< YUV  4:2:0 12bpp ( 3 channels, one brightness channel, the othe
  /// two for the U component and V component channel, all channels are continuous)
  PIX_FMT_YUV420P,
  ///< YUV  4:2:0 12bpp ( 2 channels, one channel is a continuous
  /// luminance
  /// channel, and the other channel is interleaved as a UV component )
  PIX_FMT_NV12,
  ///< YUV  4:2:0   	12bpp ( 2 channels, one channel is a continuous
  /// luminance
  /// channel, and the other channel is interleaved as a UV component )
  PIX_FMT_NV21,
  ///< BGRA 8:8:8:8 	32bpp ( 4-channel 32bit BGRA pixels )
  PIX_FMT_BGRA8888,
  ///< BGR  8:8:8   	24bpp ( 3-channel 24bit BGR pixels )
  PIX_FMT_BGR888,
  ///< RGBA 8:8:8：8	32bpp ( 4-channel 32bit RGBA pixels )
  PIX_FMT_RGBA8888,
  ///< RGB  8:8:8		24bpp ( 3-channel 24bit RGB pixels )
  PIX_FMT_RGB888
} det_pixel_format;

*/
package com.khadas.npudemo;

public class DetectResult {
	private  int detect_num ;
//	private final float[] left = new float[100];
//	private final float[] top = new float[100];
//	private final float[] right = new float[100];
//	private final float[] bottom = new float[100];
//	private final float[] score = new float[100];
    public float left[];
	public float top[];
	public float right[];
	public float bottom[];
	public float score[];

	public int lable_id[];
	public String lable_name [];

	DetectResult() {}

	public float[] getLeft() {
        return left;
    }

	public float[] getTop() {
        return top;
    }

	public float[] getRight() {
        return right;
    }

	public float[] getBottom() {
        return bottom;
    }

	public float[] getScore() {
        return score;
    }

	public int[] getlableid() {
		return lable_id;
	}

	public int getDetectnum() {
		return detect_num;
	}



	public void setDetectnum(int num) {
		 detect_num = num;
	}


	public  class Det_position_float {
		//rect_point
		float mLeft;   ///< The value of left direction of the rectangle
		float mTop;    ///< The value of top direction of the rectangle
		float mRight;  ///< The value of right direction of the rectangle
		float mBottom; ///< The value of bottom direction of the rectangle
		float mScore;  ///< The value of score of the rectangle

		//circlePoint
		float mcenter;   ///<circle ceter point
		float mradius;   ///<radius
		float mcircle_score;    ///<The value of score of the score

		//singlepoint
		float mx;        ///<point x value
		float my;        ///<point y value
		float mparam;    ///<point param value if need



		    public Det_position_float() {
		        mLeft = -1;
		        mTop = -1;
		        mRight = -1;
				mBottom = -1;
		        mScore = -1;
		   }

		    public Det_position_float(float left, float top, float right, float bottom ,float score ) {
		        mLeft = left;
		        mTop = top;
		        mRight = right;
		        mBottom = bottom;
				mScore = score;
		    }



    }

}

