package org.ddrr.bbt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Created by PhoebeHuyi on 2014.12.23.
 */
public class BlurActivityTemp extends Activity {

    private Bitmap mBackgroundOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(null);
        super.setContentView(R.layout.activity_blur_temp);
        if (null == mBackgroundOriginal) {
            Intent intent = getIntent();
            mBackgroundOriginal = intent.getParcelableExtra(BlurActivity.EXTRA_BACKGROUND);
            intent.removeExtra(BlurActivity.EXTRA_BACKGROUND);
            BlurSwitcher switcher = (BlurSwitcher) findViewById(R.id.blur_switcher);
            switcher.setBitmapOriginal(mBackgroundOriginal);
            switcher.startAnim();

        }
    }


    @Override
    public void setContentView(int layoutResID) {
        ViewGroup blurContentContainer = (ViewGroup) findViewById(R.id.blur_content);
        LayoutInflater.from(this).inflate(layoutResID, blurContentContainer);
    }
}
