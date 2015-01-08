package org.ddrr.bbt;

import android.view.View;
import android.widget.TextView;

import java.util.Map;

public class Keyboard0To9 extends BaseKeyboardController {

	public Keyboard0To9(org.ddrr.bbt.KeyboardCallbacks externalCallbacks) {
		super(externalCallbacks);

	}

	@Override
	protected void onSetupView(Map<Integer, TextView> views) {
		for (int i = 0; i < 10; i++) {
			final String value = String.valueOf(i);
			TextView tv = views.get(i);
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
