package org.ddrr.bbt;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class SquareTextView extends TextView {

	public SquareTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SquareTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = MeasureSpec.getSize(widthMeasureSpec);
		int height = getMeasuredHeight();
		if (size > height) {
			size = height;
		}
		setMeasuredDimension(size, size);
	}

}
