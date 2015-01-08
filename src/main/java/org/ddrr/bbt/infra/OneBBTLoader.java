package org.ddrr.bbt.infra;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.mmscn.utils.RefTask;

import org.ddrr.bbt.BaseApplication;
import org.ddrr.bbt.persistent.BaseProvider;
import org.ddrr.bbt.persistent.EntryBaseProvider;

import java.util.Map;

/**
 * Created by PhoebeHuyi on 2014.12.18.
 */
public class OneBBTLoader extends RefTask<OneBBTLoader.OnBBTEntryLoadedListener,Void, BBTEntry>{
    private final String mEntryDate;
    private final int mTimePoint;
    public OneBBTLoader(String entryDate, int timePoint, OnBBTEntryLoadedListener onBBTEntryLoadedListener) {
        super(onBBTEntryLoadedListener);
        this.mEntryDate = entryDate;
        this.mTimePoint = timePoint;
    }

    @Override
    protected BBTEntry doInBackground(Void... params) {
        BBTEntry result = null;
        ContentResolver cr = BaseApplication.getAppInstance().getContentResolver();
        Uri uri = EntryBaseProvider.CONTENT_URI;
        String[] projections = null;
        String selection = BaseProvider.KEY_BBT_ENTRY_DATE + " = ? AND " + BaseProvider.KEY_BBT_TIME_POINT + " = ?";
        String[] selectionArgs = {mEntryDate, String.valueOf(mTimePoint)};
        String orderBy = null;
        Cursor c = cr.query(uri, projections, selection, selectionArgs, orderBy);
        if(c.moveToFirst()){
            Map<String, String> map = new CursorReader().read(c).get(0);
            result = MapDecorer.createFromMap(BBTEntry.class, map);
            Log.e("result","map = " + map);
        } else{
            Log.e("no result ","sel = " + selection + " date = " + selectionArgs[0] + " timePoint = " + mTimePoint);
        }
        c.close();
        return result;
    }

    @Override
    protected void onResultWithValidInstance(OnBBTEntryLoadedListener instance, BBTEntry result) {
        super.onResultWithValidInstance(instance, result);
        Log.e("onResultWithValidInstance","result = " + result);
        instance.onBBTEntryLoaded(result);
    }

    public interface OnBBTEntryLoadedListener{
        public void onBBTEntryLoaded(BBTEntry entry);
    }
}
