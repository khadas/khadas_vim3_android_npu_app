package com.khadas.npudemo;

public class DetectResult {
	private  int detect_num ;
    public float left[];
	public float top[];
	public float right[];
	public float bottom[];
	public float score[];
        public int class_id[];
        public float prob[];
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

