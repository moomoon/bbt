package org.ddrr.bbt.infra;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.ddrr.bbt.persistent.BaseProvider;
import org.ddrr.bbt.persistent.MapPersister;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BBTEntry extends MapDecorer<String, String> implements Parcelable {
	public final static int BBT_ENTRY_TIME_POINT_MRNG = 0;
	public final static int BBT_ENTRY_TIME_POINT_EVE = 1;

	@Key(BaseProvider.KEY_BBT_UNIT_TITLE)
    @IgnoreField
	public String unit;
    @Key(key = BaseProvider.KEY_BBT_UNIT_CODE, converter = StringToIntegerConverter.class)
    public int unitCode;
	@Key(BaseProvider.KEY_BBT_RECORD_DATE_TIME)
	public String recordDateTime;
	@Key(BaseProvider.KEY_BBT_ENTRY_DATE)
    @MapPersister.SelectionArg
	public String entryDate;
    @Key(BaseProvider.KEY_BBT_ENTRY_TIME)
    public String entryTime;
	@Key(key = BaseProvider.KEY_BBT_VALUE, converter = StringToFloatConverter.class)
	public float value;
	@Key(BaseProvider.KEY_BBT_NOTE)
	public String note;
	@Key(key = BaseProvider.KEY_BBT_TIME_POINT, converter = StringToIntegerConverter.class)
    @MapPersister.SelectionArg
	public int timePoint;
    @Key(BaseProvider.KEY_BBT_TIME_POINT_LABEL)
    @IgnoreField
    public String timePointLabel;

	public static final Creator<BBTEntry> CREATOR = new Creator<BBTEntry>() {

		@Override
		public BBTEntry createFromParcel(Parcel source) {
			Bundle b = source.readBundle();
			Map<String, String> map = new HashMap<String, String>();
			for (String key : b.keySet()) {
				map.put(key, b.getString(key));
			}
			BBTEntry entry = MapDecorer.createFromMap(BBTEntry.class, map);
			return entry;
		}

		@Override
		public BBTEntry[] newArray(int size) {
			return new BBTEntry[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle b = new Bundle();

		for (Entry<String, String> entry : getMap().entrySet()) {
			b.putString(entry.getKey(), entry.getValue());
		}
		dest.writeBundle(b);

	}

	public static BBTEntry getDefaultEntry(String entryDate, int timePoint) {
        Map<String, String> map = new HashMap<>();
        map.put(BaseProvider.KEY_BBT_ENTRY_DATE, entryDate);
        map.put(BaseProvider.KEY_BBT_TIME_POINT, String.valueOf(timePoint));
        return MapDecorer.createFromMap(BBTEntry.class, map);
    }
}
