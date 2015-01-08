package org.ddrr.bbt;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListenerEditText;
import android.widget.ListenerEditText.OnSelectionChangedListener;
import android.widget.OvalViewSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.mmscn.calendar.CalendarDialog;
import com.mmscn.timepicker.OnTimePickedListener;
import com.mmscn.timepicker.TimePickerDialog;
import com.mmscn.utils.ViewController;
import com.mmscn.widgets.TimeUtils;

import org.ddrr.bbt.infra.BBTConfig;
import org.ddrr.bbt.infra.BBTEntry;
import org.ddrr.bbt.infra.OneBBTLoader;
import org.ddrr.bbt.persistent.MapPersister;

public class NewEntryActivityTemp extends BlurActivityTemp implements
        OnSelectionChangedListener, OneBBTLoader.OnBBTEntryLoadedListener {


    public final static String EXTRA_ENTRY_DATE = "extra_entry_date";
    public final static String EXTRA_TIME_POINT = "extra_time_point";
    private BBTEntryInfo mEntryInfo;
    private ViewSwitcher mSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (null == savedInstanceState) {
            mEntryInfo = new BBTEntryInfo(this);
            Intent intent = getIntent();
            String entryDate = intent.getStringExtra(EXTRA_ENTRY_DATE);
            int timePoint = intent.getIntExtra(EXTRA_TIME_POINT, -1);
            if (null == entryDate || timePoint < 0) {
                throw new RuntimeException("extra entryDate = " + entryDate + " extra timePoint = " + timePoint);
            }
            mEntryInfo.setEntryDateAndTimePoint(entryDate, timePoint);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry_temp);
        mSwitcher = (ViewSwitcher) findViewById(R.id.new_entry_switcher);
        final OvalViewSwitcher labelSwitcher = (OvalViewSwitcher) findViewById(R.id.new_entry_label_switcher);
        labelSwitcher.setOnNextListener(new OvalViewSwitcher.OnNextListener() {
            @Override
            public void onNext(OvalViewSwitcher switcher, int currentChild) {
                switch (currentChild) {
                    case 0:
                        switcher.setInAnimation(NewEntryActivityTemp.this, R.anim.new_entry_label_slide_in_right);
                        switcher.setOutAnimation(NewEntryActivityTemp.this, R.anim.new_entry_label_slide_out_left);
                        mEntryInfo.setTimePoint(BBTEntry.BBT_ENTRY_TIME_POINT_EVE);
                        break;
                    case 1:
                        switcher.setInAnimation(NewEntryActivityTemp.this, R.anim.new_entry_label_slide_in_left);
                        switcher.setOutAnimation(NewEntryActivityTemp.this, R.anim.new_entry_label_slide_out_right);
                        mEntryInfo.setTimePoint(BBTEntry.BBT_ENTRY_TIME_POINT_MRNG);
                        break;
                }
            }
        });
        labelSwitcher.getChildAt(0).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                labelSwitcher.showNext();
            }
        });
        labelSwitcher.getChildAt(1).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                labelSwitcher.showNext();
            }
        });


        ListenerEditText etValue = (ListenerEditText) findViewById(R.id.new_entry_value);
        etValue.setOnSelectionChangedListener(this);
        mSwitcher.addView(new Keyboard35To42(new KBCB35To42()).getView(this,
                mSwitcher));
        mSwitcher.addView(new Keyboard0To9(new KBCB0To9()).getView(this,
                mSwitcher));
        findViewById(R.id.new_entry_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String[] str = ((TextView) v).getText().toString().split(":");
                new TimePickerDialog.Builder(v.getContext()).setTime(Integer.valueOf(str[0]), Integer.valueOf(str[1])).setOnTimePickedLitener(new OnTimePickedListener() {
                    @Override
                    public void onTimePicked(int hour, int minute) {
                        String timeStr = String.format("%d:%02d", hour, minute);
                        ((TextView) v).setText(timeStr);
                        ((ClockView) findViewById(R.id.new_entry_time_clock)).setTime(timeStr);
                    }
                }).create().show();
            }
        });
        findViewById(R.id.new_entry_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String[] str = ((TextView) v).getText().toString().split("\\-");
                new CalendarDialog.Builder(v.getContext()).setTime(Integer.valueOf(str[0]), Integer.valueOf(str[1]) - 1, Integer.valueOf(str[2])).setOnDateSetLisener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String dateStr = String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                        mEntryInfo.setEntryDate(dateStr);
                        ((TextView) v).setText(dateStr);
                    }
                }).create().show();
            }
        });


        startLayoutAnimation();
        onSelectionChanged(
                (ListenerEditText) findViewById(R.id.new_entry_value), 0, 0);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (collectData()) {
            mEntryInfo.writeToDB();
        }
        mEntryInfo.clear();

    }

    private boolean collectData() {
        try {
            String value = ((TextView) findViewById(R.id.new_entry_value)).getText().toString();
            if (value.length() > 0) {
                boolean changed = mEntryInfo.changed | mEntryInfo.setValue(Float.parseFloat(value + "0"));
                if (changed) {
                    String entryTimeStr = ((TextView) findViewById(R.id.new_entry_time)).getText().toString();
                    String nowStr = TimeUtils.toDateTime(System.currentTimeMillis());
                    mEntryInfo.setRecordDateTime(nowStr);
                    mEntryInfo.setEntryTime(entryTimeStr);
                }
                return changed;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mEntryInfo.forceCreate();
            return collectData();
        }
        return false;
    }
//
//
//    @Override
//    protected void onBlurFinished() {
//        super.onBlurFinished();
//        Log.e("onBlurFinished", "onBlurfinished");
//        Log.e("test", "onBlurFinished");
//    }

    private void startLayoutAnimation() {
        mSwitcher.setVisibility(View.VISIBLE);

    }


    @Override
    public void onBBTEntryLoaded(BBTEntry entry) {
        updateDisplay();
    }

    private void updateDisplay() {
        ViewSwitcher labelSwitcher = (ViewSwitcher) findViewById(R.id.new_entry_label_switcher);
        if (mEntryInfo.getEntry().timePoint != labelSwitcher.getDisplayedChild()) {
            labelSwitcher.showNext();
        }
        TextView tvEntryDate = (TextView) findViewById(R.id.new_entry_date);
        tvEntryDate.setText(mEntryInfo.getEntry().entryDate);
        TextView tvEntryTime = (TextView) findViewById(R.id.new_entry_time);
        ClockView clockView = (ClockView) findViewById(R.id.new_entry_time_clock);
        String entryTime = mEntryInfo.getEntry().entryTime;
        if (null == entryTime) {
            Time t = new Time(Time.getCurrentTimezone());
            t.setToNow();
            entryTime = String.format("%02d:%02d", t.hour, t.minute);
        }
        tvEntryTime.setText(entryTime);
        clockView.setTime(entryTime);
        TextView tvValue = (TextView) findViewById(R.id.new_entry_value);
        float value = mEntryInfo.getEntry().value;
        if (Float.isNaN(value)) {
            tvValue.setText(BBTConfig.getDefaultValue("basal-body-temp"));
        } else {
            tvValue.setText(String.valueOf(mEntryInfo.getEntry().value));
        }
    }

    private class KBCBViewCtrl extends ViewController {

        @Override
        protected final View onCreateView(Context context, ViewGroup parent) {
            return findViewById(R.id.new_entry_value);
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        protected EditText getEditText() {
            return (EditText) getView(null, null);
        }

    }

    private class KBCB0To9 extends KBCBViewCtrl implements KeyboardCallbacks {

        @Override
        public void onInput(String content) {
            if (!isKeyboardEnabled) {
                Log.e("onInput", "blocked");
                return;
            }
            EditText et = getEditText();
            int length = et.length();
            if (length < 3) {
                Log.e("keyboard error", "value = " + et.getText().toString()
                        + " keyboardType = KB0To9");
                return;
//                onSelectionChanged((ListenerEditText) et,
//                        et.getSelectionStart(), et.getSelectionEnd());
            } else {
                int selStart = et.getSelectionStart();
                int selEnd = et.getSelectionEnd();
                if (selStart < 3) {
                    Log.e("selection error", "value = "
                            + et.getText().toString() + " selectionStart = "
                            + selStart);
                    return;
                }
                et.getText().replace(selStart, selEnd, content);
                et.setSelection(selStart == selEnd ? selEnd + content.length()
                        : selEnd);
                et.requestFocus();
                updateChangedState();
            }
            if (!et.getText().toString().contains(".")) {
                Log.e("onInput", "wrong");
            }
        }

        @Override
        public void onBackspace() {
            onKeyboardBackspace();
        }
    }

    private class KBCB35To42 extends KBCBViewCtrl implements KeyboardCallbacks {

        @Override
        public void onInput(String content) {
            if (!isKeyboardEnabled) {
                return;
            }
            EditText et = getEditText();
            content = content.trim() + '.';
            int selStart = et.getSelectionStart();
            int selEnd = et.getSelectionEnd();
            et.getText().replace(selStart, selEnd, content);
            et.setSelection(selStart == selEnd ? selEnd + content.length()
                    : selEnd);
            et.requestFocus();
            updateChangedState();
        }

        @Override
        public void onBackspace() {
            onKeyboardBackspace();
        }

    }

    private void onKeyboardBackspace() {
        EditText et = (EditText) findViewById(R.id.new_entry_value);
        int selStart = et.getSelectionStart();
        int selEnd = et.getSelectionEnd();
        if (selStart == 0) {
            et.setText(null);
        } else if (selEnd != selStart) {
            et.getText().replace(selStart, selEnd, "");
        } else if (selStart > 3) {
            et.getText().replace(selStart - 1, selStart, "");
        } else {
            et.setText(null);
        }
        et.requestFocus();
        updateChangedState();
    }

    private void updateChangedState() {
        boolean changed = false;
        float originValue = mEntryInfo.getEntry().value;
        String currValueStr = ((TextView) findViewById(R.id.new_entry_value)).getText().toString();
        float currValue = 0F;
        try {
            currValue = Float.parseFloat(currValueStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        String originTime = mEntryInfo.getEntry().entryTime;
        String currTime = ((TextView) findViewById(R.id.new_entry_time)).getText().toString();
        changed |= !currTime.equals(originTime);
        changed |= currValue == originValue;
        mEntryInfo.setChangedState(changed);
    }

    @Override
    public void onSelectionChanged(ListenerEditText et, int selStart, int selEnd) {
        int length = et.length();
        if (3 < selStart) {
            if (4 < length && (4 != selStart || 5 != selEnd)) {
                et.setSelection(4, 5);
            }
        } else if (2 < selStart) {
            if (3 < length && 4 != selEnd) {
                et.setSelection(3, 4);
            }
        } else if (0 <= selStart) {
            if (1 < length && 3 != selEnd) {
                et.setSelection(0, 3);
            }
        }

        int keyboardId = 0;
        if (2 < selStart) {
            keyboardId = 1;
        }
        if (keyboardId != mSwitcher.getDisplayedChild()) {
            isKeyboardEnabled = false;
            mSwitcher.setInAnimation(this, R.anim.keyboard_fade_in);
            mSwitcher.setOutAnimation(this, R.anim.keyboard_fade_out);
            mSwitcher.setDisplayedChild(keyboardId);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isKeyboardEnabled = true;
                }
            }, getResources().getInteger(R.integer.keyboard_switch_time) * 2);
        }
    }

    private boolean isKeyboardEnabled = true;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private static class BBTEntryInfo {

        private String entryDate = null;
        private int timePoint = -1;
        private BBTEntry tempEntry = null;
        private boolean changed = false;
        private boolean existed = false;

        private final OneBBTLoader.OnBBTEntryLoadedListener refListener;

        private final OneBBTLoader.OnBBTEntryLoadedListener internalListener = new OneBBTLoader.OnBBTEntryLoadedListener() {
            @Override
            public void onBBTEntryLoaded(BBTEntry entry) {
                Log.e("internal loaded", "entry = " + entry);


                if (null == entry) {
                    if (validateCurrentEntryInternal()) {
                        refListener.onBBTEntryLoaded(getEntry());
                    }
                    setChangedStateInternal(true, false);
                } else {
                    if (null == tempEntry) {
                        setEntryInternal(entry);
                        validateCurrentEntryInternal();
                        refListener.onBBTEntryLoaded(getEntry());
                        setChangedStateInternal(false, true);
                    } else {
                        validateCurrentEntryInternal();
                        setChangedStateInternal(true, true);
                    }
                }
            }
        };

        public void forceCreate() {
            Log.wtf("entryInfo", "force create");
            internalListener.onBBTEntryLoaded(null);
        }

        private BBTEntryInfo(OneBBTLoader.OnBBTEntryLoadedListener l) {
            this.refListener = l;
        }

        public void setChangedState(boolean changed) {
            setChangedStateInternal(changed, this.existed);
        }

        private void setChangedStateInternal(boolean changed, boolean existed) {
            this.changed = changed;
            this.existed = existed;
        }

        public void clear() {
            entryDate = null;
            timePoint = -1;
            tempEntry = null;
            setChangedStateInternal(false, false);
        }

        private void setEntryInternal(BBTEntry entry) {
            this.tempEntry = entry;
        }


        private boolean validateCurrentEntryInternal() {
            if (null == tempEntry) {
                tempEntry = BBTEntry.getDefaultEntry(entryDate, timePoint);
                Log.e("valid", "entry = " + tempEntry);
                return true;
            }
            tempEntry.entryDate = this.entryDate;
            tempEntry.timePoint = this.timePoint;
            return false;
        }

        void setEntryDateAndTimePoint(String entryDate, int timePoint) {
            this.entryDate = entryDate;
            this.timePoint = timePoint;
            reloadEntry();
        }


        boolean setEntryDate(String entryDate) {
            if (timePoint < 0) {
                throw new RuntimeException("setting entry date while timePoint = " + timePoint);
            }
            if (!entryDate.equals(this.entryDate)) {
                this.entryDate = entryDate;
                reloadEntry();
                return true;
            }
            return false;
        }

        boolean setTimePoint(int timePoint) {
            if (null == entryDate) {
                throw new RuntimeException("setting time point while entryDate is null");
            }
            if (timePoint != this.timePoint) {
                this.timePoint = timePoint;
                reloadEntry();
                return true;
            }
            return false;
        }

        boolean setValue(float value) {
            if (null == tempEntry) {
                throw new RuntimeException("setting value while tempEntry is null");
            }
            if (value != tempEntry.value) {
                tempEntry.value = value;
                return true;
            }
            return false;
        }

        void setRecordDateTime(String recordDateTime) {
            tempEntry.recordDateTime = recordDateTime;
            Log.e("setRecordDateTime", "entry = " + tempEntry);
        }

        void setEntryTime(String entryTime) {
            tempEntry.entryTime = entryTime;
            Log.e("setEntryTime", "entry = " + tempEntry);
        }


        private void reloadEntry() {
            new OneBBTLoader(this.entryDate, this.timePoint, internalListener).execute();
        }

        void writeToDB() {
            new MapPersister().writeToDB(getEntry());
        }


        BBTEntry getEntry() {
            return tempEntry;
        }
    }


}
