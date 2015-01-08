package org.ddrr.bbt;

import android.widget.ListView;

import com.mmscn.utils.Row;
import com.mmscn.utils.SimpleRowAdapter;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

public abstract class ItemHeaderRowAdapter extends SimpleRowAdapter {
	private final ItemHeaderController mHeaderController = new ItemHeaderController();
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
				lv.setOnScrollListener(mHeaderController);
				lv.setAdapter(this);
			}
			mLvRef = new WeakReference<ListView>(lv);
		}
	}

	@Override
	protected void setRowList(List<Row> l) {
		if (null != l)
			for (Row r : l) {
				if (r instanceof ItemHeaderRow) {
					ItemHeaderRow headerRow = (ItemHeaderRow) r;
					headerRow.bindController(mHeaderController);
				}
			}
		super.setRowList(l);
	}

}
