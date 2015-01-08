package org.ddrr.bbt;

import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.mmscn.utils.SimpleBaseAdapter;
import com.mmscn.widgets.ItemHeaderLayout;

import org.ddrr.bbt.infra.BBTEntry;
import org.ddrr.bbt.infra.CompBBTEntry;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CompBBTAdapter extends SimpleBaseAdapter<CompBBTEntry> {
    private OnBBTEntrySelectedListener mListener;
    private final static float HIGH_THRESHOLD = 36.5F;
    private final static float LOW_THRESHOLD = 36F;
    private ItemHeaderController mController = new ItemHeaderController();

    private Reference<ListView> mLvRef;

    public void show(ListView lv) {
        ListView currLv = null;
        if (null != mLvRef) {
            currLv = mLvRef.get();
        }
        if (lv != currLv) {
            if (null != currLv) {
                currLv.setOnScrollListener(null);
            }
            if (null != lv) {
                lv.setOnScrollListener(mController);
                lv.setAdapter(this);
            }
            mLvRef = new WeakReference<ListView>(lv);
        }
    }

    public void setOnBBTEntrySelectedListener(OnBBTEntrySelectedListener mListener) {
        this.mListener = mListener;
    }

    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mController.setOnScrollListener(l);
    }

    @Override
    protected View onCreateView(int position, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_bbt_entry_row, parent, false);
    }


    @Override
    protected void setupView(int position, com.mmscn.utils.ViewHolder h, View v) {
        ViewHolder holder = (ViewHolder) h;
        CompBBTEntry prev = position == 0 ? null : getItem(position - 1);
        CompBBTEntry curr = getItem(position);
        CompBBTEntry next = position == getCount() - 1 ? null
                : getItem(position + 1);
        holder.injectCompBBTEntry(prev, curr, next);
    }

    @Override
    protected com.mmscn.utils.ViewHolder createViewHolder(int position, View v) {
        ItemHeaderViewHolder holder = (ItemHeaderViewHolder) super.createViewHolder(position, v);
        mController.registerViewHolder(holder);
        return holder;
    }

    private class ViewHolder extends ItemHeaderViewHolder {
        @Header
        @Inject(R.id.header_layout)
        private ItemHeaderLayout headerLayout;
        @Inject(android.R.id.title)
        private TextView tvTitle;
        @Inject(R.id.list_bbt_entry_morning)
        private TextView tvMorning;
        @Inject(R.id.list_bbt_entry_night)
        private TextView tvNight;
        @Inject(R.id.list_bbt_entry_graph)
        private CellGraphView cgv;
        @Inject(R.id.list_bbt_entry_seperator)
        private View seperator;

        protected ViewHolder(View v) {
            super(v);
        }

        public void injectCompBBTEntry(CompBBTEntry prev, final CompBBTEntry entry,
                                       CompBBTEntry next) {
            Time t = new Time(Time.getCurrentTimezone());
            t.setJulianDay(entry.mJulianDay);
            tvTitle.setText(String.valueOf(t.monthDay));
            cgv.clear();
            float average = Float.NaN;
//            int tvAppId;
            if (null == entry.mMorningEntry) {
                tvMorning.setText(null);
//                tvAppId = R.drawable.entry_bg_empty;
            } else {
                average = entry.mMorningEntry.value;
//                if (entry.mMorningEntry.value > HIGH_THRESHOLD) {
//                    tvAppId = R.drawable.entry_bg_high;
//                } else if (entry.mMorningEntry.value < LOW_THRESHOLD) {
//                    tvAppId = R.drawable.entry_bg_low;
//                } else {
//                    tvAppId = R.drawable.entry_bg_normal;
//                }
                tvMorning.setText("" + entry.mMorningEntry.value);

            }
//            tvMorning.setBackgroundResource(tvAppId);
//            tvMorning.setTextColor(tvMorning.getResources().getColorStateList(tvAppId));
            if (null == entry.mNightEntry) {
                tvNight.setText(null);
//                tvAppId = R.drawable.entry_bg_empty;
            } else {
                if (Float.isNaN(average)) {
                    average = entry.mNightEntry.value;
                } else {
                    average += entry.mNightEntry.value;
                    average /= 2;
                }
//                if(entry.mNightEntry.value > HIGH_THRESHOLD){
//                    tvAppId = R.drawable.entry_bg_high;
//                } else if(entry.mNightEntry.value < LOW_THRESHOLD){
//                    tvAppId = R.drawable.entry_bg_low;
//                } else{
//                    tvAppId = R.drawable.entry_bg_normal;
//                }
                tvNight.setText("" + entry.mNightEntry.value);

            }
//            tvNight.setBackgroundResource(tvAppId);
//            tvNight.setTextColor(tvNight.getResources().getColorStateList(tvAppId));
            if (average >= HIGH_THRESHOLD) {
                seperator.setBackgroundResource(R.drawable.entry_bg_high);
            } else if (average > LOW_THRESHOLD) {
                seperator.setBackgroundResource(R.drawable.entry_bg_normal);
            } else {
                seperator.setBackgroundResource(R.drawable.entry_bg_low);
            }

            BBTEntry prevMorn = null != prev ? null != prev.mMorningEntry ? prev.mMorningEntry
                    : null
                    : null;
            BBTEntry nextMorn = null != next ? null != next.mMorningEntry ? next.mMorningEntry
                    : null
                    : null;
            BBTEntry prevNight = null != prev ? null != prev.mNightEntry ? prev.mNightEntry
                    : null
                    : null;
            BBTEntry nextNight = null != next ? null != next.mNightEntry ? next.mNightEntry
                    : null
                    : null;
            List<Float> valList = new ArrayList<>();

            if (null != prevMorn) {
                valList.add(prevMorn.value);
            }
            if (null != prevNight) {
                valList.add(prevNight.value);
            }
            if (valList.size() > 0) {
                cgv.setRangePrev(valList);
            }
            valList.clear();
            if (null != entry.mMorningEntry) {
                valList.add(entry.mMorningEntry.value);
            }
            if (null != entry.mNightEntry) {
                valList.add(entry.mNightEntry.value);
            }
            if (valList.size() > 0) {
                cgv.setRangeCurr(valList);
            }
            valList.clear();
            if (null != nextMorn) {
                valList.add(nextMorn.value);
            }
            if (null != nextNight) {
                valList.add(nextNight.value);
            }
            if (valList.size() > 0) {
                cgv.setRangeNext(valList);
            }
            cgv.updatePath();
//            cgv.addTrend(null == prevNight ? Float.NaN : prevNight.value,
//                    entry.mNightEntry.value, null == nextNight ? Float.NaN
//                            : nextNight.value, Color.RED);
//
//            cgv.addTrend(null == prevMorn ? Float.NaN : prevMorn.value,
//                    entry.mMorningEntry.value, null == nextMorn ? Float.NaN
//                            : nextMorn.value, Color.BLUE);

            if (null != mListener) {
                final String entryDate = String.format("%d-%02d-%02d", t.year, t.month + 1,
                        t.monthDay);
                tvMorning.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onBBTEntrySelected(entryDate, BBTEntry.BBT_ENTRY_TIME_POINT_MRNG);
                    }
                });
                tvNight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onBBTEntrySelected(entryDate, BBTEntry.BBT_ENTRY_TIME_POINT_EVE);
                    }
                });
            }

        }
    }

    public interface OnBBTEntrySelectedListener {
        public void onBBTEntrySelected(String entryDate, int timePoint);
    }

}
