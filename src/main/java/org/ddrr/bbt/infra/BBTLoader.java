package org.ddrr.bbt.infra;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import org.ddrr.bbt.persistent.BaseProvider;
import org.ddrr.bbt.persistent.EntryBaseProvider;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class BBTLoader implements LoaderCallbacks<Cursor> {
	private final static int LOADER_INDEX = 0;
	private final WeakReference<Context> mContextRef;
	private final Set<BBTEntryLoaderCallbacks> mCallbackSet = Collections
			.newSetFromMap(new WeakHashMap<BBTEntryLoaderCallbacks, Boolean>());
	private Reference<List<BBTEntry>> mLastDistributedListRef;
	private LoaderManager mLM;

	public BBTLoader(Context context) {
		this.mContextRef = new WeakReference<Context>(context);
	}

	public void start(LoaderManager lm) throws LoaderManagerExistedException {
		if (null != mLM) {
			throw new LoaderManagerExistedException();
		}
		mLM = lm;
		lm.initLoader(LOADER_INDEX, null, this).startLoading();
	}

	public void stop() {
		if (null != mLM) {
			mLM.getLoader(LOADER_INDEX).abandon();
		}
	}

	private List<BBTEntry> getLastDistributedList() {
		List<BBTEntry> lastDistributed = null;
		if (null != mLastDistributedListRef) {
			lastDistributed = mLastDistributedListRef.get();
		}
		return lastDistributed;
	}

	public void registerCallback(BBTEntryLoaderCallbacks cb) {
		mCallbackSet.add(cb);
		List<BBTEntry> lastDistributed = getLastDistributedList();
		if (null != lastDistributed) {
			cb.onEntryLoaded(lastDistributed);
		}
	}

	public void unregisterCallback(BBTEntryLoaderCallbacks cb) {
		mCallbackSet.remove(cb);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Context context = mContextRef.get();
		if (null == context) {
			return null;
		}
		Uri uri = EntryBaseProvider.CONTENT_URI;
		return new CursorLoader(context, uri, null, null, null, BaseProvider.KEY_BBT_ENTRY_DATE + " ASC, " + BaseProvider.KEY_BBT_TIME_POINT + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		List<Map<String, String>> rawList = new CursorReader().read(data);
        for(Map<String, String> map : rawList){
            Log.e("onLoadFinished","map = " + map);
        }
		List<BBTEntry> entryList = MapDecorer.createListFromMaps(
				BBTEntry.class, rawList);
		for (BBTEntryLoaderCallbacks cb : mCallbackSet) {
			cb.onEntryLoaded(entryList);
		}
		this.mLastDistributedListRef = new WeakReference<List<BBTEntry>>(
				entryList);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub

	}

	public interface BBTEntryLoaderCallbacks {

		public void onEntryLoaded(List<BBTEntry> list);
	}

	public class LoaderManagerExistedException extends Exception {

		private static final long serialVersionUID = 582054027367957671L;

	}

}
