package com.khadas.npudemo.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

public class RectangleView extends View {
    private RectF[] mRectF;
    private String[] resultText;
    public RectangleView(Context context) {
        super(context);
    }

    public RectF[] getRectF() {
        return mRectF;
    }

    public void setRectF(RectF[] rectF) {
        mRectF = rectF;
    }

    public String[] getResultText() {
        return resultText;
    }

    public void setResultText(String[] resultText) {
        this.resultText = resultText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0.1f);

        //文字颜色自适应问题待解决
        Paint textPaint = new Paint();
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < mRectF.length; i++) {
            canvas.drawRect(mRectF[i],paint);
            canvas.drawText(resultText[i],(mRectF[i].right+mRectF[i].left)/2,(mRectF[i].bottom+mRectF[i].top)/2,textPaint);
        }

    }
}
