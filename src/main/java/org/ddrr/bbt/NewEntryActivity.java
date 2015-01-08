package org.ddrr.bbt;

import android.content.Intent;
import android.os.Bundle;

import org.ddrr.bbt.infra.BBTEntry;

public class NewEntryActivity extends BlurActivity {
	public final static String EXTRA_BBT_MRNG = "extra_bbt_mrng";
	public final static String EXTRA_BBT_EVE = "extra_bbt_eve";
	public final static String EXTRA_CURRENT_TIME_POINT = "extra_current_time_point";
	public final static String EXTRA_VALUE_MRNG = "extra_value_mrng";
	public final static String EXTRA_VALUE_EVE = "extra_value_eve";
	private int mCurrentTimePoint = -1;
	private long mTimeMrng = -1L;
	private long mTimeEve;
	private float mValueMrng = Float.NaN;
	private float mValueEve = Float.NaN;
	private BBTEntry mBBTEntryMrng = null;
	private BBTEntry mBBTEntryEve = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_entry);
		// Intent intent = getIntent();
		// int currentTimePoint = intent.getIntExtra(EXTRA_CURRENT_TIME_POINT,
		// -1);
		// if (currentTimePoint < 0) {
		// currentTimePoint = getDefaultTimePoint();
		// }
		// mCurrentTimePoint = currentTimePoint;
		// mValueMrng = intent.getFloatExtra(EXTRA_VALUE_MRNG, Float.NaN);
		// mValueEve = intent.getFloatExtra(EXTRA_VALUE_EVE, Float.NaN);
		Intent intent = getIntent();
		BBTEntry bbtMrng = intent.getParcelableExtra(EXTRA_BBT_MRNG);
		BBTEntry bbtEve = intent.getParcelableExtra(EXTRA_BBT_EVE);
		initBBTEntries(bbtMrng, bbtEve);
	}

	private void initBBTEntries(BBTEntry entryMrng, BBTEntry entryEve) {
		
	}

	@Override
	protected void onBlurFinished() {
		super.onBlurFinished();
		startLayoutAnimation();
	}

	private void startLayoutAnimation() {
		// TODO
	}

	private void fillTextViews() {

	}

	private static int getDefaultTimePoint() {
		return 0;
	}

}
