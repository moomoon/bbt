package org.ddrr.bbt;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

/**
 * Created by PhoebeHuyi on 2014.12.21.
 */
public class ClockView extends View {
    private final static int DEFAULT_MARK_COLOR = Color.WHITE;
    private final static int DEFAULT_HAND_COLOR = Color.WHITE;
    private final static float DEFAULT_MARK_WIDTH_SHORT = 0.1F;
    private final static float DEFAULT_MARK_WIDTH_LONG = 0.2F;
    private final static float DEFAULT_HOUR_HAND_LENGTH = 0.5F;
    private final static float DEFAULT_MINUTE_HAND_LENGTH = 0.7F;
    private int mWidth, mHeight;
    private int mHour, mMinute;
    private int mColorMark, mColorHand;
    private Paint mPt = new Paint();
    private final DimenInfo mDimenInfo = new DimenInfo();
    private PointF mPointFInner = new PointF();
    private PointF mPointFOuter = new PointF();
    private Scroller mScroller;

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ClockView);
        try {
            mDimenInfo.setMarkWidthShortRel(ta.getFraction(R.styleable.ClockView_mark_width_short, 1, 1, DEFAULT_MARK_WIDTH_SHORT));
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            mDimenInfo.setMarkWidthShortAbs(ta.getDimension(R.styleable.ClockView_mark_width_short, Float.NaN));
        }
        try {
            mDimenInfo.setMarkWidthLongRel(ta.getFraction(R.styleable.ClockView_mark_width_long, 1, 1, DEFAULT_MARK_WIDTH_LONG));
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            ;
            mDimenInfo.setMarkWidthLongAbs(ta.getDimension(R.styleable.ClockView_mark_width_long, Float.NaN));
        }
        try {
            mDimenInfo.setHourHandLengthRel(ta.getFraction(R.styleable.ClockView_hour_hand_length, 1, 1, DEFAULT_HOUR_HAND_LENGTH));
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            mDimenInfo.setHourHandLengthAbs(ta.getDimension(R.styleable.ClockView_hour_hand_length, Float.NaN));
        }
        try {
            mDimenInfo.setMinuteHandLengthRel(ta.getFraction(R.styleable.ClockView_minute_hand_length, 1, 1, DEFAULT_MINUTE_HAND_LENGTH));
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            mDimenInfo.setMinuteHandLengthAbs(ta.getDimension(R.styleable.ClockView_minute_hand_length, Float.NaN));
        }
        mColorMark = ta.getColor(R.styleable.ClockView_mark_color, DEFAULT_MARK_COLOR);
        mColorHand = ta.getColor(R.styleable.ClockView_hand_color, DEFAULT_HAND_COLOR);

        ta.recycle();
        mPt.setAntiAlias(true);
    }

    private void setTimeInternal(int hour, int minute, boolean animate) {
        mScroller.abortAnimation();
        if (animate) {
//            int from = mHour * 60 + mMinute;
//            int to = hour * 60 + minute;
//            int dist = to - from;
//            if(Math.abs(dist) > 360){
//                dist += 720;
//                dist %= 720;
//            }
//            mScroller.startScroll(from, 0, dist, 0, Math.max(Math.min(1000, to * 2), 500));
            int distHour = hour - mHour;
            if (Math.abs(distHour) > 6) {
                distHour += -Math.signum(distHour) * 12;
            }
            int distMin = minute - mMinute;
            if (Math.abs(distMin) > 30) {
                distMin += -Math.signum(distMin) * 60;
            }
            mScroller.startScroll(mHour, mMinute, distHour, distMin, 500);
        } else {
            this.mHour = hour;
            this.mMinute = minute;
        }
        invalidate();
    }

    public void setTime(int hour, int minute) {
        setTimeInternal(hour, minute, false);
    }

    public void setTime(String timeStr) {
        String[] s = timeStr.split("\\:");
        setTimeInternal(Integer.parseInt(s[0]), Integer.parseInt(s[1]), true);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            mHour = (mScroller.getCurrX() + 12) % 12;
            mMinute = (mScroller.getCurrY() + 60) % 60;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final PointF pInner = mPointFInner;
        final PointF pOuter = mPointFOuter;
        final float radius = getRadius();
        mPt.setColor(mColorMark);
        for (int i = 0; i < 12; i++) {
            float markLength;
            if (i % 3 == 0) {
                markLength = mDimenInfo.getMarkWidthLong();
            } else {
                markLength = mDimenInfo.getMarkWidthShort();
            }
            getPairedPoints(i * 30, radius - markLength, markLength, pInner, pOuter);
            mPt.setStrokeWidth(i == mHour % 12 ? 5F : 1F);
            canvas.drawLine(pInner.x, pInner.y, pOuter.x, pOuter.y, mPt);
        }
        mPt.setStrokeWidth(1F);
        mPt.setColor(mColorHand);
        getPairedPoints(mHour * 30 + mMinute / 2, 0, mDimenInfo.getHourHandLength(), pInner, pOuter);
        canvas.drawLine(pInner.x, pInner.y, pOuter.x, pOuter.y, mPt);
        getPairedPoints(mMinute * 6, 0, mDimenInfo.getMinuteHandLength(), pInner, pOuter);
        canvas.drawLine(pInner.x, pInner.y, pOuter.x, pOuter.y, mPt);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float getRadius() {
        return (float) (mHeight > mWidth ? mWidth : mHeight) / 2;
    }

    private void getPoint(double rad, float radius, PointF p) {
        float r = getRadius();
        p.set((float) (r + radius * Math.cos(rad)), (float) (r - radius * Math.sin(rad)));
    }

    private void getPairedPoints(float degree, float start, float length, PointF inner, PointF outer) {
        double rad = ((double) 90 - degree) / 180 * Math.PI;
        getPoint(rad, start, inner);
        getPoint(rad, start + length, outer);
    }

    private class DimenInfo {
        private float markWidthLongAbs = Float.NaN;
        private float markWidthShortAbs = Float.NaN;
        private float hourHandLengthAbs = Float.NaN;
        private float minuteHandLengthAbs = Float.NaN;

        private float markWidthLongRel = DEFAULT_MARK_WIDTH_LONG;
        private float markWidthShortRel = DEFAULT_MARK_WIDTH_SHORT;
        private float hourHandLengthRel = DEFAULT_HOUR_HAND_LENGTH;
        private float minuteHandLengthRel = DEFAULT_MINUTE_HAND_LENGTH;

        void setMarkWidthLongAbs(float markWidthLongAbs) {
            this.markWidthLongAbs = markWidthLongAbs;
        }

        void setMinuteHandLengthRel(float minuteHandLengthRel) {
            this.minuteHandLengthRel = minuteHandLengthRel;
        }

        void setMarkWidthShortAbs(float markWidthShortAbs) {
            this.markWidthShortAbs = markWidthShortAbs;
        }

        void setHourHandLengthAbs(float hourHandLengthAbs) {
            this.hourHandLengthAbs = hourHandLengthAbs;
        }

        void setMinuteHandLengthAbs(float minuteHandLengthAbs) {
            this.minuteHandLengthAbs = minuteHandLengthAbs;
        }

        void setMarkWidthLongRel(float markWidthLongRel) {
            this.markWidthLongRel = markWidthLongRel;
        }

        void setMarkWidthShortRel(float markWidthShortRel) {
            this.markWidthShortRel = markWidthShortRel;
        }

        void setHourHandLengthRel(float hourHandLengthRel) {
            this.hourHandLengthRel = hourHandLengthRel;
        }

        float getMarkWidthLong() {
            return Float.isNaN(markWidthLongAbs) ? markWidthLongRel * getRadius() : markWidthLongAbs;
        }

        float getMarkWidthShort() {
            return Float.isNaN(markWidthShortAbs) ? markWidthShortRel * getRadius() : markWidthShortAbs;
        }

        float getHourHandLength() {
            return Float.isNaN(hourHandLengthAbs) ? hourHandLengthRel * getRadius() : hourHandLengthAbs;
        }

        float getMinuteHandLength() {
            return Float.isNaN(minuteHandLengthAbs) ? minuteHandLengthRel * getRadius() : minuteHandLengthAbs;
        }
    }
}
