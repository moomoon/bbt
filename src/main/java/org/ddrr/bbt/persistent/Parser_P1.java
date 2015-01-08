package org.ddrr.bbt.persistent;

import android.util.Log;

import org.ddrr.bbt.infra.BBTEntry;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PhoebeHuyi on 2014.12.25.
 */
public class Parser_P1 extends DIParser {
    @Override
    protected List<String[]> parseLine(String line) {
        final Map<String, String> monthNames = getMonthNameTable();
        List<String[]> result = new ArrayList<>();
        String[] raw = line.split("\\t");
        for (int i = 0; i < raw.length; i++) {
            Log.e("rawLine", "i = " + i + " snippet = " + raw[i]);
        }
        String[] dateSnippets = raw[1].split("\\-");
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.parseInt(dateSnippets[2]) > 69 ? "19" : "20").append(dateSnippets[2]).append('-').append(monthNames.get(dateSnippets[1])).append('-');
        if (dateSnippets[0].length() < 2) {
            sb.append('0');
        }
        sb.append(dateSnippets[0]);
        String date = sb.toString();
        String mValue = raw[2];
        if (mValue.length() > 0) {
            String mTime = raw[3];
            if (null == mTime || mTime.length() == 0) {
                mTime = "6:30";
            }
            result.add(new String[]{date + ' ' + mTime, date, mTime, String.valueOf(BBTEntry.BBT_ENTRY_TIME_POINT_MRNG), mValue, "0"});
        }
        if (raw.length > 4) {
            String eValue = raw[4];
            if (eValue.length() > 0) {
                String eTime = null;
                try {
                    eTime = raw[5];
                } catch (IndexOutOfBoundsException e) {
                }
                if (null == eTime || eTime.length() == 0) {
                    eTime = "11:00";
                }
                result.add(new String[]{date + ' ' + eTime, date, eTime, String.valueOf(BBTEntry.BBT_ENTRY_TIME_POINT_EVE), eValue, "0"});
            }
        }
        return result;
    }

    private Reference<Map<String, String>> mMonthNameRef;

    private Map<String, String> getMonthNameTable() {
        Map<String, String> nameTable = null;
        if (null != mMonthNameRef) {
            nameTable = mMonthNameRef.get();
        }
        if (null == nameTable) {
            nameTable = new HashMap<>();
            final String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            for (int i = 0; i < monthNames.length; i++) {
                String monthName = monthNames[i];
                nameTable.put(monthName, String.format("%02d", i + 1));
            }
            mMonthNameRef = new WeakReference<Map<String, String>>(nameTable);
        }
        return nameTable;
    }
}
