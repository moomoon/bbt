package org.ddrr.bbt;

import android.view.View;
import android.widget.TextView;

import java.util.Map;

public abstract class BaseKeyboardController extends org.ddrr.bbt.KeyboardController {

	public BaseKeyboardController(org.ddrr.bbt.KeyboardCallbacks externalCallbacks) {
		super(externalCallbacks);
		// TODO Auto-generated constructor stub
	}

	@Override
	final protected void setupView(Map<Integer, TextView> views) {
		TextView tvBackspace = views
				.get(R.id.keyboard_button_container_backspace);
		tvBackspace.setText("<");
		tvBackspace.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackspace();
			}
		});
		onSetupView(views);
	}

	protected abstract void onSetupView(Map<Integer, TextView> views);

}
