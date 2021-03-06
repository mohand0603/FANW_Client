package com.example.newyorkclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class MyPaintView extends View {

    public boolean changed = false;

    Canvas mCanvas;
    Bitmap mBitmap;
    Paint mPaint;

    float lastX;
    float lastY;

    Path mPath = new Path();

    float mCurveEndX;
    float mCurveEndY;

    Lock_queue draw_queue;

    int mInvalidateExtraBorder = 10;

    static final float TOUCH_TOLERANCE = 8;

    boolean can_touch = false;

    public MyPaintView(Context context) {
        super(context);
        init(context);
    }

    public MyPaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void get_queue(Lock_queue _temp) {
        this.draw_queue = _temp;
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5);

        this.lastX = -1;
        this.lastY = -1;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Bitmap img = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(img);
        canvas.drawColor(Color.WHITE);

        mBitmap = img;
        mCanvas = canvas;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }

    public void set_can_touch(boolean _value) {
        can_touch = _value;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!can_touch)
            return true;
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        String msg;
        switch (action) {
            case MotionEvent.ACTION_UP:
                changed = true;
                msg = "draw_up|" + x + "|" + y;
                draw_queue.push(msg);
                Rect rect = touchUp(x, y, false);
                if (rect != null) {
                    invalidate(rect);
                }
                mPath.rewind();

                return true;
            case MotionEvent.ACTION_DOWN:
                msg = "draw_down|" + x + "|" + y;
                draw_queue.push(msg);
                rect = touchDown(x, y);
                if (rect != null) {
                    invalidate(rect);
                }

                return true;
            case MotionEvent.ACTION_MOVE:
                msg = "draw_move|" + x + "|" + y;
                draw_queue.push(msg);
                rect = touchMove(x, y);
                if (rect != null) {
                    invalidate(rect);
                }
                return true;

        }
        return false;
    }

    public void draw_something(String _line) {
        String info[] = _line.split("\\|");
        float x = Float.parseFloat(info[1]);
        float y = Float.parseFloat(info[2]);
        switch(info[0]) {
            case "draw_up":
                changed = true;
                Rect rect = touchUp(x, y, false);
                if (rect != null) {
                    invalidate(rect);
                }
                mPath.rewind();
                break;
            case "draw_down":
                rect = touchDown(x, y);
                if (rect != null) {
                    invalidate(rect);
                }
                break;
            case "draw_move":
                rect = touchMove(x, y);
                if (rect != null) {
                    invalidate(rect);
                }
                break;
        }
    }

    public void clear() {
        if(mCanvas != null)
            mCanvas.drawColor(Color.WHITE);
    }

    private Rect touchMove(float x, float y) {
        Rect rect=processMove(x, y);
        return rect;
    }

    private Rect processMove(float x, float y) {

        final float dx=Math.abs(x-lastX);
        final float dy=Math.abs(y-lastY);

        Rect mInvalidateRect=new Rect();

        if(dx>=TOUCH_TOLERANCE || dy>=TOUCH_TOLERANCE){
            final int border=mInvalidateExtraBorder;

            mInvalidateRect.set((int)mCurveEndX-border,(int)mCurveEndY-border,(int)mCurveEndX+border,(int)mCurveEndY+border);

            float cx=mCurveEndX=(x+lastX)/2;
            float cy=mCurveEndY=(y+lastY)/2;

            mPath.quadTo(lastX,lastY,cx,cy);

            mInvalidateRect.union((int)lastX-border,(int)lastY-border,(int)lastX+border,(int)lastY+border);
            mInvalidateRect.union((int)cx-border,(int)cy-border,(int)cx,(int)cy+border);

            lastX=x;
            lastY=y;

            mCanvas.drawPath(mPath,mPaint);

        }

        return mInvalidateRect;
    }

    private Rect touchDown(float x, float y) {
        lastX=x;
        lastY=y;

        Rect mInvalidateRect=new Rect();
        mPath.moveTo(x,y);

        final int border=mInvalidateExtraBorder;
        mInvalidateRect.set((int)x-border,(int)y-border,(int)x+border,(int)y+border);
        mCurveEndX=x;
        mCurveEndY=y;

        mCanvas.drawPath(mPath,mPaint);
        return mInvalidateRect;
    }
    public void setStrokeWidth(int width){
        mPaint.setStrokeWidth(width);
    }

    private Rect touchUp(float x, float y, boolean b) {
        Rect rect=processMove(x, y);
        return rect;
    }

    public void setColor(int color){
        mPaint.setColor(color);
    }
    public void setCap(int cap){
        switch(cap){
            case 0:
                mPaint.setStrokeCap(Paint.Cap.BUTT);
                break;
            case 1:
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                break;
            case 2:
                mPaint.setStrokeCap(Paint.Cap.SQUARE);
                break;
        }
    }
}