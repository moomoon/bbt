package org.ddrr.bbt.infra;

import android.text.format.Time;
import android.util.SparseArray;

import com.mmscn.widgets.TimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompBBTEntry {
    public Collection<BBTEntry> mEntries;
    public int mJulianDay;
    public BBTEntry mMorningEntry;
    public BBTEntry mNightEntry;
    private static long gmtoffTest;

    private CompBBTEntry() {

    }

    public static List<CompBBTEntry> enqueue(Collection<BBTEntry> rawEntries) {
        SparseArray<Set<BBTEntry>> sortedArray = new SparseArray<Set<BBTEntry>>();
        if (null != rawEntries) {
            final long gmtoff = -new Time(Time.getCurrentTimezone()).normalize(true) / 1000;
            gmtoffTest = new Time(Time.getCurrentTimezone()).normalize(true);
            for (BBTEntry entry : rawEntries) {
                int julianDay = Time.getJulianDay(TimeUtils.parseDate(entry.entryDate), gmtoff);
                Set<BBTEntry> set = sortedArray.get(julianDay);
                if (null == set) {
                    set = new HashSet<BBTEntry>();
                    sortedArray.put(julianDay, set);
                }
                set.add(entry);
            }
        }
        List<CompBBTEntry> result = new ArrayList<CompBBTEntry>();
        for (int i = 0; i < sortedArray.size(); i++) {
            CompBBTEntry c = new CompBBTEntry();
            result.add(c);
            c.mJulianDay = sortedArray.keyAt(i);
            Set<BBTEntry> entries = sortedArray.valueAt(i);
            for (BBTEntry e : entries) {
                if (null == c.mMorningEntry
                        && e.timePoint == BBTEntry.BBT_ENTRY_TIME_POINT_MRNG) {
                    c.mMorningEntry = e;
                } else if (null == c.mNightEntry
                        && e.timePoint == BBTEntry.BBT_ENTRY_TIME_POINT_EVE) {
                    c.mNightEntry = e;
                }
            }
        }
        return result;
    }
}
