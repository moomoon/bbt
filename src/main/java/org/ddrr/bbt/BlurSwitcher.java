package org.ddrr.bbt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by PhoebeHuyi on 2014.12.23.
 */
public class BlurSwitcher extends View {
    private int mAnimTime = 50;
    private Bitmap mBMOrig;
    private Bitmap mBMBase;
    private Bitmap mBMFade;
    private Bitmap mBMTemp;
    private int mHeight, mWidth;
    private float mScaleX, mScaleY;
    private Paint mPt;
    private int mAlpha;
    private Blurizer.BlurCallbacks mCurrCallbacks = null;
    private Scroller mScroller;
    private float mRadius;
    private float mRadiusInterval = 4F;
    private float mMaxRadius = 16F;

    public BlurSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        this.mScroller = new Scroller(context, new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return input;
            }
        }, false);
        this.mPt = new Paint();
    }

    private void getNextFrame(float radius) {
        new Blurizer().start(mBMOrig, radius, mCurrCallbacks = new Blurizer.BlurCallbacks() {
            @Override
            public void onBlurSuccessed(Bitmap blurred) {
                if (this.equals(mCurrCallbacks)) {
                    mBMTemp = blurred;
                    if (isFinished()) {
                        startAnimInternal();
                    }
                } else {
                    blurred.recycle();
                }

            }

            @Override
            public void onBlurFailed(int errorCode) {

            }
        });
    }

    private boolean isFinished() {
        return mScroller.isFinished();
    }

    public void clear(){
        if(null != mBMOrig){
            mBMOrig.recycle();
            mBMOrig = null;
        }
        if(null != mBMBase){
            mBMBase.recycle();
            mBMBase = null;
        }
        if(null != mBMFade){
            mBMFade.recycle();
            mBMFade = null;
        }
        if(null != mBMTemp){
            mBMTemp.recycle();
            mBMTemp = null;
        }
        mCurrCallbacks =null;
        mRadius = 0;
    }

    public void setBitmapOriginal(Bitmap bmOrig) {
        clear();
        this.mBMOrig = bmOrig;
        mBMBase = Bitmap.createBitmap(bmOrig);
        updateScale();
        invalidate();
    }

    public void startAnim() {
        startAnimInternal();
    }


    private void startAnimInternal() {
        if (null != mBMFade) {
            mBMBase.recycle();
            mBMBase = mBMFade;
        }
        mBMFade = mBMTemp;
        mBMTemp = null;
        mScroller.startScroll(0, 0, 255, 255, mAnimTime);
        if (mRadius < mMaxRadius) {
            mRadius = Math.min(mRadius + mRadiusInterval, mMaxRadius);
            getNextFrame(mRadius);
        }
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            mAlpha = mScroller.getCurrX();
            invalidate();
        } else if(null != mBMTemp){
            startAnimInternal();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(mScaleX, mScaleY);
        if (null != mBMBase) {
            mPt.setAlpha(255);
            canvas.drawBitmap(mBMBase, 0, 0, mPt);
        }
        if (null != mBMFade) {
            mPt.setAlpha(mAlpha);
            canvas.drawBitmap(mBMFade, 0, 0, mPt);
        }

    }


    private void updateScale(){
        if(null != mBMOrig){
            int width = mBMOrig.getWidth();
            int height = mBMOrig.getHeight();
            mScaleX = (float) mWidth / width;
            mScaleY = (float) mHeight / height;
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
        updateScale();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
