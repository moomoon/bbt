package org.ddrr.bbt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import org.ddrr.bbt.Blurizer.BlurCallbacks;

public class BlurActivity extends Activity implements BlurCallbacks {
    public final static String EXTRA_BACKGROUND = "extra_background";
    private final static int BLUR_LEVEL_TIME = BaseApplication.getAppInstance()
            .getResources().getInteger(R.integer.blur_level_time);
    private final static float BLUR_RADIUS_MAX = 16F;
    private final static float BLUR_RADIUS_INTERVAL = 4F;
    private final Handler mHandler = new Handler();
    private boolean mBlurred = false;
    private ImageSwitcher mSwitcher;
    private float mRadius = 0F;
    private BlurTimer mTimer;
    private Bitmap mBackgroundOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(null);
        super.setContentView(R.layout.activity_blur);
        mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
        mSwitcher.setFactory(new BlurViewFactory());
        if (null == mBackgroundOriginal) {
            Intent intent = getIntent();
            mBackgroundOriginal = intent.getParcelableExtra(EXTRA_BACKGROUND);
            mSwitcher.setImageDrawable(new BitmapDrawable(getResources(),
                    mBackgroundOriginal));
            intent.removeExtra(EXTRA_BACKGROUND);
        }
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                R.anim.blur_bg_fade_in));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
        R.anim.blur_bg_stay));
        if (!mBlurred) {
            mBlurred = true;
            nextBlurFrame(0);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup blurContentContainer = (ViewGroup) findViewById(R.id.blur_content);
        LayoutInflater.from(this).inflate(layoutResID, blurContentContainer);
    }

    protected void onBlurFinished() {

    }

    private void nextBlurFrame(int minimalDelay) {
        if (mRadius < BLUR_RADIUS_MAX) {
            mRadius += BLUR_RADIUS_INTERVAL;
            mRadius = Math.min(BLUR_RADIUS_MAX, mRadius);
            mTimer = new BlurTimer();
            if (minimalDelay > 0) {
                mHandler.postDelayed(mTimer, minimalDelay);
            } else {
                mTimer.run();
            }
            Log.e("nextFrame " + minimalDelay,"time = " + (System.currentTimeMillis() % 10000) + " radius = " + mRadius);
            new org.ddrr.bbt.Blurizer().start(mBackgroundOriginal, mRadius, this);
        } else {
            onBlurFinished();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBlurred = false;
        mRadius = 0;

        try {
            mBackgroundOriginal.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBackgroundOriginal = null;
    }

    private class BlurTimer implements Runnable {
        private boolean overdue = false;
        private Bitmap blurred;

        public void submit(Bitmap blurred) {
            if (overdue) {
                Log.e("nextFrame overdued","time = " + (System.currentTimeMillis() % 10000) + " radius = " + mRadius);
                mSwitcher.setImageDrawable(new BitmapDrawable(getResources(),
                        blurred));
                nextBlurFrame(BLUR_LEVEL_TIME);
            } else {
                Log.e("nextFrame submit","time = " + (System.currentTimeMillis() % 10000) + " radius = " + mRadius);
                this.blurred = blurred;
            }
        }

        @Override
        public void run() {
            if (null == blurred) {
                overdue = true;

                Log.e("nextFrame set overdued","time = " + (System.currentTimeMillis() % 10000) + " radius = " + mRadius);

            } else {

                Log.e("nextFrame too fast","time = " + (System.currentTimeMillis() % 10000) + " radius = " + mRadius);
                mSwitcher.setImageDrawable(new BitmapDrawable(getResources(),
                        blurred));
                nextBlurFrame(BLUR_LEVEL_TIME);
            }
        }

    }

    private class BlurViewFactory implements ViewSwitcher.ViewFactory {

        @Override
        public View makeView() {
            ImageView iv = new ImageView(BlurActivity.this);
            // iv.setBackgroundColor(0xFF000000);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setLayoutParams(new ImageSwitcher.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            return iv;
        }

    }

    @Override
    public void onBlurSuccessed(Bitmap blurred) {
        mTimer.submit(blurred);

    }

    @Override
    public void onBlurFailed(int errorCode) {
        // TODO Auto-generated method stub

    }
}
