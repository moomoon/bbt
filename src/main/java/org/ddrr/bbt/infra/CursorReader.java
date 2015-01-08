package org.ddrr.bbt.infra;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CursorReader {

	public List<Map<String, String>> read(Cursor cursor) {
		List<Map<String, String>> list = new ArrayList<>();
		if (cursor.moveToFirst()) {
			final String[] columns = cursor.getColumnNames();
			final int columnCount = columns.length;
			if (columnCount > 0) {
				do {
					Map<String, String> map = new LinkedHashMap<>();
					list.add(map);
					for (int i = 0; i < columnCount; i++) {
						map.put(columns[i], cursor.getString(i));
					}
				} while (cursor.moveToNext());
			}
		}
		return list;
	}
}
