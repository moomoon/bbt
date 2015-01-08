package org.ddrr.bbt;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by PhoebeHuyi on 2014.12.22.
 */
public class MonthView extends FrameLayout implements AbsListView.OnScrollListener {
    private final int[] COLORS = {0x82357362, 0x82D93E3A, 0x8210611E, 0x82847606, 0x823D3D3D, 0x82782073, 0x82776C63, 0x82EE8E2F, 0x821F417C, 0x829D1358, 0x820F6606, 0x825D0E0B};
    private List<MonthInfo> mVisibleMonth = new ArrayList<>();
    private final Set<MonthInfo> mRecylePool = Collections.newSetFromMap(new HashMap<MonthInfo, Boolean>());
    private Paint mPt;
    private final String[] mMonthNames;
    private float mTextSizeMonth, mTextSizeYear;
    private float mSquareRadius;
    private int mTextColorMonth, mTextColorYear;
    private float mSquareWidth, mSquareHeight;
    private float mPaddingLeftYear;

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final Resources res = context.getResources();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MonthView);
        mTextSizeMonth = ta.getDimension(R.styleable.MonthView_month_text_size, res.getDimension(R.dimen.month_view_default_month_text_size));
        mTextSizeYear = ta.getDimension(R.styleable.MonthView_year_text_size, res.getDimension(R.dimen.month_view_default_year_text_size));
        mTextColorMonth = ta.getColor(R.styleable.MonthView_month_text_color, res.getColor(R.color.month_view_default_month_text_color));
        mTextColorYear = ta.getColor(R.styleable.MonthView_year_text_color, res.getColor(R.color.month_view_default_year_text_color));
        mPaddingLeftYear = ta.getDimension(R.styleable.MonthView_year_padding_left, res.getDimension(R.dimen.month_view_default_year_padding_left));
        mSquareWidth = ta.getDimension(R.styleable.MonthView_square_width, res.getDimension(R.dimen.month_view_default_square_width));
        mSquareHeight = ta.getDimension(R.styleable.MonthView_square_height, res.getDimension(R.dimen.month_view_default_square_height));
        mSquareRadius = ta.getDimension(R.styleable.MonthView_square_radius, res.getDimension(R.dimen.month_view_default_square_radius));
        ta.recycle();
        mMonthNames = res.getStringArray(R.array.entry_month_name);
        mPt = new Paint();
        mPt.setTextAlign(Paint.Align.CENTER);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.save();
//        canvas.rotate(90);
        for (MonthInfo mi : mVisibleMonth) {
            mi.draw(canvas);
        }

//        canvas.restore();
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mRecylePool.addAll(mVisibleMonth);
        MonthInfo mi = null;
        Time t = new Time(Time.getCurrentTimezone());
        int index = 0;
        int top = Integer.MAX_VALUE;
        int maxTop = top;
        for (int i = visibleItemCount - 1; i >= 0; i--) {
            int julianDay = ((CompBBTAdapter) view.getAdapter()).getItem(i + firstVisibleItem).mJulianDay;
            t.setJulianDay(julianDay);
            int year = t.year;
            int month = t.month;
            View child = view.getChildAt(i);
            int height = child.getHeight();
            if (!isSame(mi, month, year)) {
                boolean nextNotInOrder = true;
                if (index < mVisibleMonth.size()) {
                    mi = mVisibleMonth.get(index);
                    nextNotInOrder = !isSame(mi, month, year);
                }
                if (nextNotInOrder) {
                    mi = getMonthInfo();
                    mi.setInfo(month, year);
                    mVisibleMonth.add(index, mi);
                }
                if (top != Integer.MAX_VALUE) {
                    maxTop = top - height;
                }
                index++;
            }
            top = Math.max(0, child.getTop());
            mi.setTop(Math.min(maxTop, top));
        }
        if (index < mVisibleMonth.size() - 2) {
            cacheFrom(index + 1);
        }
        invalidate();
    }

    private MonthInfo getMonthInfo() {
        if (mRecylePool.isEmpty()) {
            return new MonthInfo();
        } else {
            MonthInfo mi = mRecylePool.iterator().next();
            mRecylePool.remove(mi);
            return mi;
        }
    }

    private void cacheFrom(int index) {
        final int size = mVisibleMonth.size();
        for (int i = index; i < size; i++) {
            MonthInfo monthInfo = mVisibleMonth.get(index);
            mRecylePool.add(monthInfo);
            mVisibleMonth.remove(index);

        }
    }

    private static boolean isSame(MonthInfo mi, int month, int year) {
        return null != mi && mi.month == month && mi.year == year;
    }

    private class MonthInfo {
        private int year = -1;
        private int month = -1;
        private float monthStrPos, yearStrPos;
        private String monthStr;
        private String yearStr;
        private int top;

        private void draw(Canvas c) {
            mPt.setColor(COLORS[month]);
            final int paddingLeft = getPaddingLeft();
            c.drawRoundRect(new RectF(paddingLeft, (float) top, paddingLeft + mSquareWidth, (float) (top + mSquareHeight)), mSquareRadius, mSquareRadius, mPt);
            mPt.setColor(Color.WHITE);
            mPt.setTextSize(mTextSizeYear);
//            mPt.setColor(mTextColorMonth);
            c.drawText(yearStr, paddingLeft + mSquareWidth / 2, top + yearStrPos, mPt);
            mPt.setTextSize(mTextSizeMonth);
            c.drawText(monthStr, paddingLeft + mSquareWidth / 2, top + monthStrPos, mPt);
//            c.drawText(monthStr, top, 0, mPt);
//            c.drawText(String.format("%s %d", monthStr, year % 100), top, 0, mPt);
//            mPt.setTextSize(mTextSizeYear);
//            mPt.setColor(mTextColorYear);
//            c.drawText(yearStr, top, -mPaddingLeftYear, mPt);
        }

        private boolean setInfo(int month, int year) {
            boolean changed = false;
            if (year != this.year) {
                this.year = year;
                this.yearStr = String.valueOf(year);
                mPt.setTextSize(mTextSizeYear);
                Rect r = new Rect();
                mPt.getTextBounds(yearStr, 0, yearStr.length(), r);
                yearStrPos = mSquareHeight / 4 + r.height() / 2;
                changed = true;
            }
            if (month != this.month) {
                this.month = month;
                this.monthStr = mMonthNames[month];
                mPt.setTextSize(mTextSizeMonth);
                Rect r = new Rect();
                mPt.getTextBounds(monthStr, 0, monthStr.length(), r);
                monthStrPos = mSquareHeight / 4 * 3 + r.height() / 2;
                changed = true;
            }
            return changed;
        }

        private void setTop(int top) {
            this.top = top;
        }
    }

}
