package org.ddrr.bbt;

import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.mmscn.utils.RefTask;

import java.lang.ref.WeakReference;

public class Blurizer {

    public final static int ERROR_ORIGINAL_BITMAP_NOT_AVAILABLE = 0;

    public void start(Bitmap bitmapOriginal, float radius,
                      BlurCallbacks callbacks) {
        new Processor(callbacks, bitmapOriginal, radius).execute();
    }

    private class Processor extends RefTask<BlurCallbacks, Void, Bitmap> {
        private final WeakReference<Bitmap> bitmapRef;
        private final float radius;

        public Processor(BlurCallbacks t, Bitmap bitmapOriginal, float radius) {
            super(t);
            this.bitmapRef = new WeakReference<Bitmap>(bitmapOriginal);
            this.radius = radius;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            // define this only once if blurring multiple times
            Bitmap bitmapOriginal = bitmapRef.get();
            if (null == bitmapOriginal || bitmapOriginal.isRecycled()) {
                return null;
            }
            RenderScript rs = RenderScript.create(BaseApplication
                    .getAppInstance());
            // this will blur the bitmapOriginal with a radius of 8 and save it
            // in
            // bitmapOriginal
            final Allocation input = Allocation.createFromBitmap(rs,
                    bitmapOriginal); // use this constructor
            // for best performance,
            // because it uses
            // USAGE_SHARED mode
            // which reuses memory
            final Allocation output = Allocation.createTyped(rs,
                    input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs,
                    Element.U8_4(rs));

            ScriptC_brightness scb = new ScriptC_brightness(rs, BaseApplication.getAppInstance().getResources(), R.raw.brightness);

            // ScriptC_brightness s = new ScriptC_brightness(rs);
            // s.set_brightness(0F);
            // s.set_gIn(input);
            // s.set_gOut(output);
            // s.forEach_root(input, output);

            script.setRadius(radius);
            script.setInput(input);
            script.forEach(output);
            scb.set_brightness((float)(-0.1 * Math.sqrt(radius)));
            scb.forEach_root(output, input);
            // s.setAdd(-radi
            // us * 5, -radius * 5, -radius * 5, 0);
            // ScriptC.
            // s.forEach(input, output);
            Bitmap blurred = Bitmap.createBitmap(bitmapOriginal.getWidth(),
                    bitmapOriginal.getHeight(), bitmapOriginal.getConfig());
            input.copyTo(blurred);
            return blurred;
        }

        @Override
        protected void onResultWithValidInstance(BlurCallbacks instance,
                                                 Bitmap result) {
            super.onResultWithValidInstance(instance, result);
            if (null == result) {
                instance.onBlurFailed(ERROR_ORIGINAL_BITMAP_NOT_AVAILABLE);
            } else {
                instance.onBlurSuccessed(result);
            }
        }

    }

    public interface BlurCallbacks {
        public void onBlurSuccessed(Bitmap blurred);

        public void onBlurFailed(int errorCode);
    }
}
