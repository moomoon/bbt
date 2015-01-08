package org.ddrr.bbt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ListView;

import org.ddrr.bbt.infra.BBTEntry;
import org.ddrr.bbt.infra.BBTLoader;
import org.ddrr.bbt.infra.BBTLoader.BBTEntryLoaderCallbacks;
import org.ddrr.bbt.infra.CompBBTEntry;
import org.ddrr.bbt.infra.DIReader;

import java.util.List;

public class EntryActivity extends FragmentActivity implements
        BBTEntryLoaderCallbacks, CompBBTAdapter.OnBBTEntrySelectedListener {
    public final static int RESULT_BBT_ENTRY = 0;
    private final BBTLoader mLoader = new BBTLoader(this);
    private final CompBBTAdapter mAdapter = new CompBBTAdapter();

    public EntryActivity() {
        super();
        mAdapter.setOnBBTEntrySelectedListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        mLoader.registerCallback(this);
        ListView lv = (ListView) findViewById(android.R.id.list);
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);
        int totalWidth = p.x;
        int retainedWidth = getResources().getDimensionPixelSize(
                R.dimen.list_bbt_entry_date_width);
        LayoutParams lp = lv.getLayoutParams();
        lp.width = totalWidth * 2 - retainedWidth;
        lv.setLayoutParams(lp);
        FrameLayout.LayoutParams lpBg = (FrameLayout.LayoutParams) findViewById(R.id.entry_graph_bg).getLayoutParams();
        lpBg.width = totalWidth - retainedWidth - lpBg.rightMargin * 2;
        lpBg.setMargins(totalWidth + lpBg.leftMargin, lpBg.topMargin, lpBg.rightMargin, lpBg.bottomMargin);
        EntryGraphForeground egf = (EntryGraphForeground) findViewById(R.id.entry_graph_fg);
        egf.setData(new float[]{35.2F, 35.4F, 35.6F, 35.8F, 36F, 36.2F, 36.4F, 36.6F, 36.8F}, new String[]{"35", "36", "37", "38", "39", "40", "41", "39", "40", "41"});
        egf.setLayoutParams(lpBg);

        try {
            mLoader.start(getSupportLoaderManager());
        } catch (BBTLoader.LoaderManagerExistedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mAdapter.show(lv);
        mAdapter.setOnScrollListener((MonthView) findViewById(R.id.entry_month_view));
    }

    private void startEditActivityWithDefaultDate() {
        Time t = new Time(Time.getCurrentTimezone());
        t.setToNow();
        String entryDate = String.format("%04d-%02d-%02d", t.year, t.month + 1, t.monthDay);
        int timePoint = t.hour >= 12 ? BBTEntry.BBT_ENTRY_TIME_POINT_EVE : BBTEntry.BBT_ENTRY_TIME_POINT_MRNG;
        startEditActivity(entryDate, timePoint);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi = menu.add("NEW");
        mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startEditActivityWithDefaultDate();
                return true;
            }
        });
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mi = menu.add("EXP");
        mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DIReader.getProfiles().get(0).execExport();
                return false;
            }
        });
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mi = menu.add("IMP");
        mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DIReader.getProfiles().get(0).execImport();
                return false;
            }
        });
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    private void startEditActivity(String entryDate, int timePoint) {

        View decorView = ((ViewGroup) getWindow()
                .getDecorView());
        Rect r = new Rect();
        decorView.getWindowVisibleDisplayFrame(r);
        int statusbarHeight = r.top;
        Bitmap bm = Bitmap.createBitmap(
                decorView.getWidth() / 5,
                (decorView.getHeight() - statusbarHeight) / 5,
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        c.scale(0.2F, 0.2F);
        c.translate(0, -statusbarHeight);
        decorView.draw(c);
        Intent intent = new Intent(EntryActivity.this,
                NewEntryActivityTemp.class);
        intent.putExtra(BlurActivity.EXTRA_BACKGROUND, bm);
        intent.putExtra(NewEntryActivityTemp.EXTRA_TIME_POINT, timePoint);
        intent.putExtra(NewEntryActivityTemp.EXTRA_ENTRY_DATE, entryDate);

        startActivity(intent);
        overridePendingTransition(R.anim.blur_activity_stay,
                R.anim.blur_activity_stay);
    }

    @Override
    public void onEntryLoaded(List<BBTEntry> list) {
        mAdapter.setList(CompBBTEntry.enqueue(list));
    }

    @Override
    public void onBBTEntrySelected(String entryDate, int timePoint) {
        startEditActivity(entryDate, timePoint);
    }
}
