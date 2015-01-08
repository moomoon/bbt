package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.graphics.Path;

/**
 * Created by PhoebeHuyi on 2014.12.12.
 */
public class OvalViewSwitcher extends ViewSwitcher {
    private float mHeight, mWidth;
    private Path mClipPath;
    private OnNextListener mOnNextListener;
    public OvalViewSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs
        );
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(mClipPath);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public void showNext() {
        if(null != mOnNextListener){
            mOnNextListener.onNext(this, getDisplayedChild());
        }
        super.showNext();
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
        Path p = new Path();
        p.addOval(new RectF(0, 0, mWidth, mHeight), Path.Direction.CW);
        this.mClipPath = p;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public interface OnNextListener {
        public void onNext(OvalViewSwitcher switcher, int currentChild);

    }

    public void setOnNextListener(OnNextListener l){
        this.mOnNextListener = l;
    }

}
