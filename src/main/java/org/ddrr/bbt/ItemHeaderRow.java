package org.ddrr.bbt;

import android.view.View;

import com.mmscn.utils.SimpleRow;
import com.mmscn.utils.ViewHolder;

public abstract class ItemHeaderRow extends SimpleRow {
	private ItemHeaderController mController;

	public void bindController(ItemHeaderController controller) {
		this.mController = controller;
	}

	@Override
	protected ViewHolder createViewHolder(View v) {
		ItemHeaderViewHolder holder = (ItemHeaderViewHolder) super.createViewHolder(v);
		mController.registerViewHolder(holder);
		return holder;
	}

}
