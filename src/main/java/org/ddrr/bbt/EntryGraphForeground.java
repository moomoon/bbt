package org.ddrr.bbt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by PhoebeHuyi on 2014.12.23.
 */
public class EntryGraphForeground extends View {
    private int mWidth, mHeight;
    private float mMaxData = 37F, mMinData = 35F;
    private float[] mDividers;
    private float[] mDividerPos;
    private String[] mLabels;
    private float mTextSize;
    private Paint mPt;

    public EntryGraphForeground(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPt = new Paint();
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != mDividerPos) {
            mPt.setColor(0x44888888);
            for (int i = 0; i < mDividerPos.length; i++) {
                if (i % 2 == 0) {
//                    TODO
                    canvas.drawRect(mDividerPos[i], 0, i < mDividerPos.length - 1 ? mDividerPos[i + 1] : mWidth, mHeight, mPt);
                }

            }
        }
    }

    private void updateDividerPos() {
        if (null != mDividers) {
            float[] pos = new float[mDividers.length];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = getXPosFromData(mDividers[i]);
            }
            mDividerPos = pos;
        }
    }

    private float getXPosFromData(float data) {
        return mWidth / (mMaxData - mMinData) * (data - mMinData);
    }

    public void setData(float[] dividers, String[] labels) {
        this.mDividers = dividers;
        this.mLabels = labels;
        updateDividerPos();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
        updateDividerPos();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
