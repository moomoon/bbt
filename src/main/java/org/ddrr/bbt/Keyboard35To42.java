package org.ddrr.bbt;

import android.view.View;
import android.widget.TextView;

import java.util.Map;

public class Keyboard35To42 extends BaseKeyboardController {
	private final static int[] LAYOUT = { 40, 41, 42, -1, -1, 35, 36, 37, 38,
			39 };

	public Keyboard35To42(org.ddrr.bbt.KeyboardCallbacks externalCallbacks) {
		super(externalCallbacks);
	}

	@Override
	protected void onSetupView(Map<Integer, TextView> views) {
		for (int i = 0; i < 10; i++) {
			int intValue = LAYOUT[i];
			TextView tv = views.get(i);
			if (intValue < 0) {
                tv.setOnTouchListener(null);
				continue;
			}
			final String value = String.valueOf(intValue) + "  ";
			tv.setText(value);
			tv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					onInput(value);
				}
			});
		}
	}

}
