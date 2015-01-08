package org.ddrr.bbt.infra;

import java.util.ArrayList;
import java.util.List;

public class BBTEntryStorage {
	public static List<BBTEntry> getBBTEntryList() {
		List<BBTEntry> l = new ArrayList<BBTEntry>();
//		for (int i = 0; i < 100; i++) {
//			BBTEntry entry = new BBTEntry();
//			entry.entryDateTime = 12L * 3600000L * i;
//			entry.note = "note";
//			entry.recordDateTime = 2L;
//			entry.timePoint = i % 2;
//			entry.unit = "degree";
//			entry.value = 35F + i % 3 - entry.timePoint / 2;
//			l.add(entry);
//		}
		return l;
	}
}
