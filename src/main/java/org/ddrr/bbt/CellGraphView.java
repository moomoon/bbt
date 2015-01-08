package org.ddrr.bbt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.ddrr.bbt.infra.MapDecorer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CellGraphView extends View {
    private float mHeight, mWidth;
    private float mValStart = 35f, mValEnd = 37f;
    private float mMinWidth = 5F;
    private Range mRangePrev = null, mRangeCurr = null, mRangeNext = null;

    private Path mPath;
    private Paint mPt;
//    private Paint mPtBlur;


//    private final List<Trend> mTrendList = new ArrayList<Trend>();

    public CellGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPt = new Paint();
        mPt.setColor(0xff0099cc);
        mPt.setStyle(Paint.Style.FILL);
//        mPtBlur = new Paint();
//        mPtBlur.setColor(0xff0099cc);
//        mPtBlur.setStyle(Paint.Style.STROKE);
//        mPtBlur.setStrokeWidth(30F);

//        mPtBlur.setAntiAlias(true);
//        mPtBlur.setDither(true);
//        mPtBlur.setStrokeJoin(Paint.Join.ROUND);
//        mPtBlur.setStrokeCap(Paint.Cap.ROUND);
//        mPtBlur.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));

    }

    public void setRangePrev(List<Float> valList) {
        if (valList.size() == 2) {
            this.mRangePrev = new Range(valList.get(0), valList.get(1));
        } else if (valList.size() == 1) {
            this.mRangePrev = new Range(valList.get(0));
        }
    }

    public void setRangeCurr(List<Float> valList) {
        if (valList.size() == 2) {
            this.mRangeCurr = new Range(valList.get(0), valList.get(1));
        } else if (valList.size() == 1) {
            this.mRangeCurr = new Range(valList.get(0));
        }
    }

    public void setRangeNext(List<Float> valList) {
        if (valList.size() == 2) {
            this.mRangeNext = new Range(valList.get(0), valList.get(1));
        } else if (valList.size() == 1) {
            this.mRangeNext = new Range(valList.get(0));
        }
    }


    private class Range {
        private float low, high;
        private float xLow, xHigh;

        private Range(float low, float high) {
            this.low = Math.min(low, high);
            this.high = Math.max(low, high);
        }

        private Range(float data) {
            this(data, data);
        }

        private void calculateXPosition() {
            float step = mWidth / (mValEnd - mValStart);
            float xl = step * (low - mValStart);
            float xh = step * (high - mValStart);
            float range = xh - xl;
            if (range < mMinWidth) {
                if (measured) {
                    Log.e("start calc before", "width = " + mWidth + " xl = " + xl + " xh = " + xh);
                }
                float sum = xl + xh;
                xl = (sum - mMinWidth) / 2;
                xh = (sum + mMinWidth) / 2;
                if (measured) {
                    Log.e("start calc", "low = " + low + " high = " + high + " xl = " + xl + " xh = " + xh);
                }
            }
            this.xLow = xl;
            this.xHigh = xh;
        }
    }

    public void updatePath() {
        if (null == mRangeCurr) {
            return;
        }
        mRangeCurr.calculateXPosition();
        List<Float> xLow = new ArrayList<>();
        List<Float> y = new ArrayList<>();
        List<Float> xHigh = new ArrayList<>();
        if (null != mRangePrev) {
            mRangePrev.calculateXPosition();
            xLow.add((mRangePrev.xLow + mRangeCurr.xLow) / 2);
            xHigh.add((mRangePrev.xHigh + mRangeCurr.xHigh) / 2);
            y.add(0F);
        }
        xLow.add(mRangeCurr.xLow);
        xHigh.add(mRangeCurr.xHigh);
        y.add(mHeight / 2F);
        if (null != mRangeNext) {
            mRangeNext.calculateXPosition();
            xLow.add((mRangeNext.xLow + mRangeCurr.xLow) / 2);
            xHigh.add((mRangeNext.xHigh + mRangeCurr.xHigh) / 2);
            y.add(mHeight);
        }
        if (xLow.size() <= 1) {
            return;
        }

        MapDecorer.Converter<List<Float>, float[]> conv = new MapDecorer.Converter<List<Float>, float[]>() {
            @Override
            public float[] convert(List<Float> object) {
                float[] array = new float[object.size()];
                Iterator<Float> iter = object.iterator();
                for (int j = 0; iter.hasNext(); j++) {
                    array[j] = iter.next();
                }
                return array;
            }
        };

        Path p = null;

        try {
            LineInterpolator.Calibrat c = new LineInterpolator.Calibrat() {

                @Override
                public float calibrat(int count) {
                    return 1F / (6 - count * 0.75F);
                }
            };
            float[] yArray = conv.convert(y);
            float[][] low = LineInterpolator.interpolate(conv.convert(xLow), yArray, 3, c);
            float[][] high = LineInterpolator.interpolate(conv.convert(xHigh), yArray, 3, c);
            p = new Path();
            if (measured) {
                Log.e("start", "xLow[0] " + xLow.get(0));
                Log.e("start", "low[0][0] " + low[0][0] + " low[1][0] " + low[1][0]);
            }
            p.moveTo(low[0][0], low[1][0]);
            for (int i = 1; i < low[0].length; i++) {
                p.lineTo(low[0][i], low[1][i]);
            }
            for (int i = high[0].length - 1; i >= 0; i--) {
                p.lineTo(high[0][i], high[1][i]);
            }
            p.close();
            this.mPath = p;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    private class Trend {
//        private final float prev, curr, next;
//        private Path path;
//        private Paint pt;
//
//        private Trend(float prev, float curr, float next, int color) {
//            this.prev = prev;
//            this.curr = curr;
//            this.next = next;
//            this.pt = new Paint();
//            this.pt.setStyle(Paint.Style.STROKE);
//            this.pt.setColor(color);
//            this.pt.setStrokeWidth(5F);
//            updatePath();
//        }
//
//        private void updatePath() {
//            Path p = null;
//            List<Integer> x = new ArrayList<Integer>();
//            List<Integer> y = new ArrayList<Integer>();
//
//            final float valRange = mValEnd - mValStart;
//            final float step = valRange / mWidth;
//            float yCenter = mHeight / 2F;
//            float xCenter = (curr - mValStart) / step;
//            if (!Float.isNaN(prev)) {
//                float xPrev = (prev - mValStart) / step;
//                x.add((int) ((xPrev + xCenter) / 2));
//                y.add(0);
//            }
//            x.add((int) xCenter);
//            y.add((int) yCenter);
//            if (!Float.isNaN(next)) {
//                float xNext = (next - mValStart) / step;
//                x.add((int) ((xNext + xCenter) / 2));
//                y.add((int) mHeight);
//            }
//
//
//            // it is absurd that we have to do this
//            MapDecorer.Converter<List<Integer>, int[]> conv = new MapDecorer.Converter<List<Integer>, int[]>() {
//                @Override
//                public int[] convert(List<Integer> object) {
//                    int[] array = new int[object.size()];
//                    Iterator<Integer> iter = object.iterator();
//                    for (int j = 0; iter.hasNext(); j++) {
//                        array[j] = iter.next();
//                    }
//                    return array;
//                }
//            };
//            try {
//                p = LineInterpolator.interpolate(conv.convert(x), conv.convert(y), 3, new LineInterpolator.Calibrat() {
//
//                    @Override
//                    public float calibrat(int count) {
//                        return 1F / (6 - count * 0.75F);
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            path = p;
//        }
//
//        private void draw(Canvas canvas) {
////            final float valRange = mValEnd - mValStart;
////            final float step = valRange / mWidth;
////            float centerY = mHeight / 2F;
////            float x = (curr - mValStart) / step;
////            if (!Float.isNaN(prev)) {
////                float xPrev = (prev - mValStart) / step;
////                canvas.drawLine((xPrev + x) / 2, 0, x, centerY, pt);
////            }
////            if (!Float.isNaN(next)) {
////                float xNext = (next - mValStart) / step;
////                canvas.drawLine(x, centerY, (xNext + x) / 2, mHeight, pt);
////            }
//            if (null != path) {
//                canvas.drawPath(path, pt);
//            }
//        }
//    }
//
//    public void addTrend(float prev, float curr, float next, int color) {
//        Trend t = new Trend(prev, curr, next, color);
//        Log.e("addTrend", "prev = " + prev + " curr = " + curr + " next = " + next);
//        mTrendList.add(t);
//    }

    public void clear() {
//        mTrendList.clear();
        mRangePrev = null;
        mRangeCurr = null;
        mRangeNext = null;
        mPath = null;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        for (Trend t : mTrendList) {
//            t.draw(canvas);
//        }
        if (null != mPath) {
//            canvas.drawPath(mPath,mPtBlur);
            canvas.drawPath(mPath, mPt);
        }
    }

    boolean measured = false;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.mWidth = MeasureSpec.getSize(widthMeasureSpec);
        measured = true;
        Log.e("startMeasure", "width = " + mWidth);
//        for (Trend t : mTrendList) {
//            t.updatePath();
//        }
        updatePath();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public static class LineInterpolator {

        public static float[][] interpolate(float[] x, float[] y, int time, Calibrat cal) {
            float[] newX = x;
            float[] newY = y;
            for (int i = 0; i < time; i++) {
                float ratio = cal.calibrat(i);
                newX = interpolate(newX, ratio);
                newY = interpolate(newY, ratio);
            }
            return new float[][]{newX, newY};
//            Path p = new Path();
//            final int length = newX.length;
//            p.moveTo(newX[0], newY[0]);
//            for (int i = 1; i < length; i++) {
//                p.lineTo(newX[i], newY[i]);
//            }
//            return p;
        }

        private static float[] interpolate(float[] pos, final float ratio) {
            final int length = pos.length * 2 - 2;
            float[] result = new float[length];
            result[0] = pos[0];
            result[length - 1] = pos[pos.length - 1];
            final int interpolateLast = length - 1;
            for (int i = 1; i < interpolateLast; i++) {
                if (i % 2 == 0) {
                    result[i] = interpolate(pos[i / 2], pos[i / 2 + 1], ratio);
                } else {
                    result[i] = interpolate(pos[i / 2 + 1], pos[i / 2], ratio);
                }
            }
            return result;
        }

        private static float interpolate(float pos0, float pos1, float ratio) {
            return (float) (pos0 * (1 - ratio) + pos1 * ratio);
        }

        public interface Calibrat {
            public float calibrat(int count);
        }
    }
}
